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

package org.wso2.carbon.transport.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.Resources;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.persistence.PersistenceFactory;
import org.wso2.carbon.core.persistence.ServicePersistenceManager;
import org.wso2.carbon.core.transports.TransportService;
import org.wso2.carbon.core.transports.util.TransportDetails;
import org.wso2.carbon.core.transports.util.TransportParameter;
import org.wso2.carbon.core.transports.util.TransportSummary;
//import org.wso2.carbon.service.mgt.ServiceAdmin;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.CarbonConstants;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * This services manages transports for all available transport bundles. This is the service behind
 * the transport management UI component.
 */
public class TransportAdmin extends AbstractAdmin {

	private static final Log log = LogFactory.getLog(TransportAdmin.class);

    public TransportAdmin() {
        super();
    }

	/**
	 * Returns all available transports. That is - all transports corresponding to the deployed
	 * transport bundles. If a given transport is not included axis2.xml - then that transport to be
	 * present here it should have the corresponding <transpport-name>-transports.xml inside
	 * [CARBON_HOME]\conf.
	 * 
	 * @return TransportSummary[]
	 */
	public TransportSummary[] listTransports() {
		Map<String, TransportService> transports;
		Collection<TransportSummary> transCollection;
		TransportStore transportStore;

		// TransportStore already being created - so we pass null.
		transportStore = TransportStore.getInstance();
		// All transport bundles update the TransportStore - with the corresponding transports
		// supported by those.
		transports = transportStore.getAvailableTransports();
		transCollection = new ArrayList<TransportSummary>();

		for (Iterator<TransportService> iter = transports.values().iterator(); iter.hasNext();) {
			TransportService transportService;
			TransportSummary summary;

			transportService = iter.next();
			// TransportSummary only needs a subset of information from TransportInfo.
			summary = new TransportSummary();
			summary.setProtocol(transportService.getName());
			// All transports already loaded in to axis2configuration are considered as active.
			// Inactive transports still available in the management UI so the user can enable
			// those.
			summary.setListenerActive(transportService.isEnabled(true, getAxisConfig()));
            summary.setSenderActive(transportService.isEnabled(false, getAxisConfig()));
            transCollection.add(summary);
        }

		return transCollection.toArray(new TransportSummary[transCollection.size()]);
	}

	/**
	 * Returns all defined transports for the given services. Only the active transports are
	 * considered here.All transports already loaded in to axis2configuration are considered as
	 * active
	 *
	 * @param serviceName Name of the service where transports are listed.
	 * @return TransportSummary[]
	 * @throws Exception on error
	 */
	public TransportSummary[] listTransportsForService(String serviceName) throws Exception {
		Map<String, TransportService> transports;
		Collection<TransportSummary> transCollection;
		TransportStore transportStore;
		boolean isUTEnabled;

		if (serviceName == null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid service name");
			}
			throw new Exception("Invalid service name");
		}

		// TransportStore already being created - so we pass null.
		transportStore = TransportStore.getInstance();
		// All transport bundles update the TransportStore - with the corresponding transports
		// supported by those.
		transports = transportStore.getAvailableTransports();
		transCollection = new ArrayList<TransportSummary>();

		// If the service is UT enabled then the only transport that can be
		// exposed is HTTPS.
		isUTEnabled = isUTEnabled(serviceName);

		for (Iterator<TransportService> iter = transports.values().iterator(); iter.hasNext();) {
			TransportService transportService;
			TransportSummary summary;

			transportService = iter.next();

			summary = new TransportSummary();
			summary.setProtocol(transportService.getName());
			// All transports already loaded in to axis2configuration are considered as active.
			// Inactive transports still available in the management UI so the user can enable
			// those.
			summary.setListenerActive(transportService.isEnabled(true, getAxisConfig()));
            summary.setSenderActive(transportService.isEnabled(false, getAxisConfig()));
            // Only active transports are considered here.
			if (summary.isListenerActive()) {
				if (isUTEnabled) {
					// If the service is UT enabled then the only transport that can be
					// exposed is HTTPS.
					if (ServerConstants.HTTPS_TRANSPORT.equalsIgnoreCase(transportService.getName())) {
						transCollection.add(summary);
					}
				} else {
					transCollection.add(summary);
				}
			}
		}

