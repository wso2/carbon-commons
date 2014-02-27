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

package org.wso2.carbon.databridge.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.databridge.core.DataBridge;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.DataBridgeServiceValueHolder;
import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.definitionstore.AbstractStreamDefinitionStore;
import org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.databridge.core.internal.authentication.CarbonAuthenticationHandler;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeCoreBuilder;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="databridge.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 */
public class DataBridgeDS {
    private static final Log log = LogFactory.getLog(DataBridgeDS.class);
    private AuthenticationService authenticationService;
    private ServiceRegistration receiverServiceRegistration;
    private ServiceRegistration subscriberServiceRegistration;
    private DataBridge databridge;
    private OMElement initialConfig;
    private ServiceRegistration databridgeRegistration;

    /**
     * initialize the agent server here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            DataBridgeConfiguration dataBridgeConfiguration = new DataBridgeConfiguration();
            List<String[]> streamDefinitions = new ArrayList<String[]>();
            try {
                initialConfig = DataBridgeCoreBuilder.loadConfigXML();
            } catch (DataBridgeConfigurationException e) {
                log.error("The data Bridge config was not found. Falling back to defaults.");
            }
            DataBridgeCoreBuilder.populateConfigurations(dataBridgeConfiguration, streamDefinitions, initialConfig);

            if (databridge == null) {
                String definitionStoreName = dataBridgeConfiguration.getStreamDefinitionStoreName();
                AbstractStreamDefinitionStore streamDefinitionStore = null;
                try {
                    streamDefinitionStore = (AbstractStreamDefinitionStore) DataBridgeDS.class.getClassLoader().loadClass(definitionStoreName).newInstance();

                    if (definitionStoreName.equals(DataBridgeConstants.DEFAULT_DEFINITION_STORE)) {
                        log.warn("The default stream defintion store is loaded : " + definitionStoreName + ". Please configure a proper definition store.");
                    }
//                    streamDefinitionStore = (AbstractStreamDefinitionStore) Class.forName(definitionStoreName).newInstance();
                } catch (Exception e) {
                    log.warn("The stream definition store :" + definitionStoreName + " cannot be created. Hence using " + DataBridgeConstants.DEFAULT_DEFINITION_STORE, e);
                    //by default if used InMemoryStreamDefinitionStore
                    streamDefinitionStore = new InMemoryStreamDefinitionStore();
                }


                databridge = new DataBridge(new CarbonAuthenticationHandler(authenticationService), streamDefinitionStore, dataBridgeConfiguration);
                databridge.setInitialConfig(initialConfig);

                //todo
//                for (String[] streamDefinition : streamDefinitions) {
//                    try {
//                        dataReceiver.saveStreamDefinition(streamDefinition[0], streamDefinition[1]);
//                    } catch (MalformedStreamDefinitionException e) {
//                        log.error("Malformed Stream Definition for " + streamDefinition[0] + ": " + streamDefinition[1], e);
//                    } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
//                        log.warn("Redefining event stream of " + streamDefinition[0] + ": " + streamDefinition[1], e);
//                    } catch (RuntimeException e) {
//                        log.error("Error in defining event stream " + streamDefinition[0] + ": " + streamDefinition[1], e);
//                    }
//                }
                receiverServiceRegistration = context.getBundleContext().
                        registerService(DataBridgeReceiverService.class.getName(), databridge, null);
                subscriberServiceRegistration = context.getBundleContext().
                        registerService(DataBridgeSubscriberService.class.getName(), databridge, null);
//                databridgeRegistration =
//                        context.getBundleContext().registerService(DataBridge.class.getName(), databridge, null);
                log.info("Successfully deployed Agent Server ");
            }
        } catch (DataBridgeConfigurationException e) {
            log.error("Agent Server Configuration is not correct hence can not create and start Agent Server ", e);
        } catch (RuntimeException e) {
            log.error("Error in starting Agent Server ", e);
        }
    }


    protected void deactivate(ComponentContext context) {
        context.getBundleContext().ungetService(receiverServiceRegistration.getReference());
        context.getBundleContext().ungetService(subscriberServiceRegistration.getReference());
//        databridgeRegistration.unregister();
        if (log.isDebugEnabled()) {
            log.debug("Successfully stopped agent server");
        }
    }

    protected void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    protected void unsetAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = null;
    }

    protected void setRealmService(RealmService realmService) {
        DataBridgeServiceValueHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        DataBridgeServiceValueHolder.setRealmService(null);
    }

}
