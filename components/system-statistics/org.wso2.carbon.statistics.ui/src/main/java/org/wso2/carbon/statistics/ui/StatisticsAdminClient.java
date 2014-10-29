/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.statistics.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.statistics.stub.StatisticsAdminStub;
import org.wso2.carbon.statistics.stub.types.carbon.OperationStatistics;
import org.wso2.carbon.statistics.stub.types.carbon.ServiceStatistics;
import org.wso2.carbon.statistics.stub.types.carbon.SystemStatistics;
import org.wso2.carbon.statistics.stub.webapp.StatisticData;


import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 */
public class StatisticsAdminClient {

    private static final Log log = LogFactory.getLog(StatisticsAdminClient.class);
    private static final String BUNDLE = "org.wso2.carbon.statistics.ui.i18n.Resources";
    private StatisticsAdminStub stub;
    private ResourceBundle bundle;

    public StatisticsAdminClient(String cookie,
                                 String backendServerURL,
                                 ConfigurationContext configCtx,
                                 Locale locale) throws AxisFault {
        String serviceURL = backendServerURL + "StatisticsAdmin";
        bundle = ResourceBundle.getBundle(BUNDLE, locale);

        stub = new StatisticsAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);            

    }

    public SystemStatistics getSystemStatistics() throws RemoteException {
        try {
            return stub.getSystemStatistics();
        } catch (RemoteException e) {
            if (e.getMessage().contains("Backend server may be unavailable")) {
                log.debug("Backend server may be unavailable", e);
                throw new RemoteException("Backend server may be unavailable", e);
            }

            handleException(bundle.getString("cannot.get.system.stats"), e);
        }
        return null;
    }

    public ServiceStatistics getServiceStatistics(String serviceName) throws RemoteException {
        try {
            return stub.getServiceStatistics(serviceName);
        } catch (RemoteException e) {
            if (e.getMessage().contains("Backend server may be unavailable")) {
                log.debug("Backend server may be unavailable", e);
                return null;
            }

            handleException(MessageFormat.format(bundle.getString("cannot.get.service.stats"),
                    serviceName), e);
        }
        return null;
    }

    public OperationStatistics getOperationStatistics(String serviceName,
                                                      String operationName) throws RemoteException {
        try {
            return stub.getOperationStatistics(serviceName, operationName);
        } catch (RemoteException e) {
            handleException(MessageFormat.format(bundle.getString("cannot.get.operation.stats"),
                    serviceName, operationName), e);
        }
        return null;
    }

    private void handleException(String msg, Exception e) throws RemoteException {
        log.error(msg, e);
        throw new RemoteException(msg, e);
    }

    public StatisticData getWebappStatistics(String webApp) throws Exception{
//        StatisticData statisticData = null;
        try{
            StatisticData   statisticData =   stub.getWebappRelatedData(webApp);
            if(statisticData==null){
                statisticData = new StatisticData();
                statisticData.setRequstCount(0);
                statisticData.setResponseCount(0);
                statisticData.setFaultCount(0);
                statisticData.setMaximumResponseTime(0.0);
                statisticData.setMinimumresponseTime(0.0);
                statisticData.setAverageResponseTime(0.0);
            }
            return statisticData;
        }catch (RemoteException e){
            if (e.getMessage().contains("Backend server may be unavailable")) {
                log.debug("Backend server may be unavailable", e);
                throw new RemoteException("Backend server may be unavailable", e);
            }

            if(e.getMessage().contains("The input stream for an incoming message is null.")) {
                log.debug("Backend server may be unavailable", e);
                throw new RemoteException("Backend server may be unavailable", e);
            }

            handleException(bundle.getString("cannot.get.webapps.stats"), e);
        }

        return  null;

    }
}
