/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.deployment.synchronizer.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.deployment.synchronizer.stub.types.DeploymentSynchronizerAdminStub;
import org.wso2.carbon.deployment.synchronizer.stub.types.util.DeploymentSynchronizerConfiguration;
import org.wso2.carbon.deployment.synchronizer.stub.types.util.RepositoryConfigParameter;

import java.util.Locale;

public class DeploymentSyncAdminClient {

    private DeploymentSynchronizerAdminStub stub;

    public DeploymentSyncAdminClient(ConfigurationContext configCtx, String backendServerURL,
                                   String cookie, Locale locale) throws AxisFault {

        String serviceURL = backendServerURL + "DeploymentSynchronizerAdmin";
        stub = new DeploymentSynchronizerAdminStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public DeploymentSynchronizerConfiguration getConfiguration() throws Exception {
        return stub.getSynchronizerConfigurationForCarbonRepository();
    }

    public void disableSynchronizer() throws Exception {
        stub.disableSynchronizerForCarbonRepository();
    }

    public void enableSynchronizer(DeploymentSynchronizerConfiguration config) throws Exception {
        stub.enableSynchronizerForCarbonRepository(config);
    }

    public void updateSynchronizer(DeploymentSynchronizerConfiguration config) throws Exception {
        stub.updateSynchronizerForCarbonRepository(config);
    }

    public long getLastCommitTime() throws Exception {
        return stub.getLastCommitTime();
    }

    public long getLastCheckoutTime() throws Exception {
        return stub.getLastCheckoutTime();
    }

    public void commit() throws Exception {
        stub.commit();
    }

    public void checkout() throws Exception {
        stub.checkout();
    }
    
    public RepositoryConfigParameter[] getParamsByRepositoryType(String repositoryType) throws Exception{
    	return stub.getParamsByRepositoryType(repositoryType);
    }
    
    public String[] getRepositoryTypes() throws Exception{
    	return stub.getRepositoryTypes();
    }

}
