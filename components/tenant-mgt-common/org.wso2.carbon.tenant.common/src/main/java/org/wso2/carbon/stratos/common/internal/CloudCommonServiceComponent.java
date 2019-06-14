package org.wso2.carbon.stratos.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.stratos.common.packages.PackageInfoHolder;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

@Component(
        name = "stratos.common",
        immediate = true)
public class CloudCommonServiceComponent {

    private static Log log = LogFactory.getLog(CloudCommonServiceComponent.class);

    private static BundleContext bundleContext;

    private static RealmService realmService;

    private static RegistryService registryService;

    private static PackageInfoHolder packageInfos;

    @Activate
    protected void activate(ComponentContext context) {

        try {
            bundleContext = context.getBundleContext();
            if (CommonUtil.getStratosConfig() == null) {
                StratosConfiguration stratosConfig = CommonUtil.loadStratosConfiguration();
                CommonUtil.setStratosConfig(stratosConfig);
            }
            // Loading the EULA
            if (CommonUtil.getEula() == null) {
                String eula = CommonUtil.loadTermsOfUsage();
                CommonUtil.setEula(eula);
            }
            packageInfos = new PackageInfoHolder();
            context.getBundleContext().registerService(PackageInfoHolder.class.getName(), packageInfos, null);
            // Register manager configuration OSGI service
            try {
                StratosConfiguration stratosConfiguration = CommonUtil.loadStratosConfiguration();
                bundleContext.registerService(StratosConfiguration.class.getName(), stratosConfiguration, null);
                if (log.isDebugEnabled()) {
                    log.debug("******* Cloud Common Service bundle is activated ******* ");
                }
            } catch (Exception ex) {
                String msg = "An error occurred while initializing Cloud Common Service as an OSGi Service";
                log.error(msg, ex);
            }
        } catch (Throwable e) {
            log.error("Error in activating Cloud Common Service Component" + e.toString());
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        log.debug("******* Tenant Core bundle is deactivated ******* ");
    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        CloudCommonServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {

        setRegistryService(null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        CloudCommonServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {

        setRealmService(null);
    }

    public static BundleContext getBundleContext() {

        return bundleContext;
    }

    public static RegistryService getRegistryService() {

        return registryService;
    }

    public static RealmService getRealmService() {

        return realmService;
    }

    public static TenantManager getTenantManager() {

        return realmService.getTenantManager();
    }

    public static UserRegistry getGovernanceSystemRegistry(int tenantId) throws RegistryException {

        return registryService.getGovernanceSystemRegistry(tenantId);
    }

    public static UserRegistry getConfigSystemRegistry(int tenantId) throws RegistryException {

        return registryService.getConfigSystemRegistry(tenantId);
    }

    public static PackageInfoHolder getPackageInfos() {

        return packageInfos;
    }
}
