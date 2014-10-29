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
package org.wso2.carbon.url.mapper.internal.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.catalina.*;
import org.apache.catalina.core.StandardHost;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.utils.URLMappingHolder;
import org.wso2.carbon.tomcat.ext.valves.CarbonContextCreatorValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.url.mapper.clustermessage.util.VirtualHostClusterUtil;
import org.wso2.carbon.url.mapper.data.MappingData;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.registry.RegistryManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * util class which is doing add host to engine and getting resources from the
 * registry.
 */
public class HostUtil {
	private static final Log log = LogFactory.getLog(HostUtil.class);
	private static RegistryManager registryManager = new RegistryManager();
    private static String webAppPath;
    private static String urlSuffix;

	/**
	 * This method is used to retrieve list of host names for a given
	 * webapplication.
	 * 
	 * @param webAppName
	 *            the webapp name
	 * @return list of mapped hosts.
	 * @throws UrlMapperException
	 *             throws it when failing to get resource from registry
	 */
	public static List<String> getMappingsPerWebApp(String webAppName) throws UrlMapperException {
		List<String> hostNames = new ArrayList<String>();
		try {
			MappingData mappings[] = getAllMappingsFromRegistry();
			if (mappings != null) {
				for (MappingData mapping : mappings) {
					String hostName = mapping.getMappingName();
                    String appName = registryManager.getAppNameFromRegistry(hostName);
					if (appName != null && !appName.equalsIgnoreCase("service") && webAppName.equals(mapping.getUrl())) {
						hostNames.add(hostName);
                    }
				}
			}
			return hostNames;

		} catch (Exception e) {
			log.error("Failed to get url mappings for the webapp ", e);
			throw new UrlMapperException("Failed to get url mappings for the webapp " + webAppName,
					e);
		}
	}

	/**
	 * This method is used to check if the maximum no of mappings are exceeded
	 * webapplication.
	 * 
	 * @param webAppName
	 *            the webapp name
	 * @return list of mapped hosts.
	 * @throws UrlMapperException
	 *             throws it when failing to get resource from registry
	 */
	public static boolean isMappingLimitExceeded(String webAppName) throws UrlMapperException {
		int index = 0;
		if (webAppName.contains(UrlMapperConstants.SERVICE_URL_PATTERN)) {
			webAppName = getServiceEndpoint(webAppName);
		}

		try {
			MappingData mappings[] = getAllMappingsFromRegistry();
			if (mappings != null) {
				for (MappingData mapping : mappings) {
					if (webAppName.equals(mapping.getUrl())) {
						index++;
					}
				}
			}
			return (index >= MappingConfigManager.loadMappingConfiguration().getNoOfMappings());

		} catch (Exception e) {
			log.error("Failed to get url mappings for the webapp ", e);
			throw new UrlMapperException("Failed to get url mappings for the webapp " + webAppName,
					e);
		}
	}

	/**
	 * Find out whether the hostname exists already
	 * 
	 * @param mappingName
	 *            the host name to be mapped
	 * @return Whether the hostname is valid or not
	 * @throws UrlMapperException
	 *             throws when error while retrieve from registry
	 */
	public static boolean isMappingExist(String mappingName) throws UrlMapperException {
		mappingName = mappingName + MappingConfigManager.loadMappingConfiguration().getPrefix();
		MappingData mappings[] = getAllMappingsFromRegistry();
		boolean isExist = false;
		if (mappings != null) {
			for (MappingData mapping : mappings) {
				if (mappingName.equalsIgnoreCase(mapping.getMappingName())) {
					isExist = true;
				}
			}
		}
		return isExist;
	}

	public static MappingData[] getAllMappingsFromRegistry() throws UrlMapperException {
		try {
			// get all URL mapping information.
			return registryManager.getAllMappingsFromRegistry();
		} catch (Exception e) {
			log.error("Failed to get all hosts ", e);
			throw new UrlMapperException("Failed to get all url mappings from the registry ", e);
		}
	}

	public static MappingData[] getTenantSpecificMappingsFromRegistry(String tenantName)
			throws UrlMapperException {
		try {
			// get all URL mapping information.
			return registryManager.getTenantSpecificMappingsFromRegistry(tenantName);
		} catch (Exception e) {
			log.error("Failed to get all hosts ", e);
			throw new UrlMapperException("Failed to get all url mappings from the registry ", e);
		}
	}

