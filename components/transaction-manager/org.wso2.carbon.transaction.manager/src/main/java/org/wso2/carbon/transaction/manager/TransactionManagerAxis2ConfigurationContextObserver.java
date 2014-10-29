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

package org.wso2.carbon.transaction.manager;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class represents an configuration context observer, used to register the "TransactionManager"
 * ,"UserTransaction" with JNDI when a new tenant arrives.
 */
public class TransactionManagerAxis2ConfigurationContextObserver extends AbstractAxis2ConfigurationContextObserver {

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
        int tid = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        TransactionManagerComponent.bindTransactionManagerWithJNDIForTenant(tid);
    }

}
