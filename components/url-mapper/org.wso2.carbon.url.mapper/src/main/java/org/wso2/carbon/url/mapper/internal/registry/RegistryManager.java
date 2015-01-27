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
package org.wso2.carbon.url.mapper.internal.registry;

import org.apache.catalina.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.url.mapper.data.MappingData;
import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.url.mapper.internal.util.UrlMapperConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry manager to store and retrieve properties to registry.
 */
public class RegistryManager {
	private static Log log = LogFactory.getLog(RegistryManager.class);
	private static Registry registryService = DataHolder.getInstance().getRegistry();

	/**
	 * when getting hosts from registry getHostsFromRegistry method will return
	 * all the hosts in the registry. As like addHostToRegistry will add the
	 * hostinfo to the registry
	 * 
	 * @param hostName
	 *            The virtual host to be stored in the registry
	 * @param webApp
	 *            The webapp to be deployed in the virtual host
	 * @throws Exception
	 */
	public void addHostToRegistry(String hostName, String webApp, String tenantDomain, String appName)
			throws Exception {
		try {
			registryService.beginTransaction();
			Resource hostResource = registryService.newResource();
			hostResource.addProperty(UrlMapperConstants.HostProperties.HOST_NAME, hostName);
			hostResource.addProperty(UrlMapperConstants.HostProperties.WEB_APP, webApp);
			hostResource.addProperty(UrlMapperConstants.HostProperties.TENANT_DOMAIN, tenantDomain);
            hostResource.addProperty(UrlMapperConstants.HostProperties.APP_NAME, appName);
			registryService
					.put(UrlMapperConstants.HostProperties.HOSTINFO + hostName, hostResource);
			registryService.commitTransaction();
		} catch (Exception e) {
			registryService.rollbackTransaction();
			log.error("Unable to add the host", e);
			throw e;
		}
	}

	 

    public MappingData[] getTenantSpecificMappingsFromRegistry(String tenantDomain) throws Exception {
    	
    	Collection mappings = getHostsFromRegistry();
		List<MappingData> mappingList = new ArrayList<MappingData>();
		if (mappings != null) {
			String[] mappingNames = mappings.getChildren();
			for (String mappingName : mappingNames) {
				mappingName = mappingName.replace(UrlMapperConstants.HostProperties.FILE_SERPERATOR
            	        + UrlMapperConstants.HostProperties.HOSTINFO , "");
				Resource resource = getMappingFromRegistry(mappingName);
				if (resource != null) {
					MappingData mappingData = new MappingData();
					mappingData.setMappingName(resource
							.getProperty(UrlMapperConstants.HostProperties.HOST_NAME));
					mappingData.setTenantDomain(resource
							.getProperty(UrlMapperConstants.HostProperties.TENANT_DOMAIN));
					if (resource.getProperty(UrlMapperConstants.HostProperties.SERVICE_EPR) != null) {
						mappingData.setServiceMapping(true);
						mappingData.setUrl(resource
								.getProperty(UrlMapperConstants.HostProperties.SERVICE_EPR));
					} else {
						mappingData.setUrl(resource
								.getProperty(UrlMapperConstants.HostProperties.WEB_APP));
					}
					if (tenantDomain == null || tenantDomain.equals("")) {
						mappingList.add(mappingData);
					} else if (tenantDomain.equals(mappingData.getTenantDomain())) {
						mappingList.add(mappingData);
					}
					
				}

			}
			return mappingList.toArray(new MappingData[mappingList.size()]);
		}
		return null;
    }
  

	/**
	 * when getting hosts from registry getHostsFromRegistry method will return
	 * all the hosts in the registry. As like addHostToRegistry will add the
	 * hostinfo to the registry
	 * 
	 * @param hostName
	 *            The virtual host to be stored in the registry
	 * @param webApp
	 *            The webapp to be deployed in the virtual host
	 * @throws Exception
	 */
	public void addEprToRegistry(String hostName, String webApp, String tenantDomain, String appName)
			throws Exception {

		try {
			registryService.beginTransaction();
			Resource hostResource = registryService.newResource();
			hostResource.addProperty(UrlMapperConstants.HostProperties.HOST_NAME, hostName);
			hostResource.addProperty(UrlMapperConstants.HostProperties.SERVICE_EPR, webApp);
			hostResource.addProperty(UrlMapperConstants.HostProperties.TENANT_DOMAIN, tenantDomain);
            hostResource.addProperty(UrlMapperConstants.HostProperties.APP_NAME, appName);
			registryService
					.put(UrlMapperConstants.HostProperties.HOSTINFO + hostName, hostResource);
			registryService.commitTransaction();
		} catch (Exception e) {
			registryService.rollbackTransaction();
			log.error("Unable to add the host", e);
			throw e;
		}
	}

