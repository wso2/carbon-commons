package org.wso2.carbon.logging.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.user.core.service.RealmService;

public class DataHolder {
	private static DataHolder dataHolder = new DataHolder();
	private RealmService realmService;
	private Registry registry;
	private ConfigurationContext contextService;

	public static DataHolder getInstance() {
		return dataHolder;
	}

	private DataHolder() {
	}

	public void setRealmService(RealmService realmService) {
		this.realmService = realmService;
	}

	public RealmService getRealmService() {
		return realmService;
	}

	public void setServerConfigContext(ConfigurationContext configContext) {
		this.contextService = configContext;
	}

	public ConfigurationContext getServerConfigContext() {
		return this.contextService;
	}

	public void setRegistry(Registry registryParam) {
		registry = registryParam;
	}

	public Registry getRegistry() {
		return registry;
	}
}
