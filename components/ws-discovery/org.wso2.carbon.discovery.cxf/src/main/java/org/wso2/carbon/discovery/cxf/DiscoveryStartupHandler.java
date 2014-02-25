/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.discovery.cxf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.discovery.DiscoveryException;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryStartupHandler implements ServerStartupHandler {

    private static final Log log = LogFactory.getLog(ServerStartupHandler.class);

    protected static List<CXFServiceInfo> initialMessagesList = new ArrayList<CXFServiceInfo>();

    public void invoke() {

            CxfMessageSender messageSender = new CxfMessageSender();
            for (CXFServiceInfo message : initialMessagesList) {
                try {
                    messageSender.sendHello(message, null);
                } catch (DiscoveryException e) {
                    log.error("Error sending WS-Discovery Hello message to DiscoveryProxy for " +
                            message.getServiceName());
                }

            }


    }

    protected static void queueMessage(CXFServiceInfo serviceBean) {
        initialMessagesList.add(serviceBean);
    }
}
