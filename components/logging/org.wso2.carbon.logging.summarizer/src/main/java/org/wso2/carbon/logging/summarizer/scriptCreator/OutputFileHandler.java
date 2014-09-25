package org.wso2.carbon.logging.summarizer.scriptCreator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfig;
import org.wso2.carbon.logging.summarizer.utils.LoggingConfigManager;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class OutputFileHandler {

    private static final Log log = LogFactory.getLog(OutputFileHandler.class);
    LoggingConfig config = LoggingConfigManager.loadLoggingConfiguration();
    String archivedLogLocation = config.getArchivedLogLocation();
    String hdfsConfig = config.getHdfsConfig();

    public void fileReStructure(String colFamilyName) throws IOException {
        log.info("CF "+colFamilyName);


        Configuration conf = new Configuration(false);
        /**
         * Create HDFS Client configuration to use name node hosted on host master and port 9000.
         * Client configured to connect to a remote distributed file system.
         */
        conf.set("fs.default.name", hdfsConfig);
        conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");

        /**
         * Get connection to remote file sytem
         */
        FileSystem fs = FileSystem.get(conf);

        /**
         * Crate file sourcePath object
         */
        Path filePath = new Path(archivedLogLocation);

        String tmpStrArr[] = colFamilyName.split("_");
        String tenantId = tmpStrArr[1];
        String serverName = tmpStrArr[2];

        String createdDate = tmpStrArr[3] + "_" + tmpStrArr[4] + "_" + tmpStrArr[5];
        String directoryPathName = archivedLogLocation + tenantId + "/" + serverName + "/";
        String filePathName = directoryPathName + createdDate;
        log.info("filePathName "+filePathName);
        log.info("createdDate "+createdDate);
        //Rename the 000000_0 file as a .tmp file
        Path sourceFileName = new Path(filePathName + "/000000_0");
        Path destnFileName = new Path(filePathName + "/" + createdDate + ".tmp");



        boolean  isRenamed = fs.rename(sourceFileName, destnFileName);
        log.info("rename "+isRenamed);
        
        /*if (!isRenamed) {
            String path = sourceFileName.toString();
            FileStatus[] status = fs.listStatus(new Path("/stratos/archivedLogs/212/")); // you need to
            log.info(status);
            if (status != null) {
                for (int i = 0; i < status.length; i++) {
                    log.info("X:" + status[i].getPath());
                }
            } else {
                log.info("Null");
            }
            // in your hdfs path
        }*/

        //To remove the unicode character in the created .tmp file
        if(isRenamed) {
            Path sanitizedFileName = new Path(filePathName + "/" + createdDate + ".log");
            replaceChar(destnFileName, sanitizedFileName, fs);

            log.info("Logs of Tenant " + tenantId + " of " + serverName + " on " + createdDate + " are successfully archived");
        } else {
            log.info("Logs of Tenant " + tenantId + " of " + serverName + " on " + createdDate + " are not ******* successfully archived");

        }


    }

    public static void replaceChar(Path oldPath, Path newPath, FileSystem fs)
            throws IOException {
        FSDataInputStream in = fs.open(oldPath);
        FSDataOutputStream out = fs.create(newPath);
        BufferedReader dataInput = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = dataInput.readLine()) != null) {
            out.write(line.replace("\001", "").getBytes());
            out.write("\n".getBytes());
        }

        out.close();
        dataInput.close();
        if (fs.exists(oldPath)) {
            fs.delete(oldPath, true);
        }
        in.close();
    }

    public void compressLogFile(String temp) throws IOException {
        File file = new File(temp);
        FileOutputStream outputStream = new FileOutputStream(file + ".gz");
        GZIPOutputStream gzos = new GZIPOutputStream(outputStream);
        FileInputStream inputStream = new FileInputStream(temp);
        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[1024];
        int i;
        while ((i = in.read(buffer)) >= 0) {
            gzos.write(buffer, 0, i);
        }
        in.close();
        gzos.close();
    }


    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
