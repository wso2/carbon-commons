/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.tracer.module;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.om.OMElement;

/**
 * The trace message which is stored in an in-memory buffer
 */
public class TraceMessage {
    private String operationName;
    private String serviceName;
    private int messageFlow;
    private long msgSequence;
    private OMElement soapEnvelope;
    private long timestamp;

    public TraceMessage(String serviceName, String operationName,
                        int messageFlow, long msgSequence, OMElement env) {
        this.operationName = operationName;
        this.serviceName = serviceName;
        this.messageFlow = messageFlow;
        this.msgSequence = msgSequence;
        this.soapEnvelope = env;
        this.timestamp = System.currentTimeMillis();
    }

    public String getOperationName() {
        return operationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getMessageFlow() {
        return messageFlow;
    }

    public OMElement getSoapEnvelope() {
        return soapEnvelope;
    }

    public long getMsgSequence() {
        return msgSequence;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
