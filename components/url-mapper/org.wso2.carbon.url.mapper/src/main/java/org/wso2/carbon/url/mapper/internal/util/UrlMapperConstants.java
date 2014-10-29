/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
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

package org.wso2.carbon.url.mapper.internal.util;

public final class UrlMapperConstants {

	public final static String SERVICE_URL_PATTERN =  "/services";
    public final static String JAX_SERVLET = "JAXServlet";
    public static final class HostProperties {
        public static final String HOST_NAME = "host.name";
        public static final String HOSTINFO = "hostinfo/";

        public static final String JAX_APP="jaxWebapp";
        public static final String JAGGERY_APP = "jaggeryWebapp";
        public static final String WEBAPP = "webapp";

        public static final String JAX_WEBAPPS="jaxwebapps";
        public static final String JAGGERY_APPS = "jaggeryapps";
        public static final String SERVICE = "services" ;
        public static final String WEB_APPS = "webapps";

        public static final String WAR = ".war";
        public static final String FILE_SERPERATOR = "/";
        public static final String WEB_APP = "web.app";
        public static final String TENANT_DOMAIN = "tenant.domain";
        public static final String SERVICE_EPR="service.epr";
        public static final String APP_NAME = "app.name";
        public static final String JAGGERY_LISTENER = "JaggeryConfListener";
    }
    
    public static final class MappingConfigs {
    	public static final String PREFIX= "prefix";
    	public static final String MAPPINGS = "mappings";
    	 public static final String ETC ="etc";
    	public static final String MAPPING_CONF_FILE = "url-mapping-config.xml";
    }
}
