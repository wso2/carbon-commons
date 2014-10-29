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

package org.wso2.carbon.event.ws.internal.util;

import javax.xml.namespace.QName;
import java.util.regex.Pattern;

public interface EventingConstants {

    String WSE_EVENTING_NS = "http://schemas.xmlsoap.org/ws/2004/08/eventing";
    String WSE_EXTENDED_EVENTING_NS = "http://ws.apache.org/ws/2007/05/eventing-extended";
    String WSE_EVENTING_PREFIX = "wse";
    String WSE_DEFAULT_DELIVERY_MODE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/DeliveryModes/Push";

    //Actions
    String WSE_SUBSCRIBE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe";
    String WSE_SUBSCRIBE_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscribeResponse";

    /**
     * @deprecated A typo in this field name has been corrected. Use {@link #WSE_SUBSCRIBE_RESPONSE} instead.
     */
    String WSE_SUbSCRIBE_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscribeResponse";
    String WSE_RENEW = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew";
    String WSE_RENEW_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/RenewResponse";
    String WSE_UNSUBSCRIBE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe";
    String WSE_UNSUBSCRIBE_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/UnsubscribeResponse";
    String WSE_GET_STATUS = "http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus";
    String WSE_GET_STATUS_RESPONSE = "http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatusResponse";
    String WSE_SUBSCRIPTIONEND = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscriptionEnd";
    String WSE_PUBLISH = "http://ws.apache.org/ws/2007/05/eventing-extended/Publish";
    String WSE_GET_SUBSCRIPTIONS = "http://ws.apache.org/ws/2007/05/eventing-extended/getSubscriptions";
    String WSE_GET_SUBSCRIPTIONS_RESPONSE = "http://ws.apache.org/ws/2007/05/eventing-extended/getSubscriptionsResponse";
    String WSE_INSTALL_EVENT_SINK = "http://schemas.xmlsoap.org/ws/2004/08/eventing/installEventSink";


    //Elements
    String WSE_EN_SUBSCRIBE = "Subscribe";
    String WSE_EN_END_TO = "EndTo";
    String WSE_EN_DELIVERY = "Delivery";
    String WSE_EN_MODE = "Mode";
    String WSE_EN_NOTIFY_TO = "NotifyTo";
    String WSE_EN_EXPIRES = "Expires";
    String WSE_EN_FILTER = "Filter";
    String WSE_EN_DIALECT = "Dialect";
    String WSE_EN_SUBSCRIBE_RESPONSE = "SubscribeResponse";
    String WSE_EN_SUBSCRIPTION_MANAGER = "SubscriptionManager";
    String WSE_EN_RENEW = "Renew";
    String WSE_EN_RENEW_RESPONSE = "RenewResponse";
    String WSE_EN_IDENTIFIER = "Identifier";
    String WSE_EN_UNSUBSCRIBE = "Unsubscribe";
    String WSE_EN_GET_STATUS = "GetStatus";
    String WSE_EN_GET_STATUS_RESPONSE = "GetStatusResponse";
    String WSE_EN_TOPIC = "topic";
    String WSE_EN_XPATH = "XPath";
    String WSE_EN_SUBSCRIPTIONEND = "SubscriptionEnd";

    //Faults
    String WSA_FAULT = "http://schemas.xmlsoap.org/ws/2004/08/addressing/fault";
    String WSE_FAULT_CODE_SENDER = "Sender";
    String WSE_FAULT_CODE_RECEIVER = "Receiver";
    String WSE_FAULT_EN_FAULT = "Fault";
    String WSE_FAULT_EN_CODE = "Code";
    String WSE_FAULT_EN_SUB_CODE = "Subcode";
    String WSE_FAULT_EN_REASON = "Reason";
    String WSE_FAULT_EN_DETAIL = "Detail";
    String WSE_FAULT_EN_VALUE = "Value";
    String WSE_FAULT_EN_TEXT = "Text";
    String WSE_FAULT_EN_TEXT_ATTR = "lang";

    String SUBSCRIPTION_MANAGER = "subscriptionManager";

    //Operations
    String WSE_SUBSCRIBE_OP = "SubscribeOp";
    String WSE_RENEW_OP = "RenewOp";
    String WSE_UNSUBSCRIBE_OP = "UnsubscribeOp";
    String WSE_GET_STATUS_OP = "GetStatusOp";
    String WSE_SUBSCRIPTIONEND_OP = "SubscriptionEnd";

    String TOPIC_HEADER_NAME = "topic";
    String TOPIC_HEADER_NS = "http://wso2.org/ns/2009/09/eventing/notify";

    String EVENTING_EXECUTABLE_SCRIPT_ELEMENT = "ExecuatableScript";
    String EXECUTE_SCRIPT_URI = "http://wso2.org/event/localScriptAsEventSink";

    

    String EXTSNSIONS_URI = "http://wso2.org/Services/extensions";
    QName SORTING_DATA = new QName(EXTSNSIONS_URI, "sortby");
    String SORTING_STYLE = "style";

    enum SORTING_STYLES {
        ascending, decending
    };

    String AUTH_WRITE_ACTION = "write";

    String MESSAGEBOX_AUTH_PERMISSION_SPACE = "/Permission/Messagebox";
    String SECURE_TOPIC_RESOURCE_PREFIX = "/SecureTopic";

    String NOTIFICATION_NS_URI = "http://wso2.org/ns/2009/09/eventing/notify";
    String NOTIFICATION_NS_PREFIX = "ns";

    String BROKER_SERVICE_NAME = "EventBrokerService";

    Pattern TO_ADDRESS_PATTERN = Pattern.compile("/services/.*/publish/(.*)");
}
