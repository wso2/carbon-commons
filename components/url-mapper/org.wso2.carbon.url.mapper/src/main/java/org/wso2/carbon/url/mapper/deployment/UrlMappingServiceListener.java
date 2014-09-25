package org.wso2.carbon.url.mapper.deployment;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * The axis2 listener to trigger the observer to observe the creating & terminating tenant configuration to
 * delete service mapping of tenant
 */
public class UrlMappingServiceListener extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(UrlMappingServiceListener.class);


    public void creatingConfigurationContext(org.apache.axis2.context.ConfigurationContext configCtx) {
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        //Register UrlMappingDeploymentInterceptor as an AxisObserver in tenant's AxisConfig.
        UrlMappingDeploymentInterceptor secDeployInterceptor = new UrlMappingDeploymentInterceptor();
        secDeployInterceptor.init(axisConfig);
        axisConfig.addObservers(secDeployInterceptor);
    }

    public void terminatingConfigurationContext(org.apache.axis2.context.ConfigurationContext configCtx) {
        HashMap<String, AxisService> serviceList = configCtx.getAxisConfiguration().getServices();
        for(Map.Entry<String, AxisService> entry : serviceList.entrySet()) {
            Parameter mapping = entry.getValue().getParameter("custom-mapping");
            int tenantId;
            if(mapping == null) {
                return;
            } else {
                if (((String)mapping.getValue()).equalsIgnoreCase("true")) {
                    tenantId = PrivilegedCarbonContext.
                            getCurrentContext(configCtx).getTenantId();
                    HostUtil.removeUrlMappingFromMap(tenantId, entry.getValue().getName());
                    log.info("removing service mapping" + entry.getValue().getName() );

                }

            }
        }
    }
}
