/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery;

import javax.xml.namespace.QName;

public class DiscoveryConstants {

    public static final String WS_DISCOVERY_NAMESPACE = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01";
    public static final String WS_DISCOVERY_NS_PREFIX = "wsd";
    public static final String EPR_ADDRESS_PREFIX = "urn:uuid:";

    // QName definitions for parsing SOAP payloads
    public static final QName HELLO = new QName(WS_DISCOVERY_NAMESPACE, "Hello", WS_DISCOVERY_NS_PREFIX);
    public static final QName BYE = new QName(WS_DISCOVERY_NAMESPACE, "Bye", WS_DISCOVERY_NS_PREFIX);
    public static final QName PROBE = new QName(WS_DISCOVERY_NAMESPACE, "Probe", WS_DISCOVERY_NS_PREFIX);
    public static final QName RESOLVE = new QName(WS_DISCOVERY_NAMESPACE, "Resolve", WS_DISCOVERY_NS_PREFIX);

    public static final QName PROBE_MATCHES = new QName(WS_DISCOVERY_NAMESPACE, "ProbeMatches", WS_DISCOVERY_NS_PREFIX);
    public static final QName RESOLVE_MATCHES = new QName(WS_DISCOVERY_NAMESPACE, "ResolveMatches", WS_DISCOVERY_NS_PREFIX);
    public static final QName PROBE_MATCH = new QName(WS_DISCOVERY_NAMESPACE, "ProbeMatch", WS_DISCOVERY_NS_PREFIX);
    public static final QName RESOLVE_MATCH = new QName(WS_DISCOVERY_NAMESPACE, "ResolveMatch", WS_DISCOVERY_NS_PREFIX);

    public static final QName METADATA_VERSION = new QName(WS_DISCOVERY_NAMESPACE, "MetadataVersion", WS_DISCOVERY_NS_PREFIX);
    public static final QName TYPES = new QName(WS_DISCOVERY_NAMESPACE, "Types", WS_DISCOVERY_NS_PREFIX);
    public static final QName SCOPES = new QName(WS_DISCOVERY_NAMESPACE, "Scopes", WS_DISCOVERY_NS_PREFIX);
    public static final QName XADDRESSES = new QName(WS_DISCOVERY_NAMESPACE, "XAddrs", WS_DISCOVERY_NS_PREFIX);

    public static final QName ATTR_MATCH_BY = new QName("MatchBy");

    // Types of WS-D notifications
    public static final int NOTIFICATION_TYPE_HELLO = 0;
    public static final int NOTIFICATION_TYPE_BYE = 1;

    // Types of WS-D response messages
    public static final int RESULT_TYPE_PROBE_MATCH = 0;
    public static final int RESULT_TYPE_RESOLVE_MATCH = 1;

    public static final String CONFIG_SCOPES = "Scopes";
    public static final String CONFIG_METADATA_VERSION = "MetadataVersion";
    public static final String CONFIG_UNIQUE_ID = "UniqueID";

    public static final String WS_DISCOVERY_PARAMS = "wsDiscoveryParams";
    public static final String UNDISCOVERABLE_SERVICE = "undiscoverableService";
    public static final String DISCOVERY_PROXY = "DiscoveryProxy";
    public static final String DISCOVERY_SERVICE_PROXY = "DiscoveryServiceProxy";
    public static final String SKIP_INACTIVE_SERVICES = "skipInactiveServices";

    // WS-D defined SOAP actions
    public static final String WS_DISCOVERY_PROBE_ACTION = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/Probe";
    public static final String WS_DISCOVERY_RESOLVE_ACTION = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/Resolve";
    public static final String WS_DISCOVERY_HELLO_ACTION = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/Hello";
    public static final String WS_DISCOVERY_BYE_ACTION = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/Bye";

    // Supported scope match strategies
    public static final String SCOPE_MATCH_RULE_RFC3986 = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/rfc3986";
    public static final String SCOPE_MATCH_RULE_STRCMP = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/strcmp0";
    public static final String SCOPE_MATCH_RULE_UUID = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/uuid";
    public static final String SCOPE_MATCH_RULE_NONE = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/none";
    public static final String SCOPE_MATCH_RULE_DEAULT = SCOPE_MATCH_RULE_RFC3986;

    public static final String DISCOVERY_DEFAULT_SCOPE = "http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01/DefaultScope";
    public static final int DISCOVERY_DEFAULT_METADATA_VERSION = 1;

    public static final String DISCOVERY_TARGET_EPR = "http://docs.oasis-open.org/DiscoveryTargetEPR";
    
    public static final String DISCOVERY_TYPES = "DiscoveryTypes";
    public static final String DISCOVERY_SCOPES = "DiscoveryScopes";
    public static final String DISCOVERY_SCHEME = "DiscoveryScheme";

    // Constants used by the persistence manager
    public static final String ATTR_METADATA_VERSION = "metadataVersion";
    public static final String ATTR_TYPES = "overview_types";
    public static final String ATTR_ENDPOINTS = "endpoints_entry";
    public static final String ATTR_SCOPES = "overview_scopes";
    public static final String ATTR_EPR = "endpoint";

    public static final String SERVICE_NAME_PREFIX = "DiscoveredService_";

    // Policy file names
    public static final String DISCOVERY_CLIENT_POLICY = "wsd-client-policy.xml";

    //Constants used to create SOAP header
    public static final String DISCOVERY_HEADER_ELEMENT_NAMESPACE = "http://www.wso2.org/ws/discovery";
    public static final String DISCOVERY_HEADER_ELEMENT_NAMESPACE_PREFIX = "mns";
    public static final String DISCOVERY_HEADER_SERVICE_NAME = "serviceName";
    public static final String DISCOVERY_HEADER_WSDL_URI = "wsdlURI";

}