	/**
	 * This method is used to retrieve list of host names for a given
	 * webapplication.
	 * 
	 * @param url
	 * @return list of mapped hosts.
	 * @throws UrlMapperException
	 */
	public static List<String> getMappingsPerEppr(String url) throws UrlMapperException {
		List<String> hostNames = new ArrayList<String>();
		if (isServiceURLPattern(url)) {
			url = getServiceEndpoint(url);
			try {
				MappingData mappings[] = getAllMappingsFromRegistry();
				if (mappings != null) {
					for (MappingData mapping : mappings) {
						String hostName = mapping.getMappingName();
						if (mapping.isServiceMapping() && url.equalsIgnoreCase(mapping.getUrl())) {
							hostNames.add(hostName);
						}
					}
				}
			} catch (Exception e) {
				log.error("Failed to get url mappings for the webapp " + url, e);
				throw new UrlMapperException("Failed to get url mappings for the webapp " + url, e);
			}
		}
		return hostNames;
	}

	/**
	 * retrieving host for a specific service
	 * 
	 * @param hostName
	 *            name of the host
	 * @return
	 * @throws UrlMapperException
	 */
	public static String getServiceNameForHost(String hostName) throws UrlMapperException {
		try {
			return registryManager.getServiceNameForHost(hostName);
		} catch (Exception e) {
			log.error("Failed to retrieve the servicename from the host " + hostName, e);
			throw new UrlMapperException("Failed to retrieve the servicename from the host "
					+ hostName, e);
		}
	}

	/**
	 * retrieving host for a specific service
	 * 
	 * @param hostName
	 *            name of the host
	 * @return
	 * @throws org.wso2.carbon.url.mapper.internal.exception.UrlMapperException
	 */
	public static String getApplicationContextForHost(String hostName) throws UrlMapperException {
		try {
			String appContext = registryManager.getApplicationContextForHost(hostName);
			URLMappingHolder.getInstance().putUrlMappingForApplication(hostName,
                    appContext);
            if(appContext != null && appContext.startsWith("/services/")) {
                addServiceParameter(appContext);

            }
			return appContext;
		} catch (Exception e) {
			log.error("Failed to retrieve the servicename from the host " + hostName, e);
			throw new UrlMapperException("Failed to retrieve the servicename from the host "
					+ hostName, e);
		}
	}

	public static String getHttpPort() throws UrlMapperException {
		DataHolder dataHolder = DataHolder.getInstance();
		try {
			return CarbonUtils.getBackendHttpPort(dataHolder.getServerConfigContext());
		} catch (Exception e) {
			log.error("Failed to retrieve the backend http port ", e);
			throw new UrlMapperException("Failed to retrieve the backend http port ", e);
		}

	}