		return transCollection.toArray(new TransportSummary[transCollection.size()]);
	}

	/**
	 * Lists all the transports exposed by a given service. By default if WSDL not specifically
	 * restricts then the service will expose all the active transports.All transports already
	 * loaded in to axis2configuration are considered as active.
	 *
	 * @param serviceName Name of the service where exposed transports are listed.
	 * @return TransportSummary[]
	 * @throws Exception on error
	 */
        @Deprecated
	public TransportSummary[] listExposedTransports(String serviceName) throws Exception {
                return new TransportSummary[0];

                /*
		Collection<TransportSummary> transCollection;
		String[] exposedTransports;
		ServiceAdmin admin;
		boolean isUTEnabled;

		if (serviceName == null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid service name");
			}
			throw new Exception("Invalid service name");
		}

		// If the service is UT enabled then the only transport that can be
		// exposed is HTTPS.
		isUTEnabled = isUTEnabled(serviceName);

		try {
			admin = new ServiceAdmin(getAxisConfig());
			exposedTransports = admin.getExposedTransports(serviceName);
		} catch (Exception e) {
			throw new AxisFault("error while adding exposed transport", e);
		}

		// Now we have all the exposed transports for the given service.
		// Let's build the object model out of it.
		// exposedTransports - cannot be null or empty, should at least contain one element.

		transCollection = new ArrayList<TransportSummary>();

		for (int i = 0; i < exposedTransports.length; i++) {
			String transport = exposedTransports[i];
			TransportSummary summary;
			boolean isActive = false;

			// All transports already loaded in to axis2configuration are considered as active.
			if (getAxisConfig().getTransportIn(transport) != null) {
				isActive = true;
			}

			summary = new TransportSummary();
			summary.setProtocol(transport);
			summary.setListenerActive(isActive);
            summary.setSenderActive(getAxisConfig().getTransportOut(transport) != null);

            if (isUTEnabled) {
				// If the service is UT enabled then the only transport that can be
				// exposed is HTTPS - user wont be able to remove this transport from the UI.
				if (ServerConstants.HTTPS_TRANSPORT.equalsIgnoreCase(transport)) {
					summary.setNonRemovable(true);
				}
			}

			if (isActive) {
				transCollection.add(summary);
			}
		}

		return transCollection.toArray(new TransportSummary[transCollection.size()]);
                */
	}

	/**
	 * This is a merge function to list transport data for all available transports. That is for -
	 * all transports corresponding to the deployed transport bundles. If a given transport is not
	 * included axis2.xml - then that transport to be present here it should have the corresponding
	 * <transpport-name>-transports.xml inside [CARBON_HOME]\conf.
	 *
	 * @return TransportData[] an array of TransportData
	 * @throws Exception on error
	 */
	public TransportData[] getAllTransportData() throws Exception {
		TransportData[] transportData;
		TransportSummary[] summary;
		TransportDetails details;

		try {
			// List all available transports.
			summary = listTransports();
			transportData = new TransportData[summary.length];
			for (int i = 0; i < summary.length; i++) {
                TransportSummary transportSummary;
				TransportData data;

				transportSummary = summary[i];
				// Get transport details corresponding to the given transport.
				details = getTransportDetails(transportSummary.getProtocol());
				data = new TransportData();
				data.setDetails(details);
				data.setSummary(transportSummary);
				transportData[i] = data;
            }
		} catch (Exception e) {
			throw new AxisFault("Error while retrieving transport data", e);
		}
		return transportData;
	}

	/**
	 * Adds transport to a given service. Any active available transport can be exposed by a
	 * service. All transports already loaded in to axis2configuration are considered as active.
	 * We no longer provide UI options to add transports to services
	 * @param serviceName Name of the service where new transport to be added.
	 * @param transport Name of the transport to be added.
	 * @throws Exception on error
	 */
        @Deprecated
	public void addExposedTransports(String serviceName, String transport) throws Exception {
                /*
		ServiceAdmin admin;

		if (serviceName == null) {
			handleException("Invalid service name: Service name must not be null");
		}

		if (transport == null) {
			handleException("Invalid transport name: Transport name must not be null");
		}

		try {
			admin = new ServiceAdmin(getAxisConfig());
			admin.addTransportBinding(serviceName, transport);
		} catch (Exception e) {
            handleException("Error while adding exposed transport " + transport, e);
		}
                */
	}

	/**
	 * Removes an exposed transport from a given service.
	 *
	 * @param serviceName Name of the service where new transport to be removed.
	 * @param transportProtocol Name of the transport to be removed.
	 * @throws Exception on error
	 */
    public void removeExposedTransports(String serviceName, String transportProtocol)
            throws Exception {

        TransportSummary[] transports;
        PersistenceFactory pf = PersistenceFactory.getInstance(getAxisConfig());
        ServicePersistenceManager pm;
        AxisService axisService;

        if (serviceName == null) {
            handleException("Invalid service name");
        }

        if (transportProtocol == null) {
            handleException("Invalid transport name");
        }

        axisService = getAxisConfig().getServiceForActivation(serviceName);
        if (axisService == null) {
            handleException("No service exists by the name : " + serviceName);
        }

        try {
            if (isUTEnabled(serviceName)) {
                // If UT enabled, you can't remove HTTPS transport from this service.
                if (ServerConstants.HTTPS_TRANSPORT.equalsIgnoreCase(transportProtocol)) {
                    throw new Exception("Cannot remove HTTPS transport binding for Service ["
                            + serviceName + "] since a security scenario which requires the "
                            + "service to contain only the HTTPS transport binding"
                            + " has been applied to this service.");
                }
            }

            if (!axisService.isEnableAllTransports()) {
                if (axisService.getExposedTransports().size() == 1) {
                    log.warn("At least one transport binding must exist for a service. No bindings " +
                            "will be removed.");
                    return;
                }

                // Simply remove the transport from the list of exposed transport
                axisService.removeExposedTransport(transportProtocol);

            } else {
                // This returns all the available transports - not just active ones.
                transports = listTransports();

                // populate the exposed transports list with the other transports
                for (TransportSummary transport : transports) {
                    if (transport.isListenerActive() &&
                            !transport.getProtocol().equals(transportProtocol)) {
                        axisService.addExposedTransport(transport.getProtocol());
                    }
                }
                axisService.setEnableAllTransports(false);
            }

            pm = pf.getServicePM();
            pm.removeExposedTransports(serviceName, transportProtocol);

            getAxisConfig().notifyObservers(
                    new AxisEvent(CarbonConstants.AxisEvent.TRANSPORT_BINDING_REMOVED, axisService),
                    axisService);

        } catch (Exception e) {
            handleException("Error while removing exposed transport : " + transportProtocol, e);
        }
    }

    private void handleException(String msg) throws Exception {
        log.error(msg);
        throw new Exception(msg);
    }

    private void handleException(String msg, Throwable t) throws Exception {
        log.error(msg, t);
        throw new Exception(msg, t);
    }

	/**
	 * Returns transport details of particular transport.
	 *
	 * @param transportProtocol Name of the transport where details are required.
	 * @return TransportDetails
	 * @throws Exception on error
	 */
	public TransportDetails getTransportDetails(String transportProtocol) throws Exception {
		TransportService transportService;
		TransportDetails details;

		if (transportProtocol == null) {
			if (log.isDebugEnabled()) {
				log.debug("Invalid transport name");
			}
			throw new Exception("Invalid transport name");
		}

		// All transport bundles update the TransportStore - with the corresponding transports
		// supported by those.
		transportService = TransportStore.getInstance().getTransport(transportProtocol);
        if (transportService != null) {
            details = new TransportDetails();
            details.setListenerActive(transportService.isEnabled(true, getAxisConfig()));
            details.setSenderActive(transportService.isEnabled(false, getAxisConfig()));
            details.setInParameters(transportService.getGlobalTransportParameters(true, getAxisConfig()));
            details.setOutParameters(transportService.getGlobalTransportParameters(false, getAxisConfig()));
            return details;
        } else {
            log.warn("Transport service not available for : " + transportProtocol);
            return null;
        }
    }

    /**
     * Get the globally defined transport listener parameters
     *
     * @param transport name of the transport
     * @return an array of transport parameters or null
     * @throws Exception on error
     */
    public TransportParameter[] getGloballyDefinedInParameters(String transport) throws Exception {
        TransportService service = TransportStore.getInstance().getTransport(transport);
        if (service != null) {
            return service.getGlobalTransportParameters(true, getAxisConfig());
        }
        return null;
    }

    /**
     * Get the globally defined transport sender parameters
     *
     * @param transport name of the transport
     * @return an array of TransportParameter objects or null
     * @throws Exception on error
     */
    public TransportParameter[] getGloballyDefinedOutParameters(String transport) throws Exception {
        TransportService service = TransportStore.getInstance().getTransport(transport);
        if (service != null) {
            return service.getGlobalTransportParameters(false, getAxisConfig());
        }
        return null;
    }

    /**
     * Updates the set of transport listener parameters
     *
     * @param transport name of the transport
     * @param params an array of TransportPrameter objects
     * @throws Exception on error
     */
    public void updateGloballyDefinedInParameters(String transport,
                                                  TransportParameter[] params) throws Exception {

        TransportService service = TransportStore.getInstance().getTransport(transport);
        if (service != null) {
            service.updateGlobalTransportParameters(params, true, getConfigContext());
        } else {
            throw new Exception("Transport management service is not available for : " + transport);
        }
    }

    /**
     * Updates the set of transport sender parameters
     *
     * @param transport name of the transport
     * @param params an array of TransportParameter objects
     * @throws Exception on error
     */
    public void updateGloballyDefinedOutParameters(String transport,
                                                  TransportParameter[] params) throws Exception {

        TransportService service = TransportStore.getInstance().getTransport(transport);
        if (service != null) {
            service.updateGlobalTransportParameters(params, false, getConfigContext());
        } else {
            throw new Exception("Transport management service is not available for : " + transport);
        }
    }

    /**
     * Get the set of transport listener parameters specific to a service
     *
     * @param transport name of the transport
     * @param service name of the service
     * @return an array of TransportParameter objects or null
     * @throws Exception on error
     */
    public TransportParameter[] getServiceSpecificInParameters(String transport,
                                                               String service) throws Exception {

        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (service != null) {
            return trpService.getServiceLevelTransportParameters(service, true, getAxisConfig());
        }
        return null;
    }

    /**
     * Get the set of transport sender parameters specific to a service
     *
     * @param transport name of the transport
     * @param service name of the service
     * @return an array of TransportParameter objects or null
     * @throws Exception on error
     */
    public TransportParameter[] getServiceSpecificOutParameters(String transport,
                                                                String service) throws Exception {

        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (service != null) {
            return trpService.getServiceLevelTransportParameters(service, false, getAxisConfig());
        }
        return null;
    }


    /**
     * Updates the set of transport listener parameters specific to a service
     *
     * @param transport name of the transport
     * @param service name of the service
     * @param params an array of TransportPrameter objects
     * @throws Exception on error
     */
    public void updateServiceSpecificInParameters(String transport, String service,
                                                  TransportParameter[] params) throws Exception {


        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (trpService != null) {
            trpService.updateServiceLevelTransportParameters(service, params, true, getConfigContext());
        } else {
            throw new Exception("Transport management service is not available for : " + transport);
        }
    }

    /**
     * Updates the set of transport sender parameters specific to a service
     *
     * @param transport name of the transport
     * @param service name of the service
     * @param params an array of TransportPrameter objects
     * @throws Exception on error
     */
    public void updateServiceSpecificOutParameters(String transport, String service,
                                                  TransportParameter[] params) throws Exception {

        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (trpService != null) {
            trpService.updateServiceLevelTransportParameters(service, params, false, getConfigContext());
        } else {
            throw new Exception("Transport management service is not available for : " + transport);
        }
    }

    public void disableListener(String transport) throws Exception {
        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (trpService != null) {
            trpService.disableTransport(true, getAxisConfig());
        }
    }

    public void disableSender(String transport) throws Exception {
        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (trpService != null) {
            trpService.disableTransport(false, getAxisConfig());
        }
    }

    public boolean dependenciesAvailable(String transport, TransportParameter[] params) throws Exception {
        TransportService trpService = TransportStore.getInstance().getTransport(transport);
        if (trpService == null) {
            throw new Exception("The transport management service for " + transport + " is not" +
                    " available in the transport store");
        }

        return trpService.dependenciesAvailable(params);
    }

    /**
	 * Checks whether UT being enabled for the given service.
	 *
	 * @param serviceName Name of the service to check whether UT being enabled.
	 * @return True if enabled else false.
	 * @throws AxisFault on error
	 */
	private boolean isUTEnabled(String serviceName) throws AxisFault {
		AxisService axisService;
		OMElement serviceElement;

		axisService = getAxisConfig().getServiceForActivation(serviceName);

		try {
			ServicePersistenceManager pm = PersistenceFactory.getInstance(getAxisConfig()).getServicePM();
			serviceElement = pm.getService(axisService);

			if (serviceElement == null) {
				pm.handleNewServiceAddition(axisService);
				serviceElement = pm.getService(axisService);
			}
			if (serviceElement.getAttributeValue(new QName(Resources.ServiceProperties.IS_UT_ENABLED)) != null) {
				return true;
			}
		} catch (Exception e) {
			log.error("Error occurred while checking whether UT being enabled for service "
					+ serviceName, e);
			return false;
		}
		return false;
	}


}
