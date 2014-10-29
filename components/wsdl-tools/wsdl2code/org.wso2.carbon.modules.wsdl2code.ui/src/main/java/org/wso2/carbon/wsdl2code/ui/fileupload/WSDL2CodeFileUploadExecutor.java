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
package org.wso2.carbon.wsdl2code.ui.fileupload;

import org.wso2.carbon.CarbonException;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;

public class WSDL2CodeFileUploadExecutor extends AbstractFileUploadExecutor {

    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".wsdl", ".xml", ".zip"};

    public void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0)
            out.write(buffer, 0, len);

        out.flush();
        out.close();
    }

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {
        PrintWriter out = response.getWriter();
        try {
            List<FileItemData> fileItemDataList = getAllFileItems();
            String filePaths = "";

            //todo why several fileItemData objects for wsdl2code
            for (FileItemData fileItemData : fileItemDataList) {

                FileItem fileItem = fileItemData.getFileItem();
                String fileName = getFileName(fileItem.getName());
                checkServiceFileExtensionValidity(fileName, ALLOWED_FILE_EXTENSIONS);

                //for plain text WSDL files
                if (fileName.endsWith(".wsdl") || fileName.endsWith(".xml")) {

                    String uuid = String.valueOf(
                            System.currentTimeMillis() + Math.random());
                    String serviceUploadDir =
                            configurationContext
                                    .getProperty(ServerConstants.WORK_DIR) +
                                    File.separator +
                                    "extra" + File
                                    .separator +
                                    uuid + File.separator;
                    File dir = new File(serviceUploadDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File uploadedFile = new File(dir, uuid + ".xml");
                    FileOutputStream fileOutStream = new FileOutputStream(uploadedFile);
                    fileItemData.getDataHandler().writeTo(fileOutStream);
                    fileOutStream.flush();
                    fileOutStream.close();
                    response.setContentType("text/plain; charset=utf-8");

                    String outPath = File.separator + "extra" + File.separator +
                            uploadedFile.getAbsolutePath().split(
                            File.separator + "tmp" +
                                    File.separator + "work" +
                                    File.separator + "extra" + File.separator) [1];

                    filePaths = filePaths + outPath + ",";

                    filePaths = filePaths.substring(0, filePaths.length() - 1);
                    out.write(filePaths);
                    out.flush();
                } else if (fileName.endsWith(".zip")) {
                    ZipInputStream zipInputStream = null;
                    try {
                        zipInputStream = new ZipInputStream(fileItemData.getFileItem().getInputStream());
                        String serviceUploadDir =
                                configurationContext
                                        .getProperty(ServerConstants.WORK_DIR) +
                                        File.separator +
                                        "extra" + File
                                        .separator;

                        String uuidDir = String.valueOf(
                                System.currentTimeMillis() + Math.random());

                        String zipPath = serviceUploadDir + uuidDir;
                        new File(zipPath).mkdir();

                        ZipEntry ze;
                        while ((ze = zipInputStream.getNextEntry()) != null) {
                            File destinationFilePath = new File(zipPath, ze.getName());
                            //create directories if required.
                            destinationFilePath.getParentFile().mkdirs();

                            if (!ze.isDirectory()) {
                                String uuid;
                                if (ze.getName().endsWith(".wsdl")) {
                                    uuid = String.valueOf(
                                            System.currentTimeMillis() + Math.random());
                                    destinationFilePath = new File(destinationFilePath.getParent() + File.separator
                                            + uuid + ".xml");
                                    response.setContentType("text/plain; charset=utf-8");
                                    String outPath = File.separator + "extra" + File.separator +
                                            destinationFilePath.getAbsolutePath().split(
                                            File.separator + "tmp" +
                                                    File.separator + "work" +
                                                    File.separator + "extra" + File.separator) [1];

                                    filePaths = filePaths + outPath + ",";
                                }

                                copyInputStream(zipInputStream,
                                        new BufferedOutputStream(new FileOutputStream(destinationFilePath )));
                                zipInputStream.closeEntry();

                            }
                        }
                    } catch (IOException e) {
                        throw new Exception(e);
                    } finally {
                        if (zipInputStream != null) {
                            zipInputStream.close();
                        }
                    }

                    if (filePaths.length() != 0) {
                        filePaths = filePaths.substring(0, filePaths.length() - 1);
                        out.write(filePaths);
                    } else {
                        throw new Exception("No WSDL found in the provided archive " + fileName);
                    }
                    out.flush();	
                }
                log.info("File Successfully Uploaded " + fileName);
            }
        } catch (Exception e) {
            log.error("File upload FAILED", e);
            out.write("<script type=\"text/javascript\">" +
                    "top.wso2.wsf.Util.alertWarning('File upload FAILED. File may be non-existent or invalid.');" +
                    "</script>");
        } finally {
            out.close();
        }
        return true;
    }
}
