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
package org.wso2.carbon.discovery.cxf.listeners;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scannotation.AnnotationDB;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.cxf.APIScanner;
import org.wso2.carbon.discovery.cxf.CXFServiceInfo;
import org.wso2.carbon.discovery.cxf.CxfMessageSender;
import org.wso2.carbon.discovery.cxf.util.CarbonAnnotationDB;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TomcatCxfDiscoveryListener implements org.apache.catalina.LifecycleListener {

    private ServletContext context = null;
    private String cxfServletClass = "org.apache.cxf.transport.servlet.CXFServlet";

    private static final String httpPort = "mgt.transport.http.port";
    private static final String hostName = "carbon.local.ip";

    private Log log = LogFactory.getLog(TomcatCxfDiscoveryListener.class);

    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        try {
            String type = lifecycleEvent.getType();
            if (Lifecycle.AFTER_START_EVENT.equals(type) ||
                    Lifecycle.BEFORE_STOP_EVENT.equals(type)) {
                StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
                String jaxServletMapping = null;

                boolean isJaxWebapp = false;
                Map<String, ? extends ServletRegistration> servletRegs = context.getServletContext().getServletRegistrations();
                for (ServletRegistration servletReg : servletRegs.values()) {
                    if (cxfServletClass.equals(servletReg.getClassName())) {
                        Object[] mappings = servletReg.getMappings().toArray();
                        jaxServletMapping = mappings.length > 0 ? getServletContextPath((String)mappings[0]) : null;
                        isJaxWebapp = true;
                        break;
                    }
                }

                if (isJaxWebapp) {
                    CXFServiceInfo serviceBean = getServiceInfo(context, jaxServletMapping);
                    if (serviceBean == null) {
                        return;
                    }

                    if(Lifecycle.AFTER_START_EVENT.equals(type)) {
                        new CxfMessageSender().sendHello(serviceBean, null);

                    } else if (Lifecycle.BEFORE_STOP_EVENT.equals(type))  {
                        new CxfMessageSender().sendBye(serviceBean, null);
                    }
                }
            }

        } catch (DiscoveryException e) {
            log.warn("Error while publishing the services to the discovery service ", e);
        } catch (Throwable e) {
            log.warn("Error while publishing the services to the discovery service ", e);
        }
    }

    private String getServletContextPath(String jaxServletPattern) {
        if (!"".equals(jaxServletPattern) && jaxServletPattern != null) {
            if (jaxServletPattern.endsWith("/*")) {
                jaxServletPattern = jaxServletPattern.substring(0, jaxServletPattern.length() - 2);
            }
            if (jaxServletPattern.startsWith("/")) {
                jaxServletPattern = jaxServletPattern.substring(1);
            }
        } else {
            jaxServletPattern = "services";
        }

        return jaxServletPattern;

    }

    private CXFServiceInfo getServiceInfo(StandardContext context, String jaxServletMapping) {
        CXFServiceInfo serviceInfo = new CXFServiceInfo();
        String contextPath = context.getServletContext().getContextPath();
        serviceInfo.setServiceName(contextPath);
        serviceInfo.setType(getPortType(context));
        serviceInfo.setTenantDomain(PrivilegedCarbonContext.getThreadLocalCarbonContext().
                getTenantDomain(true));

        List<String> endpoints = new ArrayList<String>(5);
        try {
            InputStream configStream = getConfigLocation(context.getServletContext());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder b = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = b.parse(configStream); //doc.getDomConfig().setParameter();

            NodeList endpointElements = doc.getElementsByTagNameNS("http://cxf.apache.org/jaxws", "endpoint");
            NodeList serverElements = doc.getElementsByTagNameNS("http://cxf.apache.org/jaxws", "server");

            for (int i = 0; i < endpointElements.getLength(); i++) {
                Node node = endpointElements.item(i);
                if (node instanceof Element) {
                    Element endpointElement = (Element) node;
                    String cxfEndpoint = endpointElement.getAttribute("address");
                    endpoints.add(cxfEndpoint);
                }
            }

            for (int i = 0; i < serverElements.getLength(); i++) {
                Node node = serverElements.item(i);
                if (node instanceof Element) {
                    Element serverElement = (Element) node;
                    String cxfEndpoint = serverElement.getAttribute("address");
                    endpoints.add(cxfEndpoint);
                }
            }
        } catch (ParserConfigurationException e) {
            log.error("Error processing CXF config file of " + contextPath, e);
        } catch (SAXException e) {
            log.error("Error processing CXF config file of " + contextPath, e);
        } catch (IOException e) {
            log.error("Error processing CXF config file of " + contextPath, e);
        }

        if (endpoints.isEmpty()) {
            return null;
        }

        serviceInfo.setWsdlURI(getWsdlUri(context, jaxServletMapping, endpoints));
        serviceInfo.setxAddrs(getxAddrs(context, jaxServletMapping, endpoints));

        return serviceInfo;

    }

    private QName getPortType(StandardContext context) {
        try {
            String sei = null;        //service endpoint interface
            CarbonAnnotationDB annotations = APIScanner.getAnnotatedClasses(context, javax.jws.WebService.class);
            Set<String> set = annotations.getAnnotationIndex().get(javax.jws.WebService.class.getName());
            annotations.crossReferenceImplementedInterfaces();
            Map<String, Set<String>> implementsMap = annotations.getImplementsIndex();
            if (set == null || set.isEmpty()) {
                return null;
            }
            for (String clazzName : set) {
                Set serviceImplements;
                if ((serviceImplements = implementsMap.get(clazzName)) != null) {
                    sei = (String) serviceImplements.toArray()[0];
                    break;
                } else {
                    sei = clazzName;
                }
            }
            if (sei == null ) {
                sei = (String) set.toArray()[0];
            }
            String portType;
            String targetNamespace;

            portType = sei.substring(sei.lastIndexOf('.') + 1);
            targetNamespace = getTargetNamespace(sei);

            return new QName(targetNamespace, portType);

        } catch (AnnotationDB.CrossReferenceException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    private String getTargetNamespace(String sei) {
        String[] arr = sei.substring(0, sei.lastIndexOf('.')).split("\\.");
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        for(int i = arr.length-1; i >= 0; i--) {
            sb.append(arr[i]).append(".");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("/");
        return sb.toString();

    }

    private String getWsdlUri(StandardContext context, String jaxServletMapping, List<String> endpoints) {

        String wsdlEndpoint =  "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort) +
                context.getServletContext().getContextPath()  + "/" + jaxServletMapping + endpoints.get(0) + "?wsdl";

        return wsdlEndpoint;
    }

    private List getxAddrs(StandardContext context, String jaxServletMapping, List<String> endpoints) {
        String endpoint = "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort) +
                context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoints.get(0);
        List xAddrs = new ArrayList();
        xAddrs.add(endpoint);

        return xAddrs;
    }

    private InputStream getConfigLocation(ServletContext context) throws IOException {

        String configLocation = context.getInitParameter("config-location");
        if (configLocation == null) {
            try {
                InputStream is = context.getResourceAsStream("/WEB-INF/cxf-servlet.xml");
                if (is != null && is.available() > 0) {
                    is.close();
                    configLocation = "/WEB-INF/cxf-servlet.xml";
                }
            } catch (Exception ex) {
                //ignore
            }
        }

        return context.getResourceAsStream(configLocation);

    }
}
