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
package org.wso2.carbon.wsdl2form.test.constants;

import org.wso2.carbon.wsdl2form.test.models.XXEResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class for maintaining the org.wso2.carbon.wsdl2form.test.constants for XXE XML resources.
 */
public class XXEResourceConstants {

    private static final String INSIDE_SOAP_BODY_EXCEPTION = null;
    private static final String VALID_WSDL_EXCEPTION = null;
    private static final String SAX_ERROR_PROCESSING_XML = "SAX error in processing XML document";

    private XXEResourceConstants() {
    }

    /**
     * Resource list for XXE XML injection resources
     */
    public static final List<XXEResource> XXE_RESOURCE_LIST = Collections
            .unmodifiableList(new ArrayList<XXEResource>() {
                {
                    add(new XXEResource("filenotfound.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("ftp.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("insidesoapbody.xml", INSIDE_SOAP_BODY_EXCEPTION));
                    add(new XXEResource("onbextraction1.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbextraction2.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbextraerror.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbextranice.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("onbvariation.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("outboundxxe.xml", SAX_ERROR_PROCESSING_XML));
                    add(new XXEResource("validwsdl.xml", VALID_WSDL_EXCEPTION));
                    add(new XXEResource("wafbypass.xml", SAX_ERROR_PROCESSING_XML));
                }
            });
}
