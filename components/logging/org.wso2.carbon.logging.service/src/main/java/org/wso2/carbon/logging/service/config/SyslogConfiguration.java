/*
 * Copyright 2005,2014 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.logging.service.config;

public class SyslogConfiguration {

    private String syslogHostURL = "";
    private String port = "";
    private String realm = "";
    private String userName = "";
    private String password = "";
    private String syslogLogPattern = "";
    private boolean isSyslogOn = true;

    public String getSyslogHostURL() {
        return syslogHostURL;
    }

    public void setSyslogHostURL(String syslogHostURL) {
        this.syslogHostURL = syslogHostURL;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
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

    public String getSyslogLogPattern() {
        return syslogLogPattern;
    }

    public void setSyslogLogPattern(String syslogLogPattern) {
        this.syslogLogPattern = syslogLogPattern;
    }

    public boolean isSyslogOn() {
        return isSyslogOn;
    }

    public void setSyslogOn(boolean isSyslogOn) {
        this.isSyslogOn = isSyslogOn;
    }

}
