/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.transport.jms;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.core.transports.util.TransportParameter;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * Manages JMS transport.
 */
public class JMSTransportAdmin extends AbstractAdmin {

    private final static String CONNECTION_FACTORY = "transport.jms.ConnectionFactory";
	private final static String CONNECTION_FACTORY_DEFAULT = "default";

    public static final String TRANSPORT_NAME = "jms";
	public static final String TRANSPORT_CONF = "jms-transports.xml";

    private static final Log log = LogFactory.getLog(JMSTransportAdmin.class);

    public JMSTransportAdmin() {
        axisConfig = getAxisConfig();
    }

    /**
	 * This method returns service specific JMS transport related parameters for a given service.
	 * First we check whether the given service has defined it's own connection factory in it's
	 * services.xml. If so - we return the corresponding parameters from it. Otherwise we return
	 * globally defined parameter set for 'default' connection factory.
	 * 
	 * @param serviceName Service name corresponding to the parameters are required.
	 * @return TransportParameter[] 
	 * @throws Exception on error
	 */
	public TransportParameter[] getServiceSpecificInParameters(String serviceName) throws Exception {
		TransportParameter[] params;
		AxisService service;
		String factoryName;
		TransportParameter[] tempParams;

        TransportService trpService = JMSServiceHolder.getInstance().getService();
		params = trpService.getServiceLevelTransportParameters(serviceName, true, getAxisConfig());
        service = axisConfig.getServiceForActivation(serviceName);

        if (service.getParameter(CONNECTION_FACTORY) == null) {
			// services.xml defines NO connection factory - let's settle with
			// the default one.
			factoryName = CONNECTION_FACTORY_DEFAULT;
		} else {
			factoryName = service.getParameter(CONNECTION_FACTORY).getValue().toString();
		}

		// Now we need to filter out the service specific connection factory.
		// We are not returning all the connection factories defined for JMS.
		for (TransportParameter transportParameter : params) {
			if (factoryName.equals(transportParameter.getName())) {
				tempParams = new TransportParameter[1];
				tempParams[0] = transportParameter;
				return tempParams;
			}
		}

		return params;
	}

     /**
	 * This method returns service specific JMS transport related parameters for a given service.
	 * First we check whether the given service has defined it's own connection factory in it's
	 * services.xml. If so - we return the corresponding parameters from it. Otherwise we return
	 * globally defined parameter set for 'default' connection factory.
	 *
	 * @param serviceName Service name corresponding to the parameters are required.
	 * @return TransportParameter[]
	 * @throws Exception on error
	 */
	public TransportParameter[] getServiceSpecificOutParameters(String serviceName) throws Exception {
		TransportService trpService = JMSServiceHolder.getInstance().getService();
        return trpService.getServiceLevelTransportParameters(serviceName, false, getAxisConfig());
	}

    /**
	 * Updates globally defined JMS transport related parameters.
	 * 
	 * @param inParams TransportParameter
	 * @throws Exception on error
	 */
	public void updateGloballyDefinedInParameters(TransportParameter[] inParams) throws Exception {
		TransportService trpService = JMSServiceHolder.getInstance().getService();
        if (trpService.dependenciesAvailable(inParams)) {
            trpService.updateGlobalTransportParameters(inParams, true, getConfigContext());
        } else {
            log.warn("Initial factory class cannot be found");
        }
    }

    public void updateGloballyDefinedOutParameters(TransportParameter[] outParams) throws Exception {
        TransportService trpService = JMSServiceHolder.getInstance().getService();
        if (outParams == null || trpService.dependenciesAvailable(outParams)) {
            trpService.updateGlobalTransportParameters(outParams, false, getConfigContext());
        } else {
            log.warn("Initial factory class cannot be found");
        }
    }

    /**
	 * Disables JMS transport listener globally. This will simply remove the transport from the
	 * AxisConfiguration and will update the registry.
	 * 
	 * @throws Exception on error
	 */
	public void disableTransportListener() throws Exception {
		TransportService trpService = JMSServiceHolder.getInstance().getService();
        trpService.disableTransport(true, getAxisConfig());
	}

    public void disableTransportSender() throws Exception {
        TransportService trpService = JMSServiceHolder.getInstance().getService();
        trpService.disableTransport(false, getAxisConfig());
    }



	/**
	 * Updates service specific JMS transport listener parameters.
	 * 
	 * @param serviceName Service name corresponding to the parameters requested.
	 * @param inParams Updated set of transport parameters
	 * @throws Exception on error
	 */
	public void updateServiceSpecificInParameters(String serviceName,
                                                  TransportParameter[] inParams) throws Exception {

        TransportService trpService = JMSServiceHolder.getInstance().getService();
        if (trpService.dependenciesAvailable(inParams)) {
            trpService.updateServiceLevelTransportParameters(serviceName, inParams,
                    true, getConfigContext());
        } else {
            log.warn("Initial factory class cannot be found");
        }
    }

    /**
	 * Updates service specific JMS transport sender parameters.
	 *
	 * @param serviceName Service name corresponding to the parameters requested.
	 * @param outParams Updated set of transport parameters
	 * @throws Exception on error
	 */
    public void updateServiceSpecificOutParameters(String serviceName,
                                                   TransportParameter[] outParams) throws Exception {

        TransportService trpService = JMSServiceHolder.getInstance().getService();
        if (outParams == null || trpService.dependenciesAvailable(outParams)) {
            trpService.updateServiceLevelTransportParameters(serviceName, outParams,
                    false, getConfigContext());
        } else {
            log.warn("Initial factory class cannot be found");
        }
    }

    public TransportParameter[] getGloballyDefinedInParameters() throws Exception {
        TransportService trpService = JMSServiceHolder.getInstance().getService();
        return trpService.getGlobalTransportParameters(true, getAxisConfig());
    }

    public TransportParameter[] getGloballyDefinedOutParameters() throws Exception {
        TransportService trpService = JMSServiceHolder.getInstance().getService();
        return trpService.getGlobalTransportParameters(false, getAxisConfig());
    }

    /**
     * Add a new JMS connection factory
     *
     * @param parameter transport parameter describing the connection factory
     * @param service name of the service or null if the factory is engaged globally
     * @param listener true for JMS listener and false for JMS sender
     * @throws Exception on error
     */
    public void addConnectionFactory(TransportParameter parameter, String service,
                                       boolean listener) throws Exception {
        TransportService trpService = JMSServiceHolder.getInstance().getService();
        trpService.addTransportParameter(parameter, listener, getConfigContext());
    }

    /**
     * Removes an existing JMS connection factory
     *
     * @param factoryName name of the factory
     * @param service name of the service or null
     * @param listener true for JMS listener and false for JMS sender
     * @throws Exception on error
     */
    public void removeConnectionFactory(String factoryName, String service,
                                        boolean listener) throws Exception {
        TransportService trpService = JMSServiceHolder.getInstance().getService();
        trpService.removeTransportParameter(factoryName, listener, getConfigContext());
    }

}