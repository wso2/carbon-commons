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

package org.wso2.carbon.tools.wsdlvalidator.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.tools.wsdlvalidator.WSDLValidationInfo;
import org.wso2.carbon.tools.wsdlvalidator.WsdlValidator;
import org.wso2.carbon.tools.wsdlvalidator.test.constants.XXEResourceConstants;
import org.wso2.carbon.tools.wsdlvalidator.test.models.XXEResource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Class for validating WSDLValidator against XXE attacks.
 */
public class WSDLValidatorXXETest {

    /**
     * WSDL Validator reference holder.
     */
    private WsdlValidator wsdlValidator;

    /**
     * Initialise WSDL validation.
     */
    @BeforeClass
    public void init() {

        wsdlValidator = new WsdlValidator();
    }

    /**
     * Test for provided XXE injections.
     *
     * @throws IOException on obtaining stream
     */
    @Test
    public void testXMLXXEValidation() throws IOException {

        for (XXEResource xxeResource : XXEResourceConstants.XXE_RESOURCE_LIST) {
            InputStream inputStream = xxeResource.getURLForFile().openStream();
            try {
                invokePrivateMethodValidaWSDLFromURI(inputStream);
            } catch (Exception e) {
                Assert.assertTrue(xxeResource.getExpectedException()
                        .equals(((InvocationTargetException) e).getTargetException().getMessage()));
                continue;
            }

            // Fail the test when an exception is expected but didn't throw
            if (xxeResource.getExpectedException() != null) {
                Assert.fail();
            }
        }
    }

    /**
     * Invoke validaWSDLFromURI from WSDLValidator.
     *
     * @param inputStream file input stream.
     * @return WSDLValidationInfo
     * @throws NoSuchMethodException     When method not found
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private WSDLValidationInfo invokePrivateMethodValidaWSDLFromURI(InputStream inputStream)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = WsdlValidator.class.getDeclaredMethod("validaWSDLFromURI", InputStream.class);
        method.setAccessible(true);
        return (WSDLValidationInfo) method.invoke(wsdlValidator, inputStream);
    }
}
