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
package org.wso2.carbon.event.core.internal.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.event.core.EventBundleNotificationService;
import org.wso2.carbon.event.core.internal.util.EventBrokerHolder;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

@Deprecated
@Component(
        name = "eventbrokerbuilder.component",
        immediate = true)
public class EventBrokerBuilderDS {

    private static final Log log = LogFactory.getLog(EventBrokerBuilderDS.class);

    private EventBrokerHandler eventBrokerHandler;

    /**
     * initialize the cep service here.
     *
     * @param context
     */
    @Activate
    protected void activate(ComponentContext context) {

        this.eventBrokerHandler = new EventBrokerHandler(context);
        // need to differ the bundle deployment if the Qpid bundle is in the plugins directory and it is not
        // started
        boolean isQpidBundlePresent = false;
        final BundleContext bundleContext = context.getBundleContext();
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.wso2.carbon.qpid")) {
                isQpidBundlePresent = true;
                break;
            }
        }
        if (isQpidBundlePresent) {
            // if the Qpid bundle is present we register an event broker handler
            // so that the qpid compoent will notify that.
            context.getBundleContext().registerService(EventBundleNotificationService.class.getName(), this
                    .eventBrokerHandler, null);
        } else {
            this.eventBrokerHandler.startEventBroker();
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

    }

    @Reference(
            name = "registry.service",
            service = org.wso2.carbon.registry.core.service.RegistryService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {

        EventBrokerHolder.getInstance().registerRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {

    }

    @Reference(
            name = "realm.service",
            service = org.wso2.carbon.user.core.service.RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {

        EventBrokerHolder.getInstance().registerRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

    }

    @Reference(
            name = "configurationcontext.service",
            service = org.wso2.carbon.utils.ConfigurationContextService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {

        EventBrokerHolder.getInstance().registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {

    }
}
