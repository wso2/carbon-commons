/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.discovery.cxf;

import javax.xml.namespace.QName;
import java.util.List;

public class CXFServiceInfo {
    private String serviceName;
    private String wsdlURI;
    private QName type;
    private List xAddrs;
    private String tenantDomain;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getWsdlURI() {
        return wsdlURI;
    }

    public void setWsdlURI(String wsdlURI) {
        this.wsdlURI = wsdlURI;
    }

    public QName getType() {
        return type;
    }

    public void setType(QName type) {
        this.type = type;
    }

    public List getxAddrs() {
        return xAddrs;
    }

    public void setxAddrs(List xAddrs) {
        this.xAddrs = xAddrs;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
