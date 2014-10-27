package org.wso2.carbon.url.mapper.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * The axis2 observer to observe the creating & terminating tenant configuration to delete service mapping of tenant
 * @scr.component name="org.wso2.carbon.url.mapper.deployment.UrlMappingDeploymentInterceptor"
 * immediate="true"
 *
 */

public class UrlMappingDeploymentInterceptor implements AxisObserver {
    private static final Log log = LogFactory.getLog(UrlMappingDeploymentInterceptor.class);

    protected void activate(ComponentContext ctxt) {
        BundleContext bundleCtx = ctxt.getBundleContext();

        // Publish the OSGi service
        Dictionary props = new Hashtable();
        props.put(CarbonConstants.AXIS2_CONFIG_SERVICE, AxisObserver.class.getName());
        bundleCtx.registerService(AxisObserver.class.getName(), this, props);

        PreAxisConfigurationPopulationObserver preAxisConfigObserver =
                new PreAxisConfigurationPopulationObserver() {
                    public void createdAxisConfiguration(AxisConfiguration axisConfiguration) {
                        init(axisConfiguration);
                        axisConfiguration.addObservers(UrlMappingDeploymentInterceptor.this);
                    }
                };
        bundleCtx.registerService(PreAxisConfigurationPopulationObserver.class.getName(),
                preAxisConfigObserver, null);

        // Publish an OSGi service to listen tenant configuration context creation events
        Dictionary properties = new Hashtable();
        properties.put(CarbonConstants.AXIS2_CONFIG_SERVICE,
                Axis2ConfigurationContextObserver.class.getName());
        bundleCtx.registerService(Axis2ConfigurationContextObserver.class.getName(),
                    new UrlMappingServiceListener(), properties);
    }

    public void init(AxisConfiguration axisConfiguration) {
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        Parameter mapping = axisService.getParameter("custom-mapping");
        int tenantId = PrivilegedCarbonContext.getCurrentContext(
                axisService.getAxisConfiguration()).getTenantId();
        if(mapping == null) {
            return;
        } else {
            if (((String)mapping.getValue()).equalsIgnoreCase("true")) {
                if(axisEvent.getEventType() == 1 || axisEvent.getEventType() == 3) {
                    if(tenantId != MultitenantConstants.SUPER_TENANT_ID) {
                        HostUtil.addServiceUrlMapping(tenantId, axisService.getName());
                    }
                } else if(axisEvent.getEventType() == 0 || axisEvent.getEventType() == 2) {
                    tenantId = PrivilegedCarbonContext.
                            getCurrentContext(axisService.getAxisConfiguration()).getTenantId();
                    HostUtil.removeUrlMappingFromMap(tenantId, axisService.getName());
                }
            }
        }
    }


    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {

    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
    }

    public void addParameter(Parameter parameter) throws AxisFault {
    }

    public void removeParameter(Parameter parameter) throws AxisFault {
    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {
    }

    public Parameter getParameter(String s) {
        return null;
    }

    public ArrayList<Parameter> getParameters() {
        return null;
    }

    public boolean isParameterLocked(String s) {
        return false;
    }
}