    /**
	 * It is taken the webApp which is already deployed in
	 * /repository/../webapps and redeploy it within added virtual host.
	 * 
	 * @param hostName
	 *            The virtual host name
	 * @param uri
	 *            The web app to be deployed in the virtual host
	 * @throws org.wso2.carbon.url.mapper.internal.exception.UrlMapperException
	 *             When adding directory throws an Exception
	 */
	public static void addWebAppToHost(String hostName, String uri, String appType) throws UrlMapperException {
		int tenantId;
		String tenantDomain;
		String webAppsDir;
		// if the request if from tenant
		if (MultitenantUtils.getTenantDomainFromRequestURL(uri) != null) {
				tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(uri);
				TenantManager tenantManager = DataHolder.getInstance().getRealmService()
						.getTenantManager();
				try {
					tenantId = tenantManager.getTenantId(tenantDomain);
				} catch (UserStoreException e) {
					log.error("error in getting tenant id when adding host to tomcat engine", e);
					throw new UrlMapperException(
							"error in getting tenant id when adding host to tomcat engine");
				}
				// getting the web app .war file name from the uri

				// path of web app for the tenant in the server
				webAppsDir = CarbonUtils.getCarbonTenantsDirPath() + "/" + tenantId + "/";
                webAppPath = getWebappPath(webAppsDir, uri, appType);

			} else {
				tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
				webAppsDir = CarbonUtils.getCarbonRepository();
                webAppPath = getWebappPath(webAppsDir, uri, appType);

			}
		try {
            CarbonTomcatService tomcatService = DataHolder.getInstance().getCarbonTomcatService();
            String defaultHost = tomcatService.getTomcat().getEngine().getDefaultHost();
            Host host = addHostToEngine(hostName);
			// deploying the copied webapp as the root in our own host directory
            if(UrlMapperConstants.HostProperties.JAGGERY_APP.equalsIgnoreCase(appType)) {
                LifecycleListener[] listeners = tomcatService.getTomcat().getEngine().
                        findChild(defaultHost).findChild(uri).findLifecycleListeners();
                for(LifecycleListener listener : listeners) {
                    if(listener.getClass().getName().contains(UrlMapperConstants.HostProperties.JAGGERY_LISTENER)) {
                        Context jaggeryContext = tomcatService.addWebApp(host, "/", webAppPath, listener);
                        log.info("Deployed JaggeryApp on host: " + jaggeryContext);
                        break;
                    }
                }
            } else {
                Context contextForHost = tomcatService
                        .addWebApp(host, "/", webAppPath);
                if(UrlMapperConstants.HostProperties.JAX_APP.equalsIgnoreCase(appType)) {
                    log.info("Deployed JAXRS on host: " + contextForHost);
                    //registering services context in url-mapping.
                } else {
                    log.info("Deployed webapp on host: " + contextForHost);
                }
            }

			// add entry to registry with the tenant domain if exist in the uri
			// if adding virtual host is successful.
			registryManager.addHostToRegistry(hostName, uri, tenantDomain, appType);
			URLMappingHolder.getInstance().putUrlMappingForApplication(hostName,
					uri);
            //adding host to cluster message
            VirtualHostClusterUtil.addVirtualHostsToCluster(hostName, uri, webAppPath);

		} catch (Exception e) {
			log.error("error in adding the virtual host to tomcat engine", e);
			throw new UrlMapperException("error in adding the virtual host to tomcat engine");
		}
	}

	public static String getWebappPath(String webappsDir, String uri, String appType) {
        String webAppFile = getContextFromUri(uri) + UrlMapperConstants.HostProperties.WAR;

		String appPath = null;
        
		if(UrlMapperConstants.HostProperties.WEBAPP.equalsIgnoreCase(appType)) {
            appPath = webappsDir + UrlMapperConstants.HostProperties.WEB_APPS + "/" + webAppFile;
        } else if(UrlMapperConstants.HostProperties.JAGGERY_APP.equalsIgnoreCase(appType)) {
            appPath = webappsDir + UrlMapperConstants.HostProperties.JAGGERY_APPS + "/" + webAppFile;
        } else if(UrlMapperConstants.HostProperties.JAX_APP.equalsIgnoreCase(appType)) {
            appPath = webappsDir + UrlMapperConstants.HostProperties.JAX_WEBAPPS + "/" + webAppFile;
        }
        File warFile = new File(appPath);

		if (warFile.exists()) {
            return appPath;
		} else {
            appPath = appPath.substring(0, appPath.indexOf(".war"));
            File dir = new File(appPath);
            if(dir.isDirectory()) {
            return appPath;
            }
	    }
        return null;
    }

	/**
	 * add host to engine.
	 * 
	 * @param hostName
	 *            name of the host
	 * @return will return the added host of Engine
	 */
	public static Host addHostToEngine(String hostName) {
		String hostBaseDir = CarbonUtils.getCarbonRepository() + "/"
				+ UrlMapperConstants.HostProperties.WEB_APPS + "/";
		CarbonTomcatService carbonTomcatService = DataHolder.getInstance().getCarbonTomcatService();
		// adding virtual host to tomcat engine
		Engine engine = carbonTomcatService.getTomcat().getEngine();
		StandardHost host = new StandardHost();
		host.setAppBase(hostBaseDir);
		host.setName(hostName);
		host.setUnpackWARs(false);
		host.addValve(new CarbonContextCreatorValve());
		host.addValve(new CompositeValve());
		engine.addChild(host);
		log.info("host added to the tomcat: " + host);
		return host;
	}

