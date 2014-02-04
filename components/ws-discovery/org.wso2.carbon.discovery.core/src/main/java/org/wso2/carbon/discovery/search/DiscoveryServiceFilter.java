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

package org.wso2.carbon.discovery.search;

import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.messages.Probe;
import org.wso2.carbon.discovery.util.Util;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.dataobjects.Service;

import javax.xml.namespace.QName;
import java.net.URI;

/**
 * Service filter implementation for filtering out service artifacts stored in the governance
 * registry, to find the set of services that meets the constraints specified in a WS-D probe.
 */
public class DiscoveryServiceFilter implements ServiceFilter {

    private Probe probe;

    private boolean skipInactiveServices;

    public DiscoveryServiceFilter(Probe probe) {
        this.probe = probe;
    }

    public void setSkipInactiveServices(boolean skipInactiveServices) {
        this.skipInactiveServices = skipInactiveServices;
    }

    public boolean matches(Service service) throws GovernanceException {
        QName[] existingTypes = null;
        URI[] existingScopes = null;

        if(service.getAttachedEndpoints().length == 0 || (skipInactiveServices && !service.isActive())) {
            return false;
        }

        String[] types = service.getAttributes(DiscoveryConstants.ATTR_TYPES);
        if (types != null) {
            existingTypes = Util.toQNameArray(types);
        }

        String[] scopes = service.getAttributes(DiscoveryConstants.ATTR_SCOPES);
        if (scopes != null) {
            existingScopes = Util.toURIArray(scopes);
        }

        return matchTypes(existingTypes, probe.getTypes()) &&
                matchScopes(existingScopes, probe.getScopes(), probe.getMatchBy());
    }

    private boolean matchTypes(QName[] existingTypes, QName[] requiredTypes) {
        if (requiredTypes == null) {
            return true;
        }

        if (existingTypes != null) {
            for (QName requiredType : requiredTypes) {

                boolean typeFound = false;

                for (QName existingType : existingTypes) {
                    if (requiredType.equals(existingType)) {
                        typeFound = true;
                        break;
                    }
                }

                if (!typeFound) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean matchScopes(URI[] existingScopes, URI[] requiredScopes, URI rule) {
        if (requiredScopes == null) {
            // According to the spec if the probe does not contain any scopes we should
            // default to the 'any scopes'
            // So any scope should match...
            return true;
        }

        ScopeMatchStrategy strategy = getScopeMatchStrategy(rule);
        if (strategy != null) {
            return strategy.match(existingScopes, requiredScopes);
        }

        return false;
    }

    private ScopeMatchStrategy getScopeMatchStrategy(URI rule) {
        if (DiscoveryConstants.SCOPE_MATCH_RULE_RFC3986.equals(rule.toString())) {
            return new RFC3986ScopeMatchStrategy();
        } else if (DiscoveryConstants.SCOPE_MATCH_RULE_STRCMP.equals(rule.toString())) {
            return new StrCmpScopeMatchStrategy();
        } else if (DiscoveryConstants.SCOPE_MATCH_RULE_UUID.equals(rule.toString())) {
            return new UUIDScopeMatchStrategy();
        } else if (DiscoveryConstants.SCOPE_MATCH_RULE_NONE.equals(rule.toString())) {
            return new NoneScopeMatchStrategy();
        }
        return null;
    }
}
