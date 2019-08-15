/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.view.internal;

import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.logging.view.appenders.LoggingAppender;

import java.util.Hashtable;

/**
 * Log Viewer Service Component.
 */
@Component(name = "org.wso2.carbon.logging.view.component", immediate = true)
public class CarbonLoggingViewServiceComponent {

    @Activate
    protected void activate(ComponentContext componentContext) {

        LoggingAppender loggingAppender = new LoggingAppender(DataHolder.getInstance().getLogBuffer());
        Hashtable<String, Object> props = new Hashtable();
        props.put("org.ops4j.pax.logging.appender.name", "InMemoryLogAppender");
        componentContext.getBundleContext().registerService(PaxAppender.class, loggingAppender, props);
    }

}
