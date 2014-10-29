/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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
 *
 */
package org.wso2.carbon.tracer.module.handler;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.tracer.TracerConstants;

/**
 * This handler gets hold of the SOAP Enveloper, and temporatilly stores it in the In MessageContext.
 * This is done because the SOAP Tracer needs to show the original SOAP Message that came in.
 * Handlers down the line, like the Rampart handlers, can change the SOAP Envelope, so we make it a
 * point to save the original SOAP envelope, before other handlers manipulate it.
 */
public class TracingMessageInObservationHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        msgContext.setProperty(TracerConstants.TEMP_IN_ENVELOPE,
                               msgContext.getEnvelope().cloneOMElement());
        return InvocationResponse.CONTINUE;
    }
}
