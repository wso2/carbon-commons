/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.ws.internal.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.ws.internal.builders.*;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidExpirationTimeException;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.event.ws.internal.builders.utils.BuilderUtils;
import org.wso2.carbon.event.ws.internal.WSEventBrokerHolder;
import org.wso2.carbon.event.ws.internal.exception.WSEventException;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;
import org.wso2.carbon.event.ws.internal.util.EventBrokerUtils;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.Message;
import org.wso2.carbon.event.core.util.EventBrokerConstants;
import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;

import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;


public class CarbonEventingMessageReceiver extends AbstractMessageReceiver {


    private static final Log log = LogFactory.getLog(CarbonEventingMessageReceiver.class);

    private static final String ENABLE_SUBSCRIBE = "enableSubscribe";

    private static final String ENABLE_UNSUBSCRIBE = "enableUnsubscribe";

    private static final String ENABLE_RENEW = "enableRenew";

    private static final String ENABLE_GET_STATUS = "enableGetStatus";

    private static final Pattern TO_ADDRESS_PATTERN = Pattern.compile("/services/.*/publish/(.*)");

    private boolean isEnabled(MessageContext mc, String operation) {
        if (mc.getAxisService() != null) {
            String operationValue =
                    (String) mc.getAxisService().getParameterValue(operation);
            return operationValue == null || !operationValue.toLowerCase().equals(
                    Boolean.toString(false));
        }
        return true;
    }

