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

import org.wso2.carbon.discovery.DiscoveryConstants;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a WS-Discovery probe. A probe may consist of a set of types,
 * scopes and a matching rule definition.
 */
public class Probe {
    
    private QName[] types;
    private URI[] scopes;
    private URI matchBy;

    public Probe() {
        try {
            matchBy = new URI(DiscoveryConstants.SCOPE_MATCH_RULE_DEAULT);
        } catch (URISyntaxException ignore) {

        }
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

    public URI getMatchBy() {
        return matchBy;
    }

    public void setMatchBy(URI matchBy) {
        this.matchBy = matchBy;
    }
}
