/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.core.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.reporting.core.utils.CommonUtil;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


public class ReportingAxis2ConfigurationContextObserver implements Axis2ConfigurationContextObserver {
    @Override
    public void creatingConfigurationContext(int i) {

    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Registry tenantRegistry = ReportingComponent.getRegistryService().getRegistry(
                    CarbonConstants.REGISTRY_SYSTEM_USERNAME, tenantId);
            CommonUtil.addJrxmlConfigs(tenantRegistry);
        } catch (RegistryException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {

    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configurationContext) {

    }
}