	/**
	 * edit the host from CATALINA-HOME directory
	 * 
	 * @param hostName
	 */
	public static void deleteHostDirectory(String hostName) {
		String filePath = CarbonUtils.getCarbonCatalinaHome() + "/" + hostName;
		File file = new File(filePath);
		if (file.isDirectory()) {
			// make sure tomcat engine has removed folder structure inside the
			// host folder
			if (file.list().length == 0) {
				file.delete();
			}
		}
	}

	/**
	 * delete the existing host with the given name
	 * 
	 * @param webAppName
	 *            the associated webapp name of the host to be edited
	 * @param oldHost
	 *            the existing hostname to be edited
	 * @param newHost
	 *            the hostname given
	 * @throws UrlMapperException
	 *             throws when error while removing oldHost or adding newHost
	 */
	public static void editHostInEngine(String webAppName, String newHost, String oldHost)
			throws UrlMapperException {
        try {
            String appType = registryManager.getAppNameFromRegistry(oldHost);
            removeHostForCluster(oldHost);
            deleteResourceToRegistry(oldHost);
            addWebAppToHost(newHost, webAppName, appType);
        } catch (Exception e) {
            log.error("error while getting app name from registry", e);
        }

    }

	/**
	 * remove the host from the engine and registry
	 * 
	 * @param hostName
	 *            name of the host to be removed
	 * @throws UrlMapperException
	 *             throws when error while removing context or host from engine
	 *             and from registry
	 */
	public static void removeHost(String hostName) throws UrlMapperException {
		Container[] hosts = DataHolder.getInstance().getCarbonTomcatService().getTomcat()
				.getEngine().findChildren();
		CarbonTomcatService carbonTomcatService = DataHolder.getInstance().getCarbonTomcatService();
		Engine engine = carbonTomcatService.getTomcat().getEngine();
		for (Container host : hosts) {
			if (host.getName().contains(hostName)) {
				try {
					Context context = (Context) host.findChild("/");
					if (host.getState().isAvailable()) {
						if (context != null && context.getAvailable()) {
							context.setRealm(null);
							context.stop();
							context.destroy();
							log.info("Unloaded webapp from the host: " + host
									+ " as the context of: " + context);
						}
						host.removeChild(context);
						host.setRealm(null);
						host.stop();
						host.destroy();
						engine.removeChild(host);
                        URLMappingHolder.getInstance().removeUrlMappingMap(hostName);
                        log.info("Unloaded host from the engine: " + host);
						break;
					}
				} catch (LifecycleException e) {
					throw new UrlMapperException("Error when removing host from tomcat engine."
							+ host, e);
				}

			}
		}
		// host name should be deleted explicitly because when host is deleted
		// from tomcat engine the folder with the host name will not get
		// removed.
		deleteHostDirectory(hostName);
	}

    public static void removeHostForCluster(String hostName) throws UrlMapperException {
        removeHost(hostName);
        //removing host in other clusters
        try {
            VirtualHostClusterUtil.deleteVirtualHostsToCluster(hostName);
        } catch (AxisFault axisFault) {
            log.error("error while deleting host from cluster", axisFault);
        }
    }

	/**
	 * edit the service mapping when an actual service got deleted.
	 * 
	 * @param epr
	 *            service endpoint of a service
	 */
	public static void deleteServiceMapping(String epr) {
		List<String> urlMappins;
		try {
			urlMappins = getMappingsPerEppr(epr);
			for (String urlmapping : urlMappins) {
				URLMappingHolder.getInstance().removeUrlMappingMap(urlmapping);
				HostUtil.deleteResourceToRegistry(urlmapping);
			}
		} catch (UrlMapperException e) {
			log.error("error while getting url mapping for service", e);
		}
	}

	/**
	 * remove the service mapping when an actual service got deleted.
	 * 
	 * @param epr
	 *            service endpoint of a service
	 */
	public static void removeServiceMapping(String epr) {
		List<String> urlMappins;
		try {
			urlMappins = getMappingsPerEppr(epr);
			for (String urlmapping : urlMappins) {
				URLMappingHolder.getInstance().removeUrlMappingMap(urlmapping);
			}
		} catch (UrlMapperException e) {
			log.error("error while getting url mapping for service", e);
		}
	}

