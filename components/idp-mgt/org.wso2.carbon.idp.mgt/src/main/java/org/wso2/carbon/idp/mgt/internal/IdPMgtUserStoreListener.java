/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.idp.mgt.internal;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.idp.mgt.dao.IdPMgtDAO;
import org.wso2.carbon.idp.mgt.exception.IdentityProviderMgtException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;

public class IdPMgtUserStoreListener extends AbstractUserOperationEventListener {

    private IdPMgtDAO dao = new IdPMgtDAO();

    public boolean doPostUpdateRoleName (String newRoleName, String oldRoleName, UserStoreManager um) throws UserStoreException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            dao.renameTenantRole(newRoleName, tenantId, oldRoleName, tenantDomain);
        } catch (IdentityProviderMgtException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
        return true;
    }

    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) throws UserStoreException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        try {
            dao.deleteTenantRole(tenantId, roleName, tenantDomain);
        } catch (IdentityProviderMgtException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
        return true;
    }
}
