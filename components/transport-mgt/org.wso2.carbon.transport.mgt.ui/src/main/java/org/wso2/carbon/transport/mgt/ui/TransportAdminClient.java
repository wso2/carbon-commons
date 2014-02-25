/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.transport.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.transport.mgt.stub.TransportAdminStub;
import org.wso2.carbon.transport.mgt.stub.types.carbon.TransportData;
import org.wso2.carbon.transport.mgt.stub.types.carbon.TransportDetails;
import org.wso2.carbon.transport.mgt.stub.types.carbon.TransportParameter;
import org.wso2.carbon.transport.mgt.stub.types.carbon.TransportSummary;

import java.lang.Exception;

public class TransportAdminClient {

	private static final Log log = LogFactory.getLog(TransportAdminClient.class);
	private TransportAdminStub stub;

	/**
	 * Instantiates TransportAdminClient
	 * 
	 * @param cookie For session management
	 * @param backendServerURL URL of the back end server where TransportAdmin is running.
	 * @param configCtx ConfigurationContext
	 * @throws AxisFault on error
	 */
	public TransportAdminClient(String cookie, String backendServerURL,
			ConfigurationContext configCtx) throws AxisFault {
		String serviceURL = backendServerURL + "TransportAdmin";
		stub = new TransportAdminStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	/**
	 * Returns all available transports.
	 * 
	 * @return TransportSummary[]
	 * @throws AxisFault on error
	 */
	public TransportSummary[] listTransports() throws AxisFault {
		TransportSummary[] summary = new TransportSummary[0];

		try {
			summary = stub.listTransports();
		} catch (Exception e) {
			handleException("Error while retriving transport summary", e);
		}

		return summary;
	}

	/**
	 * Returns all defined transports for the given services.
	 * 
	 * @param serviceName Name of the service where transports are listed.
	 * @return TransportSummary[]
	 * @throws AxisFault on error
	 */
	public TransportSummary[] listTransports(String serviceName) throws AxisFault {
		TransportSummary[] summary = new TransportSummary[0];

		try {
			summary = stub.listTransportsForService(serviceName);
		} catch (Exception e) {
			handleException("Error while retriving transport summary", e);
		}

		return summary;
	}

	/**
	 * Lists all the transports exposed by a given service.
	 * 
	 * @param serviceName Name of the service where exposed transports are listed.
	 * @return TransportSummary[]
	 * @throws AxisFault on error
	 */
	public TransportSummary[] listExposedTransports(String serviceName) throws AxisFault {
		TransportSummary[] summary = new TransportSummary[0];

		try {
			summary = stub.listExposedTransports(serviceName);
            if (summary == null || summary.length == 0 || summary[0] == null) {
                return null;
            }
		} catch (Exception e) {
			handleException("Error while retreiving exposed transports for service " + serviceName,
					e);
		}

		return summary;
	}

	/**
	 * Adds transport to a given service.
	 * 
	 * @param serviceName Name of the service where new transport to be added.
     * @param transport Name of the transport over which the service to be exposed
	 * @throws AxisFault on error
	 */
	public void addExposedTransports(String serviceName, String transport) throws AxisFault {
		try {
			stub.addExposedTransports(serviceName, transport);
		} catch (Exception e) {
			handleException("Error while adding exposed transports for service " + serviceName, e);
		}
	}

	/**
	 * Removes an exposed transport from a given service.
	 * 
	 * @param serviceName Name of the service where new transport to be removed.
     * @param transport Name of the transport
	 * @throws AxisFault on error
	 */
	public void removeExposedTransports(String serviceName, String transport) throws AxisFault {
		try {
			stub.removeExposedTransports(serviceName, transport);
		} catch (Exception e) {
			handleException("Error while removing exposed transports for service " + serviceName, e);
		}
	}

	/**
	 * This is a merge function to list transport data for all available transports.
	 * 
	 * @return TransportData[]
	 * @throws AxisFault on error
	 */
	public TransportData[] getAllTransportData() throws AxisFault {
		TransportData[] data = new TransportData[0];

		try {
			data = stub.getAllTransportData();
		} catch (Exception e) {
			handleException("Error while retriving transport data ", e);

		}
		return data;
	}

	/**
	 * Returns transport details of a particular transport.
	 * 
	 * @param transportProtocol Name of the transport where details are required.
	 * @return TransportDetails
	 * @throws AxisFault on error
	 */
	public TransportDetails getTransportDetails(String transportProtocol) throws AxisFault {
		TransportDetails details = null;

		try {
			details = stub.getTransportDetails(transportProtocol);
		} catch (Exception e) {
			handleException("Error while retriving transport details for " + transportProtocol, e);
		}

		return details;
	}

    /**
     * Get the globally defined parameters for the specified transport listener
     *
     * @param transport name of the transport
     * @return an array of TransportParameter objects or null
     * @throws AxisFault on error
     */
    public TransportParameter[] getGloballyDefinedInParameters(String transport) throws AxisFault {
        TransportParameter[] parameters = new  TransportParameter[0];
        try {
             parameters = stub.getGloballyDefinedInParameters(transport);
        } catch (Exception e) {
            handleException("Error while retreiving transport listener parameters for " + transport, e);
        }

        if (parameters == null || parameters.length == 0 || parameters[0] == null) {
            return null;
        }
        return parameters;
    }

    /**
     * Get the globally defined parameters for the specified transport sender
     *
     * @param transport name of the transport
     * @return an array of TransportParameter objects or null
     * @throws AxisFault on error
     */
    public TransportParameter[] getGloballyDefinedOutParameters(String transport) throws AxisFault {
        TransportParameter[] parameters = new  TransportParameter[0];
        try {
             parameters = stub.getGloballyDefinedOutParameters(transport);
        } catch (Exception e) {
            handleException("Error while retreiving transport sender parameters for " + transport, e);
        }

        if (parameters == null || parameters.length == 0 || parameters[0] == null) {
            return null;
        }
        return parameters;
    }

    /**
     * Get the service specific parameters for the specified transport listener and service
     *
     * @param transport name of the transport
     * @param service name of the service
     * @return an array of TransportParameter objects or null
     * @throws AxisFault on error
     */
    public TransportParameter[] getServiceSpecificInParameters(String transport,
                                                               String service) throws AxisFault {
        TransportParameter[] parameters = new  TransportParameter[0];
        try {
             parameters = stub.getServiceSpecificInParameters(transport, service);
        } catch (Exception e) {
            handleException("Error while retreiving service specific transport listener parameters; " +
                    "Transport: " + transport + ", Service: " + service, e);
        }
        return parameters;
    }

    /**
     * Get the service specific parameters for the specified transport sender and service
     *
     * @param transport name of the transport
     * @param service name of the service
     * @return an array of TransportParameter objects or null
     * @throws AxisFault on error
     */
    public TransportParameter[] getServiceSpecificOutParameters(String transport,
                                                                String service) throws AxisFault {
        TransportParameter[] parameters = new  TransportParameter[0];
        try {
             parameters = stub.getServiceSpecificOutParameters(transport, service);
        } catch (Exception e) {
            handleException("Error while retreiving service specific transport sender parameters; " +
                    "Transport: " + transport + ", Service: " + service, e);
        }
        return parameters;
    }

    /**
     * Update the global parameters for the specified transport listener
     *
     * @param transport name of the transport
     * @param params updated set of transport parameters
     * @throws AxisFault on error
     */
    public void updateGloballyDefinedInParameters(String transport,
                                                  TransportParameter[] params) throws AxisFault {
        try {
            stub.updateGloballyDefinedInParameters(transport, params);
        } catch (Exception e) {
            handleException("Error while updating parameters for " + transport + " listener", e);
        }
    }

    /**
     * Update the global parameters for the specified transport sender
     *
     * @param transport name of the transport
     * @param params updated set of transport parameters
     * @throws AxisFault on error
     */
    public void updateGloballyDefinedOutParameters(String transport,
                                                   TransportParameter[] params) throws AxisFault {
        try {
            stub.updateGloballyDefinedOutParameters(transport, params);
        } catch (Exception e) {
            handleException("Error while updating parameters for " + transport + " transport sender", e);
        }
    }

    /**
     * Update the global parameters for the specified transport listener
     *
     * @param transport name of the transport
     * @param service name of the service
     * @param params updated set of transport parameters
     * @throws AxisFault on error
     */
    public void updateServiceSpecificInParameters(String transport, String service,
                                                  TransportParameter[] params) throws AxisFault {
        try {
            stub.updateServiceSpecificInParameters(transport, service, params);
        } catch (Exception e) {
            handleException("Error while updating service specific transport listener parameters; " +
                    "Transport: " + transport + ", Service: " + service, e);
        }
    }

    /**
     * Update the global parameters for the specified transport sender
     *
     * @param transport name of the transport
     * @param service name of the service
     * @param params updated set of transport parameters
     * @throws AxisFault on error
     */
    public void updateServiceSpecificOutParameters(String transport, String service,
                                                   TransportParameter[] params) throws AxisFault {
        try {
            stub.updateServiceSpecificOutParameters(transport, service, params);
        } catch (Exception e) {
            handleException("Error while updating service specific transport sender parameters; " +
                    "Transport: " + transport + ", Service: " + service, e);
        }
    }

    /**
     * Globally disables the specified transport listener
     *
     * @param transport Name of the transport
     * @throws AxisFault on error
     */
    public void disableListener(String transport) throws AxisFault {
        try {
            stub.disableListener(transport);
        } catch (Exception e) {
            handleException("Error while disabling the " + transport + " transport listener", e);
        }
    }

    /**
     * Globally disables the specified transport sender
     *
     * @param transport Name of the transport
     * @throws AxisFault on error
     */
    public void disableSender(String transport) throws AxisFault {
        try {
            stub.disableSender(transport);
        } catch (Exception e) {
            handleException("Error while disabling the " + transport + " transport sender", e);
        }
    }

    /**
	 * Enables the specified transport listener with the given set of parameters.
	 *
     * @param transport Name of the transport
	 * @param inParams Updated set of transport parameters.
	 * @throws AxisFault on error
	 */
	public void enableListener(String transport, TransportParameter[] inParams) throws AxisFault {

		try {
            if (stub.dependenciesAvailable(transport, inParams)) {
                if (inParams != null) {
                    stub.updateGloballyDefinedInParameters(transport, inParams);
                } else {
                    stub.updateGloballyDefinedInParameters(transport, new TransportParameter[]{});
                }
            }

        } catch (Exception e) {
			handleException("Error while enabling the " + transport +" transport listener", e);
		}
	}

    /**
	 * Enables the specified transport sender with the given set of parameters.
	 *
     * @param transport Name of the transport
	 * @param outParams Updated set of transport parameters.
	 * @throws AxisFault on error
	 */
	public void enableSender(String transport, TransportParameter[] outParams) throws AxisFault {

		try {
            if (stub.dependenciesAvailable(transport, outParams)) {
                if (outParams != null) {
                    stub.updateGloballyDefinedOutParameters(transport, outParams);
                } else {
                    stub.updateGloballyDefinedOutParameters(transport, new TransportParameter[]{});
                }
            }

        } catch (Exception e) {
			handleException("Error while enabling the " + transport +" transport sender", e);
		}
	}

    /**
	 * Logs and wraps the given exception.
	 * 
	 * @param msg Error message
	 * @param e Exception
	 * @throws AxisFault on error
	 */
	private void handleException(String msg, Exception e) throws AxisFault {
		log.error(msg, e);
		throw new AxisFault(msg, e);
	}

    
}