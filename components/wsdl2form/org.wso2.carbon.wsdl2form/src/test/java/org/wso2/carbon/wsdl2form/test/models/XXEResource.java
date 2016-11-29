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
package org.wso2.carbon.wsdl2form.test.models;

import java.io.File;
import java.net.URL;

/**
 * Class to denote a XXE XML injection resource.
 */
public class XXEResource {

    private String fileName;
    private String expectedException;

    public XXEResource(String fileName, String expectedException) {
        this.fileName = fileName;
        this.expectedException = expectedException;
    }

    /**
     * Get resource URL for file.
     *
     * @return URL of the xml file
     */
    public URL getURLForFile() {
        String xxeResourceFolderName = "xxe";
        return getClass().getClassLoader().getResource(xxeResourceFolderName + File.separator + fileName);
    }

    /**
     * Get the expected exception string.
     *
     * @return exception string
     */
    public String getExpectedException() {
        return expectedException;
    }
}
