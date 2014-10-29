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

package org.wso2.carbon.discovery.messages;

import org.apache.axis2.addressing.EndpointReference;

import javax.xml.namespace.QName;
import java.net.URI;

/**
 * Represents a WS-Discovery target service instance. A target service
 * consists of a service ID (endpoint reference) and may also contain a
 * set of types, scopes, transport addresses (x-addresses) and a version
 * number.
 */
public class TargetService {

    protected EndpointReference epr;
    protected QName[] types;
    protected URI[] scopes;
    protected URI[] xAddresses;
    protected int metadataVersion = -1;

    public TargetService(EndpointReference epr) {
        this.epr = epr;
    }

    public EndpointReference getEpr() {
        return epr;
    }

    public int getMetadataVersion() {
        return metadataVersion;
    }

    public void setMetadataVersion(int metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

    public URI[] getScopes() {
        return scopes;
    }

    public void setScopes(URI[] scopes) {
        this.scopes = scopes;
    }

    public QName[] getTypes() {
        return types;
    }

    public void setTypes(QName[] types) {
        this.types = types;
    }

    public URI[] getXAddresses() {
        return xAddresses;
    }

    public void setXAddresses(URI[] xAddresses) {
        this.xAddresses = xAddresses;
    }    
}