    public final void invokeBusinessLogic(MessageContext mc) throws AxisFault {
        try {
            processMessage(mc);
        } catch (WSEventException e) {
            log.error("An exception occured. Unable to Process Request", e);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String details = sw.toString();
            pw.close();
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    e.getMessage(), details, mc.isSOAP11());
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleSubscribe(MessageContext mc) throws AxisFault, WSEventException {
        try {
            if (!isEnabled(mc, ENABLE_SUBSCRIBE)) {
                log.warn("Subscribe operation is disabled");
                return;
            }
            Subscription subscription = null;
            SubscribeCommandBuilder builder = new SubscribeCommandBuilder(mc);
            try {
                subscription = builder.toSubscription(mc.getEnvelope());
                subscription.setOwner(EventBrokerUtils.getLoggedInUserName());
            } catch (InvalidExpirationTimeException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage());
                }
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "InvalidExpirationTime",
                        e.getMessage(), "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
            } catch (InvalidMessageException e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage());
                }
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "InvalidMessage",
                        e.getMessage(), "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
            }

            if (subscription != null) {
                // set the topic name using the url
                String toAddress = mc.getOptions().getTo().getAddress();
                String topicName = subscription.getEventFilter().getValue();

                if ((topicName == null) || (topicName.equals(""))) {
                    // we take string after the service name as the topic name
                    if (toAddress.indexOf(EventingConstants.BROKER_SERVICE_NAME + "/") > 0) {
                        topicName = toAddress.substring(
                                toAddress.indexOf(EventingConstants.BROKER_SERVICE_NAME + "/")
                                + EventingConstants.BROKER_SERVICE_NAME.length() + 1);
                    }
                }

                subscription.setTopicName(topicName);

                if (log.isDebugEnabled()) {
                    log.debug("Subscription request recieved  : " + subscription.getId());
                }
                if (subscription.getEventSinkURL().startsWith("sqs://")){
                    subscription.setEventDispatcherName(EventBrokerConstants.EVENT_SINK_DISPATCHER_NAME);
                } else {
                    subscription.setEventDispatcherName(EventBrokerConstants.WS_EVENT_DISPATCHER_NAME);
                }

                String subID = WSEventBrokerHolder.getInstance().getEventBroker().subscribe(subscription);
                subscription.setId(subID);
                if (subID != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Sending subscription response for Subscription ID : " +
                                  subscription.getId());
                    }
                    SOAPEnvelope soapEnvelope = builder.fromSubscription(subscription);
                    dispatchResponse(soapEnvelope, EventingConstants.WSE_SUBSCRIBE_RESPONSE, mc, false);
                } else {
                    log.debug("Subscription Failed, sending fault response");
                    SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                            EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                            "Unable to subscribe ", "", mc.isSOAP11());
                    dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
                }
            } else {
                log.debug("Subscription Failed, sending fault response");
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                        "Unable to subscribe ", "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if(errorMessage.contains("errorCode=UNAUTHORIZED_ACCESS")){
                String user = EventBrokerUtils.getLoggedInUserName();
                errorMessage = user +" does not has permission to perform subscription, please check permissions";
            }
            throw new WSEventException(errorMessage, e);
        }
    }

    protected void handleUnsubscribe(MessageContext mc) throws AxisFault, WSEventException {
        if (!isEnabled(mc, ENABLE_UNSUBSCRIBE)) {
            log.warn("Unsubscribe operation is disabled");
            return;
        }

        try {

            UnSubscribeCommandBuilder builder = new UnSubscribeCommandBuilder(mc);
            Subscription subscription = builder.toSubscription(mc.getEnvelope());
            if (log.isDebugEnabled()) {
                log.debug("UnSubscribe response recived for Subscription ID : " +
                          subscription.getId());
            }
            getBrokerService().unsubscribe(subscription.getId());
            if (log.isDebugEnabled()) {
                log.debug("Sending UnSubscribe responce for Subscription ID : " +
                          subscription.getId());
            }
            SOAPEnvelope soapEnvelope = builder.fromSubscription(subscription);
            dispatchResponse(soapEnvelope, EventingConstants.WSE_UNSUBSCRIBE_RESPONSE, mc, false);
        } catch (InvalidMessageException e) {
            throw new WSEventException("Invalid message ", e);
        } catch (EventBrokerException e) {
            log.debug("UnSubscription failed, sending fault repsponse");
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "Unable to Unsubscribe", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
        }

    }

    protected void handleGetStatus(MessageContext mc) throws AxisFault, WSEventException {
        if (!isEnabled(mc, ENABLE_GET_STATUS)) {
            log.warn("Get Status operation is disabled");
            return;
        }
        try {
            GetStatusCommandBuilder builder = new GetStatusCommandBuilder(mc);
            Subscription subscription = builder.toSubscription(mc.getEnvelope());

            if (log.isDebugEnabled()) {
                log.debug("GetStatus request recived for Subscription ID : " +
                          subscription.getId());
            }
            subscription = getBrokerService().getSubscription(subscription.getId());
            if (subscription != null) {
                String loggedInUser = EventBrokerUtils.getLoggedInUserName();
                if (!loggedInUser.equals("admin") && !loggedInUser.equals(subscription.getOwner())) {
                    throw new WSEventException("User " + loggedInUser + " does not own subscription " + subscription.getId());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Sending GetStatus responce for Subscription ID : " +
                              subscription.getId());
                }
                SOAPEnvelope soapEnvelope = builder.fromSubscription(subscription);
                dispatchResponse(soapEnvelope, EventingConstants.WSE_GET_STATUS_RESPONSE, mc, false);
            } else {
                log.debug("GetStatus failed, sending fault response");
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                        "Subscription Not Found", "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
            }
        } catch (InvalidMessageException e) {
            throw new WSEventException("Invalid message exception ", e);
        } catch (EventBrokerException e) {
            throw new WSEventException("Event processing exception ",e);
        }
    }

    protected void handleRenew(MessageContext mc) throws AxisFault, WSEventException {
        if (!isEnabled(mc, ENABLE_RENEW)) {
            log.warn("Renew operation is disabled");
            return;
        }
        RenewCommandBuilder builder = new RenewCommandBuilder(mc);
        Subscription subscription = null;
        try {
            subscription = builder.toSubscription(mc.getEnvelope());
        } catch (InvalidExpirationTimeException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "InvalidExpirationTime",
                    e.getMessage(), "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
        } catch (InvalidMessageException e) {
            e.printStackTrace();
        }

        if (subscription != null && subscription.getId() != null) {
            if (log.isDebugEnabled()) {
                log.debug("Renew request recived for Subscription ID : " +
                          subscription.getId());
            }

            try {
                getBrokerService().renewSubscription(subscription);
                if (log.isDebugEnabled()) {
                    log.debug("Sending Renew response for Subscription ID : " +
                              subscription.getId());
                }
                SOAPEnvelope soapEnvelope =
                        builder.fromSubscription(subscription);
                dispatchResponse(soapEnvelope, EventingConstants.WSE_RENEW_RESPONSE, mc, false);
            } catch (EventBrokerException e) {
                log.debug("Renew failed, sending fault response");
                SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "UnableToRenew",
                        "Subscription Not Found", "", mc.isSOAP11());
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
            }

        } else {
            SOAPEnvelope soapEnvelope = BuilderUtils.genFaultResponse(
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "UnableToRenew",
                    "Subscription Not Found", "", mc.isSOAP11());
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
        }
    }

    protected void handleGetSubscriptions(MessageContext mc) throws WSEventException {
        try {

            OMElement element = mc.getEnvelope().getBody().getFirstElement();
            String maxResultCountStr = element.getAttributeValue(new QName("maxResultCount"));
            String resultFilterStr = element.getAttributeValue(new QName("resultFilter"));
            String firstIndexStr = element.getAttributeValue(new QName("firstIndex"));

            boolean ascending = true;
            String sortingInstructions = null;
            if (mc.getEnvelope().getHeader() != null) {
                OMElement sortingInstructionsEle = mc.getEnvelope().getHeader().getFirstChildWithName(EventingConstants.SORTING_DATA);
                if (sortingInstructionsEle != null) {
                    sortingInstructions = sortingInstructionsEle.getText();
                    String style = sortingInstructionsEle.getAttributeValue(new QName(EventingConstants.SORTING_STYLE));
                    ascending = (style == null || !style.equals(EventingConstants.SORTING_STYLES.decending.toString()));
                }
            }

            int maxResultCount = Integer.MAX_VALUE;
            if (maxResultCountStr != null) {
                maxResultCount = Integer.parseInt(maxResultCountStr);
                if (maxResultCount < 0) {
                    maxResultCount = Integer.MAX_VALUE;
                }
            }

            int firstIndex = 0;
            if (firstIndexStr != null) {
                firstIndex = Integer.parseInt(firstIndexStr);
            }


            String loggedInUser = EventBrokerUtils.getLoggedInUserName();
            List<Subscription> filteredSubscriptions = new ArrayList<Subscription>();
            List<Subscription> subscriptions = getBrokerService().getAllSubscriptions(resultFilterStr);

            subscriptions = sortResults(sortingInstructions, ascending, subscriptions);
            for (Subscription subscription : subscriptions) {
                if (loggedInUser.equals("admin") || loggedInUser.equals(subscription.getOwner())) {
                    filteredSubscriptions.add(subscription);
                }
            }


            SOAPEnvelope getSubscriptionsResponseEnv =
                    GetSubscriptionsCommandBuilder.buildResponseforGetSubscriptions(
                            filteredSubscriptions, maxResultCount, firstIndex);
            dispatchResponse(getSubscriptionsResponseEnv,
                             EventingConstants.WSE_RENEW_RESPONSE, mc, false);
        } catch (AxisFault e) {
            throw new WSEventException("Error at Get Subscriptions:" + e.getMessage(), e);
        } catch (EventBrokerException e) {
            throw new WSEventException("Can not get the subscriptions ", e);
        }
    }

    protected void handleInstallEventSink(MessageContext mc) throws WSEventException {
        throw new UnsupportedOperationException();
    }

    protected void handleEvent(MessageContext mc) throws AxisFault, WSEventException {
        String topic = EventBrokerUtils.extractTopicFromMessage(mc);
        if (topic == null) {
            // No topic, just drop the message.
            return;
        }
        try {
            Message message = new Message();
            message.setMessage(mc.getEnvelope().getBody().getFirstElement());
            getBrokerService().publishRobust(message, topic);
        } catch (EventBrokerException e) {
            throw new WSEventException("Can not publish the message : " + e.getMessage(), e);
        }

    }

    public final void processMessage(MessageContext mc) throws AxisFault, WSEventException {
        if (EventingConstants.WSE_SUBSCRIBE.equals(mc.getWSAAction())) {
            handleSubscribe(mc);
        } else if (EventingConstants.WSE_UNSUBSCRIBE.equals(mc.getWSAAction())) {
            handleUnsubscribe(mc);
        } else if (EventingConstants.WSE_GET_STATUS.equals(mc.getWSAAction())) {
            handleGetStatus(mc);
        } else if (EventingConstants.WSE_RENEW.equals(mc.getWSAAction())) {
            handleRenew(mc);
        } else if (EventingConstants.WSE_GET_SUBSCRIPTIONS.equals(mc.getWSAAction())) {
            handleGetSubscriptions(mc);
        } else if (EventingConstants.WSE_INSTALL_EVENT_SINK.equals(mc.getWSAAction())) {
            //TODO this is to install certain new event sink (e.g. by adding classes to classpath) so it can be used by subscriptions
            handleInstallEventSink(mc);
        } else {
            handleEvent(mc);
        }
    }

    /**
     * Dispatch the message to the target endpoint
     *
     * @param soapEnvelope   Soap Enevlop with message
     * @param responseAction WSE action for the response
     * @param mc             Message Context
     * @param isFault        Whether a Fault message must be sent
     * @throws AxisFault Thrown by the axis2 engine.
     */
    private void dispatchResponse(SOAPEnvelope soapEnvelope, String responseAction,
                                  MessageContext mc, boolean isFault) throws AxisFault {
        MessageContext rmc = MessageContextBuilder.createOutMessageContext(mc);
        rmc.getOperationContext().addMessageContext(rmc);
        replicateState(mc);
        rmc.setEnvelope(soapEnvelope);
        rmc.setWSAAction(responseAction);
        rmc.setSoapAction(responseAction);
        if (isFault) {
            AxisEngine.sendFault(rmc);
        } else {
            AxisEngine.send(rmc);
        }
    }

    private EventBroker getBrokerService() {
        return WSEventBrokerHolder.getInstance().getEventBroker();
    }

    public List<Subscription> sortResults(String sortingInstructions, final boolean ascending, List<Subscription> list) {
        if (sortingInstructions != null) {
            Comparator<Subscription> comparator = null;

            if (sortingInstructions.equals("eventSinkAddress")) {
                comparator = new Comparator<Subscription>() {
                    public int compare(Subscription o1, Subscription o2) {
                        if (o2 == null || o1 == null) {
                            return 0;
                        }
                        return (ascending ? 1 : -1) * o1.getEventSinkURL().compareTo(o2.getEventSinkURL());
                    }
                };
            } else if (sortingInstructions.equals("createdTime")) {
                comparator = new Comparator<Subscription>() {
                    public int compare(Subscription o1, Subscription o2) {
                        if (o2 == null || o1 == null) {
                            return 0;
                        }
                        return (ascending ? 1 : -1) * o1.getCreatedTime().compareTo(o2.getCreatedTime());
                    }
                };
            } else if (sortingInstructions.equals("subscriptionEndingTime")) {
                comparator = new Comparator<Subscription>() {
                    public int compare(Subscription o1, Subscription o2) {
                        if (o2 == null || o1 == null) {
                            return 0;
                        }
                        return (ascending ? 1 : -1) * o1.getExpires().compareTo(o2.getExpires());
                    }
                };

            }
            if (comparator != null) {
                Collections.sort(list, comparator);
            }
        }
        return list;
    }


}
