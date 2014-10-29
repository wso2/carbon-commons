/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.url.mapper.internal;

import org.apache.catalina.Host;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.url.mapper.internal.exception.UrlMapperException;
import org.wso2.carbon.url.mapper.internal.util.HostUtil;

import java.util.List;

/**
 * To handle hot update of webApps when they update in web-app
 * mgt these methods are getting called.
 */

public class HotUpdateManager implements HotUpdateService {
    private static final Log log = LogFactory.getLog(HotUpdateManager.class);

    public List<String> getMappigsPerWebapp(String webAppName) {
        try {
            return HostUtil.getMappingsPerWebApp(webAppName);
        } catch (UrlMapperException e) {
            log.error("error while retrieving from registry", e);
        }
        return null;
    }

    public void deleteHost(String hostName) {
        try {
            HostUtil.deleteResourceToRegistry(hostName);
        } catch (UrlMapperException e) {
            log.error("error while deleting host for " + hostName, e);
        }
    }

    public Host addHost(String hostName) {
        return HostUtil.addHostToEngine(hostName);
    }

    public String getApplicationContextForHost(String hostName) {
        try {
            return HostUtil.getApplicationContextForHost(hostName);
        } catch (UrlMapperException e) {
            log.error("error while getting webapp for host", e);
        }
        return null;
    }

    public void removeHost(String hostName) {
        try {
            HostUtil.removeHost(hostName);
        } catch (UrlMapperException e) {
            log.error("error while removing host for " + hostName, e);
        }
    }

    public void deleteServiceMapping(String epr) {
        HostUtil.deleteServiceMapping(epr);
    }

    public void removeServiceMapping(String epr) {
        HostUtil.removeServiceMapping(epr);
    }

    public void addServiceMapping(String epr) {
        HostUtil.addServiceMapping(epr);
    }
    
    public String getSuffixOfHost() {
        return HostUtil.getUrlSuffix();
    }
}
