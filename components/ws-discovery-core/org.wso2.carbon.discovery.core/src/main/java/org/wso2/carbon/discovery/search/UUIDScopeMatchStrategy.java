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

import java.net.URI;
import java.util.UUID;

public class UUIDScopeMatchStrategy implements ScopeMatchStrategy {

    public boolean match(URI[] expected, URI[] found) {
        for (URI foundScope : found) {
            boolean matchFound = false;
            UUID foundUUID = toUUID(foundScope);

            if (foundUUID != null) {
                for (URI expectedScope : expected) {
                    UUID expectedUUID = toUUID(expectedScope);
                    if (expectedUUID != null && foundUUID.equals(expectedUUID)) {
                        matchFound = true;
                        break;
                    }
                }
            }

            if (!matchFound) {
                return false;
            }
        }
        return true;
    }

    private UUID toUUID(URI uri) {
        UUID uuid = null;
        try {            
            if (uri.getScheme() == null) {
                uuid = UUID.fromString(uri.toString());
            } else {
                if (uri.getScheme().equals("urn")) {
                    uri = URI.create(uri.getSchemeSpecificPart());
                }

                if (uri.getScheme().equals("uuid")) {
                    uuid = UUID.fromString(uri.getSchemeSpecificPart());
                }
            }
        } finally {
            return uuid;
        }
    }


}
