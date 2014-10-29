package org.wso2.carbon.hostobjects.carbonutil.internal;

import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.hostobject.carbonutil" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class CarbonHostObjectServiceComponent {

	protected void setRealmService(RealmService realmService) {
		ServiceHodler.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
    	ServiceHodler.setRealmService(null);
    }
	
}
