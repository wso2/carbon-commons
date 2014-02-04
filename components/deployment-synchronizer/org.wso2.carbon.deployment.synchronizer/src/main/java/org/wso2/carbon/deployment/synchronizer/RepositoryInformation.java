/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class RepositoryInformation {

    protected static final Log log = LogFactory.getLog(RepositoryInformation.class);

    private String url;
    private String userName;
    private String password;

    public RepositoryInformation (String url) {
        this.url = url;
        setUserName(null);
        setPassword(null);
    }

    public RepositoryInformation (String userName, String password) {
        this.userName = userName;
        this.password = password;
        setUrl(null);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