	/**
	 * add the service mapping when an actual service got deleted.
	 * 
	 * @param epr
	 *            seice endpoint of a service
	 */
	public static void addServiceMapping(String epr) {
		List<String> urlMappins;
		try {
			urlMappins = getMappingsPerEppr(epr);
			for (String urlmapping : urlMappins) {
				URLMappingHolder.getInstance().putUrlMappingForApplication(
						urlmapping, getServiceEndpoint(epr));
			}
		} catch (UrlMapperException e) {
			log.error("error while getting url mapping for service", e);
		}
	}

	/**
	 * adding domain for service in registry
	 * 
	 * @param hostName
	 * @param url
	 * @throws UrlMapperException
	 */
	public static void addDomainToServiceEpr(String hostName, String url, String appType) throws UrlMapperException {

		// if the request if from tenant
		String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
		if (url.contains("/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/")) {
			tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(url);
		}

		if (isServiceURLPattern(url)) {
			url = getServiceEndpoint(url);
		}
		try {
			// add entry to registry with the tenant domain if exist in the uri
			registryManager.addEprToRegistry(hostName, url, tenantDomain, appType);
			URLMappingHolder.getInstance().putUrlMappingForApplication(hostName,
					url);
            log.info("mapping added to service:***********: " + hostName + "******: " + url );
            //adding mapping to cluster message
            VirtualHostClusterUtil.addServiceMappingToCluster(hostName, url);
            addServiceParameter(url);
		} catch (Exception e) {
			log.error("error in adding the domain to the resitry", e);
			throw new UrlMapperException("error in adding the domain to the resitry");
		}
	}

    public static void addServiceParameter(String url)  {
        AxisService axisService;
        if(url.contains("/t/")) {
            ConfigurationContext configurationContext = TenantAxisUtils.
                    getTenantConfigurationContext(MultitenantUtils.getTenantDomainFromUrl(url),
                            DataHolder.getInstance().getServerConfigContext());
            try {
                axisService = configurationContext.getAxisConfiguration().getService(getServiceName(url).
                        substring(0, getServiceName(url).indexOf("/") ));
                axisService.addParameter("custom-mapping", "true");
            } catch (AxisFault axisFault) {
                log.error("error while getting Axis2 service instance" , axisFault);
            }

        } else {
            try {
                    axisService = DataHolder.getInstance().getServerConfigContext().
                        getAxisConfiguration().getService(getServiceName(url).
                        substring(0, getServiceName(url).indexOf("/")));
                axisService.addParameter("custom-mapping", "true");
            } catch (AxisFault axisFault) {
                log.error("error while getting Axis2 service instance" , axisFault);
            }

        }
    }


	/**
	 * update endpoint in the registry for host
	 * 
	 * @param newHost
	 *            new host to be updated
	 * @param oldHost
	 *            existing old host
	 * @throws UrlMapperException
	 */
	public static void updateEprToRegistry(String newHost, String oldHost)
			throws UrlMapperException {
		try {
			String epr = getServiceNameForHost(oldHost);
            String appType = registryManager.getAppNameFromRegistry(oldHost);
			deleteResourceToRegistry(oldHost);
            URLMappingHolder.getInstance().removeUrlMappingMap(oldHost);
            VirtualHostClusterUtil.deleteServiceMappingToCluster(oldHost);
			addDomainToServiceEpr(newHost, epr, appType);
		} catch (Exception e) {
			log.error("error in updating the domain to the resitry", e);
			throw new UrlMapperException("error in updating the domain to the resitry");
		}
	}

	/**
	 * deleting resource in registry when deleting host
	 * 
	 * @param host
	 *            hostName
	 * @throws UrlMapperException
	 */
	public static void deleteResourceToRegistry(String host) throws UrlMapperException {
		try {
			registryManager.removeFromRegistry(host);
            //VirtualHostClusterUtil.deleteServiceMappingToCluster(host);
		} catch (Exception e) {
			log.error("error in removing the domain to the resitry", e);
			throw new UrlMapperException("error in updating the domain to the resitry");
		}
	}

	/**
	 * getting context of the webapp from the uri
	 * 
	 * @param uri
	 *            uri of the actual webapp url
	 * @return returns the context
	 */
	public static String getContextFromUri(String uri) {
		// context path is /t/tenantdomain/webapps/webapp-context, then the
		// context is webapp-context
		String[] temp = uri.split(UrlMapperConstants.HostProperties.FILE_SERPERATOR);
		return temp[temp.length - 1];
	}

