package org.wso2.carbon.hostobjects.carbonutil.internal;

import org.wso2.carbon.user.core.service.RealmService;

public class ServiceHodler {
	
	private static RealmService realmService;

	public static RealmService getRealmService() {
		return realmService;
	}

	public static void setRealmService(RealmService realmService) {
		ServiceHodler.realmService = realmService;
	}

}
