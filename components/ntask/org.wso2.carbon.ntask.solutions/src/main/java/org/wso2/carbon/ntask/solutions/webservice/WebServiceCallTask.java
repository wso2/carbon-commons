/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.ntask.solutions.webservice;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

/**
 * This represents a task implementation for a web service invocation.
 */
public class WebServiceCallTask implements Task {
	
	private static final Log log = LogFactory.getLog(WebServiceCallTask.class);

	public static final String SERVICE_TARGET_EPR = "SERVICE_TARGET_EPR";
	
	public static final String SERVICE_ACTION = "SERVICE_ACTION";
	
	public static final String SERVICE_PAYLOAD_HANDLER_CLASS = "SERVICE_PAYLOAD_HANDLER_CLASS";
	
	public static final String SERVICE_MEP = "SERVICE_MEP";
	
	public static final String SERVICE_MEP_IN_ONLY = "IN_ONLY";
	
	public static final String SERVICE_MEP_IN_OUT = "IN_OUT";
	
	private ServiceClient serviceClient;
	
	private String serviceURL;
	
	private String serviceAction;
	
	private String serviceMEP;
	
	private int taskCount;
	
	private ServicePayloadHandler servicePayloadHandler;

	public WebServiceCallTask() {
		try {
			this.serviceClient = new ServiceClient();
			this.serviceClient.getOptions().setCallTransportCleanup(true);
		} catch (Exception e) {
			throw new RuntimeException("Error while initializing the web service call task", e);
		}
	}
	
	public String getServiceMEP() {
		return serviceMEP;
	}
	
	public ServicePayloadHandler getServicePayloadHandler() {
		return servicePayloadHandler;
	}

	public ServiceClient getServiceClient() {
		return serviceClient;
	}

	public String getServiceURL() {
		return serviceURL;
	}

	public String getServiceAction() {
		return serviceAction;
	}

	public int getTaskCount() {
		return taskCount;
	}
	
	private OMElement extractInputPayload() {
		OMElement payload;
		if (this.getServicePayloadHandler() != null) {
			payload = this.getServicePayloadHandler().getInputPayload(this);
		} else {
			payload = null;
		}
		return payload;
	}
	
	@Override
	public void execute(Map<String, String> properties) {
		try {
			String action = properties.get(SERVICE_ACTION);
			if (action != null) {
				this.getServiceClient().getOptions().setAction(action);
			}
			String epr = properties.get(SERVICE_TARGET_EPR);
			if (epr != null) {
				this.getServiceClient().setTargetEPR(new EndpointReference(epr));
			}
			String servicePayloadHandlerClass = properties.get(SERVICE_PAYLOAD_HANDLER_CLASS);
			if (servicePayloadHandlerClass != null) {
				this.servicePayloadHandler =
						(ServicePayloadHandler) Class.forName(servicePayloadHandlerClass).newInstance();
			}
			this.serviceMEP = properties.get(SERVICE_MEP);
			if (this.getServiceMEP() == null) {
				this.serviceMEP = SERVICE_MEP_IN_ONLY;
			}
			OMElement payload = this.extractInputPayload();
			if (SERVICE_MEP_IN_OUT.equals(this.getServiceMEP())) {
				OMElement data = this.getServiceClient().sendReceive(payload);
				if (this.getServicePayloadHandler() != null) {
					this.getServicePayloadHandler().handleServiceResult(this, data);
				}
			} else {
				this.getServiceClient().sendRobust(payload);
			}
			this.taskCount++;
		} catch (Exception e) {
			log.error("Error in executing web service call task: " + e.getMessage(), e);
		}
	}

}
