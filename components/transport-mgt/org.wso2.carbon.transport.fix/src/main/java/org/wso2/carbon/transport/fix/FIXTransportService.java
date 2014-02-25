/*
 * Copyright 2005-2008 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.transport.fix;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.core.transports.AbstractTransportService;
import org.wso2.carbon.core.transports.util.TransportParameter;

/**
 * 
 */
public class FIXTransportService extends AbstractTransportService {

    public static final String TRANSPORT_NAME = "fix";
	public static final String TRANSPORT_CONF = "fix-transports.xml";

	/**
	 * Instantiates FIXTransportService with a reference to the AxisConfiguration.
	 */
	public FIXTransportService() {
		super(TRANSPORT_NAME);
	}

    public boolean dependenciesAvailable(TransportParameter[] params) {
        try {
            Class.forName("quickfix.Application");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}