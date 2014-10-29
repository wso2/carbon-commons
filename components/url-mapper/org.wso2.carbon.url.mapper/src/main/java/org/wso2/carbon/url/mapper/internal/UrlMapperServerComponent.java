package org.wso2.carbon.url.mapper.internal;

import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * *
 * @scr.component name="org.wso2.carbon.url.mapper.UrlMapperServerComponent" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class UrlMapperServerComponent {

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setServerConfigContext(contextService.getServerConfigContext());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setServerConfigContext(null);
    }
}
