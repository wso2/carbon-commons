/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.util;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.reporting.api.ReportingException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class XMLDataSourceFields {
    public String[] getAllFields(String file) throws ReportingException {
        List<String> names = new ArrayList<String>();
        OMElementHandler omElementHandler = new OMElementHandler();
        try {
            OMElement omElement = omElementHandler.createOMElement(file);
            OMElement element = omElement.getFirstElement();
            names.add(element.getLocalName());
            Iterator<OMElement> iterator = element.getChildElements();
            while (iterator.hasNext()) {
                names.add(element.getLocalName());
            }

        } catch (ReportingException e) {
            throw new ReportingException("OMElement creation fail from " + file);

        }
        return names.toArray(new String[names.size()]);
    }
}