	/**
	 * @param host
	 *            The virtual host to be stored in the registry
	 * @param webApp
	 *            The webapp to be deployed in the virtual host
	 * @throws Exception
	 */
	public void updateHostToRegistry(Host host, String webApp) throws Exception {
		try {
			registryService.beginTransaction();
			Resource hostResource;
			String hostResourcePath = (UrlMapperConstants.HostProperties.HOSTINFO + host.getName());
			if (registryService.resourceExists(hostResourcePath)) {
				hostResource = registryService.get(hostResourcePath);
				hostResource.setProperty(UrlMapperConstants.HostProperties.HOST_NAME,
						host.getName());
				hostResource.setProperty(UrlMapperConstants.HostProperties.WEB_APP, webApp);
				registryService.put(UrlMapperConstants.HostProperties.HOSTINFO + host.getName(),
						hostResource);
			} else {
				hostResource = registryService.newResource();
				hostResource.addProperty(UrlMapperConstants.HostProperties.HOST_NAME,
						host.getName());
				hostResource.addProperty(UrlMapperConstants.HostProperties.WEB_APP, webApp);
				registryService.put(UrlMapperConstants.HostProperties.HOSTINFO + host.getName(),
						hostResource);
			}
			registryService.commitTransaction();

		} catch (Exception e) {
			registryService.rollbackTransaction();
			log.error("Unable to update the host", e);
			throw e;
		}
	}

	/**
	 * @param hostName
	 *            virtual host name to be removed
	 * @throws Exception
	 */
	public void removeFromRegistry(String hostName) throws Exception {
		try {
			registryService.beginTransaction();
			String hostResourcePath = UrlMapperConstants.HostProperties.HOSTINFO + hostName;
			if (registryService.resourceExists(hostResourcePath)) {
				registryService.delete(hostResourcePath);
			}
			registryService.commitTransaction();

		} catch (Exception e) {
			registryService.rollbackTransaction();
			log.error("Unable to remove the host", e);
			throw e;
		}
	}

	/**
	 * @param hostName
	 *            The properties Virtual host name to be retrieved
	 * @return The properties of virtual host from registry
	 * @throws Exception
	 */
	public Resource getMappingFromRegistry(String hostName) throws Exception {
		String hostResourcePath = UrlMapperConstants.HostProperties.HOSTINFO + hostName;
		if (registryService.resourceExists(hostResourcePath)) {
			return registryService.get(hostResourcePath);
		}
		return null;
	}

	public String getApplicationContextForHost(String hostName) throws Exception {
		Resource resource = getMappingFromRegistry(hostName);
        String appName = null;
		if (resource != null) {
            appName = resource.getProperty(UrlMapperConstants.HostProperties.WEB_APP);
			if(appName != null) {
                return appName;
            } else {
                appName = resource.getProperty(UrlMapperConstants.HostProperties.SERVICE_EPR);
            }
		}
		return appName;

	}

	public String getServiceNameForHost(String hostName) throws Exception {
		Resource resource = getMappingFromRegistry(hostName);
		if (resource != null) {
			return resource.getProperty(UrlMapperConstants.HostProperties.SERVICE_EPR);
		}
		return null;

	}

    public String getTenantDomainForHost(String hostName) throws Exception {
        Resource resource = getMappingFromRegistry(hostName);
        if (resource != null) {
            return resource.getProperty(UrlMapperConstants.HostProperties.TENANT_DOMAIN);
        }
        return null;

    }

	public MappingData[] getAllMappingsFromRegistry() throws Exception {
		Collection mappings = getHostsFromRegistry();
		List<MappingData> mappingList = new ArrayList<MappingData>();
		if (mappings != null) {
			String[] mappingNames = mappings.getChildren();
			for (String mappingName : mappingNames) {
				mappingName = mappingName.replace(UrlMapperConstants.HostProperties.FILE_SERPERATOR
            	        + UrlMapperConstants.HostProperties.HOSTINFO , "");
				Resource resource = getMappingFromRegistry(mappingName);
				if (resource != null) {
					MappingData mappingData = new MappingData();
					mappingData.setMappingName(resource
							.getProperty(UrlMapperConstants.HostProperties.HOST_NAME));
					mappingData.setTenantDomain(resource
							.getProperty(UrlMapperConstants.HostProperties.TENANT_DOMAIN));
					if (resource.getProperty(UrlMapperConstants.HostProperties.SERVICE_EPR) != null) {
						mappingData.setServiceMapping(true);
						mappingData.setUrl(resource
								.getProperty(UrlMapperConstants.HostProperties.SERVICE_EPR));
					} else {
						mappingData.setUrl(resource
								.getProperty(UrlMapperConstants.HostProperties.WEB_APP));
					}
					mappingList.add(mappingData);
				}

			}
			return mappingList.toArray(new MappingData[mappingList.size()]);
		}
		return null;
	}
    
    public String getAppNameFromRegistry(String hostName) throws Exception{
        Resource resource = getMappingFromRegistry(hostName);
        if (resource != null) {
            return resource.getProperty(UrlMapperConstants.HostProperties.APP_NAME);
        }
        return null;
    }

	private Collection getHostsFromRegistry() throws Exception {
		if (registryService.resourceExists(UrlMapperConstants.HostProperties.HOSTINFO)) {
			return (Collection) registryService.get(UrlMapperConstants.HostProperties.HOSTINFO);
		}
		return null;
	}

}
