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
package org.wso2.carbon.cluster.mgt.admin;

/**
 *
 */
public class GroupMember {
    private String hostName;
    private int httpPort;
    private int httpsPort;
    private String mgConsoleURL;
    private String backendServerURL;

    public GroupMember() {
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public String getMgConsoleURL() {
        return mgConsoleURL;
    }

    public void setMgConsoleURL(String mgConsoleURL) {
        this.mgConsoleURL = mgConsoleURL;
    }

    public String getBackendServerURL() {
        return backendServerURL;
    }

    public void setBackendServerURL(String backendServerURL) {
        this.backendServerURL = backendServerURL;
    }
}
