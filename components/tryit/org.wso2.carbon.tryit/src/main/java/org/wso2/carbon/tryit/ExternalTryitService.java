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
package org.wso2.carbon.tryit;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.wsdl2form.WSDL2FormGenerator;

/**
 * ExternalTryitService will be used in generating AJAX based application for a given WSDL. Given WSDL
 * could be either version 1.1 or 2.0. If the given WSDL is version 1.1, it will be converted to
 * version 2.0 using wsdl11to20.xsl. This conversion is absolute necessary due to usage of dyanmic-
 * codegen project.
 */
public class ExternalTryitService extends AbstractAdmin {

    /**
     * This is a Web method. A URL of a WSDL document is given and the ID of the generated AJAX
     * app will be return. User need to do a HTTP GET on filedownload?id=<genid> to get the XHTML
     * app.
     *
     * @param url WSDL document location.
     * @param hostName name of client host.
     * @throws org.apache.axis2.AxisFault if any anomaly occured.
     */
    public String generateTryit(String url, String hostName) throws AxisFault {
        try {
            return WSDL2FormGenerator.getInstance().getExternalTryit(url, null,
                                                                     null, null, hostName, getConfigContext());
        } catch(CarbonException e) {
            throw new AxisFault(e.getCause().getMessage());
        }      
    }
}