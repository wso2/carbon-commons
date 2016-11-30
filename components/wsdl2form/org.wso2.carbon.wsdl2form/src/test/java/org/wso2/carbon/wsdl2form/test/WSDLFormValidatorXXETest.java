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

package org.wso2.carbon.wsdl2form.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.wsdl2form.Util;
import org.wso2.carbon.wsdl2form.test.constants.XXEResourceConstants;
import org.wso2.carbon.wsdl2form.test.models.XXEResource;

/**
 * Class for validating Util against XXE attacks.
 */
public class WSDLFormValidatorXXETest {

    /**
     * Test for provided XXE injections.
     */
    @Test
    public void testXMLXXEValidation() {
        for (XXEResource xxeResource : XXEResourceConstants.XXE_RESOURCE_LIST) {
            String url = xxeResource.getURLForFile().toString();
            try {
                Util.writeWSDLToFileSystemHelpler(url);
            } catch (Exception e) {
                Assert.assertTrue(xxeResource.getExpectedException()
                        .equals(e.getMessage()));
                continue;
            }
            if (xxeResource.getExpectedException() != null) {
                Assert.fail("WSDL file validation should throw an exception since invalid WSDL");
            }
        }
    }
}
