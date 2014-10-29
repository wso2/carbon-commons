/*
 * Copyright WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.url.mapper.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.url.mapper.stub.UrlMapperAdminServiceStub;
import org.wso2.carbon.url.mapper.stub.types.carbon.MappingData;
import org.wso2.carbon.url.mapper.stub.types.carbon.PaginatedMappingData;


/**
 * This is the client to communicate with backend when the tenant tries to
 * create new virtual host by specifying webapp specific id..
 */
public class UrlMapperServiceClient {
    UrlMapperAdminServiceStub stub;
    private static final Log log = LogFactory.getLog(UrlMapperServiceClient.class);

    public UrlMapperServiceClient(String cookie, String backendServerURL,
                                  ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "UrlMapperAdminService";
        stub = new UrlMapperAdminServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        option.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
    }
    
    public MappingData[] getAllMappings() throws Exception {
    	try {
            return stub.getAllMappings();
        } catch (Exception e) {
            String msg = "Error getting URL Mappings. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }
    
    public  String getHttpPort () throws Exception {
    	try {
            return stub.getHttpPort();
        } catch (Exception e) {
            String msg = "Error getting URL Mappings. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }
    
    public String getPrefix () throws Exception {
    	try {
            return stub.getDomainNamePrefix();
        } catch (Exception e) {
            String msg = "Error getting URL Mappings. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }
    public  PaginatedMappingData getPaginatedMappings(int pageNumber, String tenantDomain) throws Exception {
    	  try {
              return stub.getPaginatedMappings(pageNumber,tenantDomain);
          } catch (Exception e) {
              String msg = "Error occurred while editing  host. Backend service may be unavailable";
              log.error(msg, e);
              throw e;
          }
    }
    
    public boolean isMappingLimitExceeded(String webAppName) throws Exception  {
    	  try {
              return stub.isMappingLimitExceeded(webAppName);
          } catch (Exception e) {
              String msg = "Error occurred while editing  host. Backend service may be unavailable";
              log.error(msg, e);
              throw e;
          }
    }
    

    public boolean editHost(String webappName, String oldhost, String newHost) throws Exception {
        try {
            return stub.editHost(webappName, oldhost, newHost);
        } catch (Exception e) {
            String msg = "Error occurred while editing  host. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void editServiceDomain(String newHost, String oldhost) throws Exception {
        try {
            stub.editServiceDomain(newHost, oldhost);
        } catch (Exception e) {
            String msg = "Error occurred while editing service host. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void removeServiceDomain(String host) throws Exception {
        try {
            stub.deleteServiceDomain(host);
        } catch (Exception e) {
            String msg = "Error occurred while deleting service host. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void addServiceDomain(String host, String epr, String appType) throws Exception {
        try {
            stub.addServiceDomain(host, epr, appType);
        } catch (Exception e) {
            String msg = "Error occurred while adding new domain to " + epr
                    + ". Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public void deleteHost(String host) throws Exception {
        try {
            stub.deleteHost(host);
        } catch (Exception e) {
            String msg = "Error occurred while deleting host. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    /**
     * @param appId   the tenant specified application ID for a web app
     * @param context the context of the web app from the appserver localhost
     * @throws Exception throws when exception in adding virtual host or webapp
     */
    public void addWebAppToHost(String appId, String context, String appType) throws Exception {
        try {
            String hostName = appId;
            boolean isDomain = isMappingExist(hostName);

            if (isDomain) {
            } else {
                stub.addWebAppToHost(hostName, context, appType);
            }
        } catch (Exception e) {
            String msg = "Error occurred while adding webb app to host. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public String[] getHostForWebApp(String webAppName) throws Exception {
        try {
            return stub.getHostForWebApp(webAppName);
        } catch (Exception e) {
            String msg = "Error occurred while getting hosts for web application. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public String[] getHostForEpr(String epr) throws Exception {
        try {
            return stub.getHostForEpr(epr);
        } catch (Exception e) {
            String msg = "Error occurred while getting hosts for web application. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }

    public boolean isMappingExist(String mappingName) throws Exception {
        try {
            return stub.isMappingExist(mappingName);
        } catch (Exception e) {
            String msg = "Error occurred while checking domain exsistance. Backend service may be unavailable";
            log.error(msg, e);
            throw e;
        }
    }
}