	/**
	 * getting service endpoint from url
	 * 
	 * @param url
	 *            url of the service
	 * @return
	 * @throws UrlMapperException
	 */
	public static String getServiceEndpoint(String url) throws UrlMapperException {
		String uri = url.substring(url.indexOf(UrlMapperConstants.SERVICE_URL_PATTERN),
				url.length());
		return uri;
	}

	/**
	 * checking for the pattern of the url for service
	 * 
	 * @param url
	 *            url of the service invoked
	 * @return boolean
	 */
	public static boolean isServiceURLPattern(String url) {
		Pattern pattern = Pattern.compile(UrlMapperConstants.SERVICE_URL_PATTERN);
		Matcher matcher;
		matcher = pattern.matcher(url);
		return matcher.find();
	}

	public static String getServiceName(String serviceContext) {
		String serviceName = null;
		if (serviceContext.contains("/t/")) {
			String temp;
			temp = serviceContext.substring(serviceContext.indexOf("/t/") + 3,
					serviceContext.length());
			serviceName = temp.substring(temp.indexOf("/") + 1, temp.length());
		} else {
			serviceName = serviceContext.substring(serviceContext.indexOf("/services") + 10,
					serviceContext.length());
		}
		return serviceName;
	}
    
    public static void addServiceUrlMapping(int tenantId, String serviceName) {
        List<String> urlMappings = null;
        String serviceContext;
        try {
            if(tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                serviceContext = UrlMapperConstants.SERVICE_URL_PATTERN + "/" + serviceName;
                urlMappings = getMappingsPerEppr(serviceContext);
                for(String urlMapping : urlMappings) {
                    URLMappingHolder.getInstance().putUrlMappingForApplication
                            (urlMapping, serviceContext);
                }
            } else {
                String tenantDomain = getTenantDomainFromTID(tenantId);
                serviceContext = UrlMapperConstants.SERVICE_URL_PATTERN + "/t/" +
                                        tenantDomain + "/" + serviceName;
                urlMappings = getMappingsPerEppr(serviceContext);
                for(String urlMapping : urlMappings) {
                    URLMappingHolder.getInstance().putUrlMappingForApplication
                            (urlMapping, serviceContext);
                }
            }

        } catch (UrlMapperException e) {
            log.error("error while getting resource from registry", e);
        }

    }
    
    public static void removeUrlMappingFromMap(int tenantId, String serviceName) {
        if(tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            List<String> urlMappings = URLMappingHolder.getInstance().
                    getUrlMappingsPerApplication(UrlMapperConstants.SERVICE_URL_PATTERN
                            + "/" + serviceName);
            for(String urlMapping : urlMappings) {
                URLMappingHolder.getInstance().
                        removeUrlMappingMap(urlMapping);
                try {
                    VirtualHostClusterUtil.deleteServiceMappingToCluster(urlMapping);
                } catch (AxisFault axisFault) {
                    log.error("error while deleting service mapping from cluster", axisFault);
                }
            }
        } else {
            String tenantDomain = getTenantDomainFromTID(tenantId);
            List<String> urlMappings = URLMappingHolder.getInstance().
                    getUrlMappingsPerApplication(UrlMapperConstants.SERVICE_URL_PATTERN
                            + "/t/" + tenantDomain + "/" + serviceName);
            for(String urlMapping : urlMappings) {
                URLMappingHolder.getInstance().
                        removeUrlMappingMap(urlMapping);
                try {
                    VirtualHostClusterUtil.deleteServiceMappingToCluster(urlMapping);
                } catch (AxisFault axisFault) {
                    log.error("error while deleting service mapping from cluster", axisFault);
                }
            }
        }
        
    }
    
    public static String getTenantDomainFromTID(int tenantId) {
        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
        try {
            tenantDomain = tenantManager.getTenant(tenantId).getDomain();
        } catch (UserStoreException e) {
            log.error("error while getting tenant" ,e);
        }
        return tenantDomain;
    }
    
    public static String getUrlSuffix() {
        return urlSuffix;
    }

    public static void setUrlSuffix(String urlSuffix) {
        HostUtil.urlSuffix = urlSuffix;
    }
}