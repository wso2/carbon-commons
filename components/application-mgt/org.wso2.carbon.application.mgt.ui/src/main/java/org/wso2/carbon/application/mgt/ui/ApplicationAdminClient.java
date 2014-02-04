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
package org.wso2.carbon.application.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminExceptionException;
import org.wso2.carbon.application.mgt.stub.ApplicationAdminStub;
import org.wso2.carbon.application.mgt.stub.types.carbon.ApplicationMetadata;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.ResourceBundle;


public class ApplicationAdminClient {
    private static final Log log = LogFactory.getLog(ApplicationAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.application.mgt.ui.i18n.Resources";
    private ResourceBundle bundle;
    public ApplicationAdminStub stub;

    public ApplicationAdminClient(String cookie,
                              String backendServerURL,
                              ConfigurationContext configCtx,
                              Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "ApplicationAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new ApplicationAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }

    public String[] getAllApps() throws AxisFault {
        try {
            return stub.listAllApplications();
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
        return null;
    }

    public ApplicationMetadata getAppData(String appNameWithVersion) throws AxisFault {
        try {
            return stub.getAppData(appNameWithVersion);
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
        return null;
    }

    public void deleteApp(String appNameWithVersion) throws AxisFault {
        try {
            stub.deleteApplication(appNameWithVersion);
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.delete.artifact"), e);
        }
    }
    public String[] getAllFaultyApps() throws AxisFault {
        try {
            return stub.listAllFaultyApplications();
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
        return null;
    }
    public String getFaultException(String faultyAppName) throws AxisFault{
        try {
            return stub.getFaultException(faultyAppName);
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
        return null;
    }
    public void deleteFaultyApp(String[] faultyAppName) throws AxisFault{
        try {
            stub.deleteFaultyApplication(faultyAppName);
        } catch (java.lang.Exception e) {
            handleException(bundle.getString("cannot.get.service.data"), e);
        }
    }

//    public void deleteServiceGroup(String sgName) throws AxisFault {
//        try {
//            stub.deleteServiceGroup(sgName);
//        } catch (java.lang.Exception e) {
//            handleException(bundle.getString("cannot.delete.artifact"), e);
//        }
//    }

//    public void deleteModule(String moduleName, String moduleVersion) throws AxisFault {
//        try {
//            stub.deleteModule(moduleName, moduleVersion);
//        } catch (java.lang.Exception e) {
//            handleException(bundle.getString("cannot.delete.artifact"), e);
//        }
//    }

    private void handleException(String msg, java.lang.Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public void downloadCappArchive(String filename, HttpServletResponse response)
            throws IOException, ApplicationAdminExceptionException {

        ServletOutputStream out = response.getOutputStream();
        DataHandler dataHandler = stub.downloadCappArchive(filename);
        if (dataHandler != null) {
            response.setHeader("Content-Disposition", "fileName=" + filename + ".car");
            response.setContentType(dataHandler.getContentType());
            InputStream in = dataHandler.getDataSource().getInputStream();
            int nextChar;
            while ((nextChar = in.read()) != -1) {
                out.write((char) nextChar);
            }
            out.flush();
            in.close();
            out.close();
        } else {
			out.write("The requested capp archive was not found on the server".getBytes());
		}

    }

}
