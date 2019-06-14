package org.wso2.carbon.url.mapper.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * *
 */
@Component(
         name = "org.wso2.carbon.url.mapper.UrlMapperServerComponent",
         immediate = true)
public class UrlMapperServerComponent {

    @Activate
    protected void activate(ComponentContext ctxt) {

    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

    }

    @Reference(
             name = "config.context.service",
             service = org.wso2.carbon.utils.ConfigurationContextService.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setServerConfigContext(null);
    }
}
