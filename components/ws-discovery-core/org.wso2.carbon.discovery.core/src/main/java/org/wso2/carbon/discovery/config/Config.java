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

package org.wso2.carbon.discovery.config;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.discovery.DiscoveryConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Config {

    private List<String> scopes;
    private String uniqueId;
    private int metadataVersion;

    public Config() {
        this.scopes = new ArrayList<String>();
    }

    public static Config fromOM(OMElement omElement) {
        Config config = new Config();

        OMElement childElement = null;
        for (Iterator iter = omElement.getChildElements(); iter.hasNext();) {
            childElement = (OMElement) iter.next();
            if (childElement.getLocalName().equals(DiscoveryConstants.CONFIG_SCOPES)) {
                String scopesString = childElement.getText();
                String[] scopeNames = scopesString.split(" ");
                for (String scope : scopeNames) {
                    config.addScope(scope);
                }

            } else if (childElement.getLocalName().equals(DiscoveryConstants.CONFIG_METADATA_VERSION)) {
                config.setMetadataVersion(Integer.parseInt(childElement.getText()));
            } else if (childElement.getLocalName().equals(DiscoveryConstants.CONFIG_UNIQUE_ID)) {
                config.setUniqueId(childElement.getText());
            }
        }

        return config;
    }

    public void addScope(String scope) {
        this.scopes.add(scope);
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getMetadataVersion() {
        return metadataVersion;
    }

    public void setMetadataVersion(int metadataVersion) {
        this.metadataVersion = metadataVersion;
    }
}
