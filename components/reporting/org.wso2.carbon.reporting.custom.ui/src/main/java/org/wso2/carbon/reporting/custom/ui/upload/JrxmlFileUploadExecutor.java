/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.reporting.custom.ui.upload;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.reporting.custom.ui.client.JrxmlFileUploaderClient;
import org.wso2.carbon.ui.CarbonUIMessage;
import org.wso2.carbon.ui.transports.fileupload.AbstractFileUploadExecutor;
import org.wso2.carbon.utils.FileItemData;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * used to execute report template upload functionality
 */
public class JrxmlFileUploadExecutor extends AbstractFileUploadExecutor {
    private static final String[] ALLOWED_FILE_EXTENSIONS = new String[]{".jrxml", ".xml"};
    private static Log log = LogFactory.getLog(JrxmlFileUploadExecutor.class);
    private String redirect;


    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws CarbonException, IOException {

        String webContext = (String) request.getAttribute(CarbonConstants.WEB_CONTEXT);
        String serverURL = (String) request.getAttribute(CarbonConstants.SERVER_URL);
        String cookie = (String) request.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        boolean status = false;
        Map<String, ArrayList<FileItemData>> fileItemsMap = getFileItemsMap();
        Map<String, ArrayList<String>> formFieldsMap = getFormFieldsMap();

        if (fileItemsMap == null || fileItemsMap.isEmpty()) {
            String msg = "File uploading failed. No files are specified";
            log.error(msg);
            CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request,
                    response, getContextRoot(request) + "/" +
                    webContext +
                    "/create-reports/list-reports.jsp");

            return false;
        }
        JrxmlFileUploaderClient uploaderClient = new JrxmlFileUploaderClient(cookie, serverURL,
                configurationContext);
        List<FileItemData> fileItems = fileItemsMap.get("upload");
        String errorRedirect = null;

        try {
            for (FileItemData fileItem : fileItems) {
                String filename = getFileName(fileItem.getFileItem().getName());
                try {
                    checkServiceFileExtensionValidity(filename, ALLOWED_FILE_EXTENSIONS);
                } catch (FileUploadException e) {
                    log.error("Failed to validate format of file : " + filename, e);
                    throw e;
                }
                if (!filename.endsWith(".jrxml")) {
                    throw new CarbonException("File with extension " +
                            getFileName(fileItem.getFileItem().getName())
                            + " is not supported!");
                }
                String uploadStatue = uploaderClient.uploadJrxmlFile(filename.split(".jrxml")[0], fileItem.getFileItem().getString());
                if (uploadStatue.equals("success")) {
                    status = true;
                }
                response.setContentType("text/html; charset=utf-8");
                String msg = "Successfully uploaded jrxml file.";

                if (redirect == null) {
                    CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
                    response.sendRedirect("../" + webContext + "/reporting_custom/list-reports.jsp?region=region5&item=reporting_list");
                } else {
                    response.sendRedirect("../" + webContext + "/" + redirect);
                }


            }
            return status;
        } catch (IOException e) {
            // This happens if an error occurs while sending the UI Error Message.
            String msg = "File upload failed. " + e.getMessage();
            log.error("File upload failed. ", e);

            if (errorRedirect == null) {
                CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.INFO, request);
                response.sendRedirect("../" + webContext + "/reporting_custom/list-reports.jsp");
            } else {
                response.sendRedirect(
                        "../" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                                == -1 ? "?" : "&") + "msg=" + URLEncoder.encode(msg, "UTF-8"));
            }
            return false;
        } catch (RuntimeException e) {
            // we explicitly catch runtime exceptions too, since we want to make them available as
            // UI Errors in this scenario.
            String msg = "File upload failed. " + e.getMessage();
            log.error("File upload failed. ", e);
            buildUIError(request, response, webContext, errorRedirect, msg);
            return false;
        } catch (Exception e) {
            String msg = "File upload failed. " + e.getMessage();
            log.error("File upload failed. ", e);

            buildUIError(request, response, webContext, errorRedirect, msg);
            return status;
        }
    }

    private void buildUIError(HttpServletRequest request, HttpServletResponse response,
                              String webContext, String errorRedirect, String msg)
            throws IOException {
        CarbonUIMessage.sendCarbonUIMessage(msg, CarbonUIMessage.ERROR, request);
        if (errorRedirect == null) {
            response.sendRedirect(
                    "../" + webContext + "/admin/error.jsp");
        } else {
            response.sendRedirect(
                    "../" + webContext + "/" + errorRedirect + (errorRedirect.indexOf("?")
                            == -1 ? "?" : "&") + "msg=" + URLEncoder.encode(msg, "UTF-8"));
        }
    }
}
