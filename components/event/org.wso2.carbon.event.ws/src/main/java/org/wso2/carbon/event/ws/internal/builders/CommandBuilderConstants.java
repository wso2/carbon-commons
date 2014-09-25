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
package org.wso2.carbon.event.ws.internal.builders;

import org.wso2.carbon.event.ws.internal.util.EventingConstants;

import javax.xml.namespace.QName;

public class CommandBuilderConstants {

    private static final String NULL_NAMESPACE = "";

    public static final QName SUBSCRIBE_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_SUBSCRIBE);
    public static final QName DELIVERY_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_DELIVERY);
    public static final QName FILTER_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_FILTER);
    public static final QName NOTIFY_TO_QNAME =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_NOTIFY_TO);
    public static final QName ATT_DIALECT =
            new QName(NULL_NAMESPACE, EventingConstants.WSE_EN_DIALECT);
    public static final QName ATT_XPATH =
            new QName(NULL_NAMESPACE, EventingConstants.WSE_EN_XPATH);
    public static final QName IDENTIFIER =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_IDENTIFIER);
    public static final QName EXPIRES =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_EXPIRES);
    public static final QName RENEW =
            new QName(EventingConstants.WSE_EVENTING_NS, EventingConstants.WSE_EN_RENEW);
}
