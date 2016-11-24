/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.tools.wsdlvalidator.test.constants;

import org.wso2.carbon.tools.wsdlvalidator.test.models.XXEResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for maintaining the constants for XXE XML resources.
 */
public class XXEResourceConstants {

    private XXEResourceConstants() {

    }

    /**
     * Resource list for XXE XML injection resources
     */
    public static final List<XXEResource> XXE_RESOURCE_LIST = Collections
            .unmodifiableList(new ArrayList<XXEResource>() {
                {
                    add(new XXEResource("filenotfound.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("ftp.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("insidesoapbody.xml", ExceptionString.INSIDE_SOAP_BODY_EXCEPTION));
                    add(new XXEResource("onbextraction1.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbextraction2.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbextraerror.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbextranice.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbvariation.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("outboundxxe.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("validwsdl.xml", ExceptionString.VALID_WSDL_EXCEPTION));
                    add(new XXEResource("wafbypass.xml", ExceptionString.SAX_ERROR_PROCESSING_XML));
                }
            });

    /**
     * Exception constants for resource files.
     */
    private static class ExceptionString {

        private static final String INSIDE_SOAP_BODY_EXCEPTION = "WSDLException (at /soap:Envelope): " +
                "faultCode=INVALID_WSDL: Expected element '{http://schemas.xmlsoap.org/wsdl/}definitions'.";
        private static final String VALID_WSDL_EXCEPTION = null;
        private static final String SAX_ERROR_PROCESSING_XML = "SAX error in processing XML document";
    }
}
