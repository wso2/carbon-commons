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

public class RFC3986ScopeMatchStrategy implements ScopeMatchStrategy {

    public boolean match(URI[] expected, URI[] found) {
        if (expected == null) {
            // No scopes to compare with => Any of the scopes in the probe does not match
            return false;
        }

        URI[] expectedScopes = normalize(expected);
        URI[] foundScopes = normalize(found);

        for (URI probe : foundScopes) {
            boolean matchFound = false;
            for (URI target : expectedScopes) {
                if (matchURIs(target, probe)) {
                    matchFound = true;
                    break;
                }
            }

            if (!matchFound) {
                return false;
            }
        }
        return true;
    }

    private URI[] normalize(URI[] uriArray) {
        URI[] normalizedURIs = new URI[uriArray.length];
        for (int i = 0; i < uriArray.length; i++) {
            String uriTxt = uriArray[i].toString();
            // remove trailing '/' if present
            if (uriTxt.endsWith("/")) {
                uriTxt = uriTxt.substring(0, uriTxt.length() - 1);
            }
            normalizedURIs[i] = URI.create(uriTxt).normalize();
        }

        return normalizedURIs;
    }

    private boolean matchURIs(URI expected, URI found) {

        // First compare the scheme and the authority in a case insensitive manner
        if (expected.getScheme().equalsIgnoreCase(found.getScheme()) &&
                expected.getAuthority().equalsIgnoreCase(found.getAuthority())) {

            String[] expectedSegments = expected.getPath().split("/");
            String[] foundSegments = found.getPath().split("/");

            // Compare path segments
            if (foundSegments.length <= expectedSegments.length) {
                for (int i = 0; i < foundSegments.length; i++) {
                    if (!foundSegments[i].equals(expectedSegments[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

}
