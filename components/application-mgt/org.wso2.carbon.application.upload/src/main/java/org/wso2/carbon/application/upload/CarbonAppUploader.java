/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.application.upload;

import org.apache.axis2.AxisFault;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.utils.CarbonUtils;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Carbon Application Uploader service.
 */
public class CarbonAppUploader extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(CarbonAppUploader.class);

    private void validateFileName(String fileName, String bpelTempPath) throws AxisFault {

        if (StringUtils.isBlank(fileName)) {
            throw new AxisFault("Invalid file name. File name is not available.");
        }

        String fileExtension = "";
        int i = fileName.lastIndexOf('.');
        if (i >= 0) { fileExtension = fileName.substring(i+1); }

        if (!(fileExtension.equalsIgnoreCase("jar") || fileExtension.equalsIgnoreCase("car"))) {
            throw new AxisFault("Invalid file type : " + fileExtension);
        }

        String tempDirCanonical, tempFileCanonical;

        try {
            // Resolve the canonical paths of the temporary directory and the file
            tempDirCanonical = new File(bpelTempPath).getCanonicalPath();
            tempFileCanonical = new File(bpelTempPath, fileName).getCanonicalPath();
        } catch (IOException e) {
            throw new AxisFault("IOError: File name validation failed.", e);
        }

        // Verify if the file is in the intended temporary directory
        if (!tempFileCanonical.startsWith(tempDirCanonical)) {
            throw new AxisFault("Invalid file name. Attempt to create file outside of the designated directories.");
        }
    }

    public void uploadApp(UploadedFileItem[] fileItems) throws AxisFault {
        try {
            // create carbonapps dir if it doesn't already exists
            String carbonAppDir = CarbonUtils.getCAppDeploymentDirPath(getAxisConfig());
            createDir(carbonAppDir);

            String carbonHomeTmp = CarbonUtils.getCarbonHome() + File.separator + "tmp";
            createDir(carbonHomeTmp);
            String carbonAppDirTemp = carbonHomeTmp + File.separator + "carbonappsuploads";
            createDir(carbonAppDirTemp);

            for (UploadedFileItem uploadedFile : fileItems) {
                String fileName = uploadedFile.getFileName();

                validateFileName(fileName, carbonAppDirTemp);
                if (uploadedFile.getFileType().equals("jar")) {
                    writeResource(uploadedFile.getDataHandler(), carbonAppDirTemp, carbonAppDir,
                            fileName);
                } else {
                    throw new AxisFault("Invalid file type : " + uploadedFile.getFileType());
                }
            }
        } catch (Exception e) {
            String msg = "Error occurred while uploading Carbon App artifacts";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private void writeResource(DataHandler dataHandler, String tempDestPath, String destPath,
                               String fileName)
            throws IOException {

        File tempDestFile = new File(tempDestPath, fileName);
        File destFile = new File(destPath, fileName);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(tempDestFile);
            dataHandler.writeTo(fos);
            fos.flush();

            /* File stream is copied to a temp directory in order handle hot deployment issue
               occurred in windows */
            FileUtils.copyFile(tempDestFile,destFile);
        } catch (FileNotFoundException e) {
            log.error("Cannot find the file", e);
            throw e;
        } catch (IOException e) {
            log.error("IO error.");
            throw e;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (!tempDestFile.delete()) {
                    log.warn("temp file: " + tempDestFile.getAbsolutePath() +
                            " deletion failed, scheduled deletion on server exit.");
                    tempDestFile.deleteOnExit();
                }
            } catch (IOException e) {
                log.warn("Can't close file streams.", e);
            }
        }
    }

    private void createDir(String path) throws Exception {
        File temp = new File(path);
        if (!temp.exists() && !temp.mkdir()) {
            String msg = "Error while creating directory : " + path;
            log.error(msg);
            throw new Exception(msg);
        }
    }

}
