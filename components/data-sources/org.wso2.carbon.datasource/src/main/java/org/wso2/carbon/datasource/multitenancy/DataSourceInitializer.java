/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.datasource.multitenancy;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.datasource.*;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.datasource.DataSourceInformationManager;
import org.wso2.carbon.datasource.DataSourceManagementHandler;
import org.wso2.carbon.datasource.internal.DataSourceServiceComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DataSourceInitializer extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(DataSourceInitializer.class);

    private static DataSourceManagementHandler handler = DataSourceManagementHandler.getInstance();
    
    private static Map<Integer, List<DataSourceRepositoryListener>> dsrListenerMap =
            new HashMap<Integer, List<DataSourceRepositoryListener>>();

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            PrivilegedCarbonContext.startTenantFlow();          
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,
			                                                                      true);

        //creating a separate datasource repository for the tenant
        DataSourceInformationRepository repository = new DataSourceInformationRepository();
        DataSourceInformationRepositoryListener listener = new DataSourceRepositoryManager(
                new InMemoryDataSourceRepository(), new JNDIBasedDataSourceRepository());
        //initializing the repository with the repository listener corresponding to the tenant
        repository.setRepositoryListener(listener);

        try {
            DataSourceInformationManager dsManager =
                    this.getDataSourceManagementHandler().getTenantDataSourceInformationManager();
            if (dsManager == null) {
                dsManager = new DataSourceInformationManager();
                dsManager.setRepository(repository);
                dsManager.setRepository(repository);
                dsManager.setRegistry(DataSourceServiceComponent.
                    getRegistryService().getConfigSystemRegistry(tenantId));
                dsManager.populateDataSourceInformation();

                this.getDataSourceManagementHandler().addDataSourceManager(tenantId, dsManager);
            }
        } catch (RegistryException e) {
            log.error(e);
        }

        List<DataSourceRepositoryListener> dsrListeners = dsrListenerMap.get(tenantId);
        if (dsrListeners != null) {
        	/* notify the listeners */
        	for (DataSourceRepositoryListener dsrListener : dsrListeners) {
        		dsrListener.setDataSourceRepository(repository);
        	}
        	dsrListenerMap.remove(tenantId);
        }

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
    
    public static void addDataSourceRepositoryListener(int tenantId,
                                                       DataSourceRepositoryListener listener) {
        Map<Integer, DataSourceInformationManager> dsManagerMap =
                DataSourceManagementHandler.getInstance().getDataSourceManagerMap();
    	synchronized (dsManagerMap) {
    		DataSourceInformationRepository repository = dsManagerMap.get(tenantId).getRepository();
        	if (repository != null) {
        		listener.setDataSourceRepository(repository);
    		} else {
    			List<DataSourceRepositoryListener> listeners = dsrListenerMap.get(tenantId);
    			if (listeners == null) {
    				listeners = new Vector<DataSourceRepositoryListener>();
    				dsrListenerMap.put(tenantId, listeners);
    			}
    			listeners.add(listener);
    		}
		}
    }

    public DataSourceManagementHandler getDataSourceManagementHandler() {
        return handler;
    }

}
