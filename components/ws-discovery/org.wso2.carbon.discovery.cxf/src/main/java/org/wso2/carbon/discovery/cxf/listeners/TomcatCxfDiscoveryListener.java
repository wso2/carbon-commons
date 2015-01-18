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
import org.wso2.carbon.discovery.cxf.CXFServiceInfo;
import org.wso2.carbon.discovery.cxf.CxfMessageSender;
import org.wso2.carbon.discovery.cxf.util.CarbonAnnotationDB;
import org.wso2.carbon.discovery.cxf.util.ClassAnnotationScanner;
import org.xml.sax.SAXException;

import javax.jws.WebService;
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

    private String cxfServletClass = "org.apache.cxf.transport.servlet.CXFServlet";

    private static final String httpPort = "mgt.transport.http.port";
    private static final String hostName = "carbon.local.ip";
    CxfMessageSender cxfMessageSender = new CxfMessageSender();

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
                        cxfMessageSender.sendHello(serviceBean, null);

                    } else if (Lifecycle.BEFORE_STOP_EVENT.equals(type))  {
                        cxfMessageSender.sendBye(serviceBean, null);
                    }
                }
            }

        } catch (DiscoveryException e) {
            log.warn("Error while publishing the services to the discovery service ", e);
        } catch (Throwable e) {
            //Catching throwable since this listener's state shouldn't affect the webapp deployment.
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

    /**
     * Get JAX-WS service info needed to send the WS-Discovery message
     * TODO: Read the service class from cxf-servlet.xml instead of traversing all the classes
     * for @WebService annotation.
     */
    private CXFServiceInfo getServiceInfo(StandardContext context, String jaxServletMapping) throws DiscoveryException {
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
                    cxfEndpoint = cxfEndpoint.trim();
                    cxfEndpoint = cxfEndpoint.startsWith("/") ?
                                  cxfEndpoint.substring(1, cxfEndpoint.length()) :
                                  cxfEndpoint;
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

    /**
     * Get the JAX-WS service port type. A port type is identified by the targetNamespace
     * of the service and the port name.
     * TODO: This needs to take care of scenarias where multiple jax-ws services exposed from the same webapp
     */
    private QName getPortType(StandardContext context) throws DiscoveryException {
        QName seiInfo = null;
        try {
//            String sei = null;        //service endpoint interface
            CarbonAnnotationDB annotations = ClassAnnotationScanner.getAnnotatedClasses(context);
            Set<String> set = annotations.getAnnotationIndex().get(javax.jws.WebService.class.getName());
            annotations.crossReferenceImplementedInterfaces();
            //map classes with its interface
            Map<String, Set<String>> implementsMap = annotations.getImplementsIndex();
            if (set == null || set.isEmpty()) {
                return null;
            }

            for (String clazzName : set) {
                Class<?> clazz = context.getServletContext().getClassLoader().loadClass(clazzName);
                //don't process if it's the sei interface. we can check sei interface through the sei class
                if (clazz.isInterface()) {
                    continue;
                }

                seiInfo = processClazz(clazz, context);
                break;
            }
        } catch (AnnotationDB.CrossReferenceException e) {
            throw new DiscoveryException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new DiscoveryException(e.getMessage(), e);
        }

        return seiInfo;
    }

    /**
     * Retrieve targetNamespace and name from the @WebService annotation in jax-ws resource class.
     * First read the SEI class, and if any of the elements are empty, then check the SEI interface as well.
     */
    private QName processClazz(Class<?> clazz, StandardContext context)
            throws DiscoveryException, ClassNotFoundException {

        WebService jwsAnnotation = clazz.getAnnotation(javax.jws.WebService.class);
        String targetNamespace = jwsAnnotation.targetNamespace();   // ex. http://apache.org/handlers
        String name = jwsAnnotation.name();
        String endpointInterfaceName = jwsAnnotation.endpointInterface();

        Class<?> endpointInterface;
        if (endpointInterfaceName != null && !endpointInterfaceName.trim().isEmpty()) {
            endpointInterface = context.getServletContext().getClassLoader().loadClass(endpointInterfaceName);
        } else {
            Class<?>[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                endpointInterface = interfaces[0];
            } else {
            // A jax-ws resource class must have an implemented interface of should have the
            // endpointInterfaceName element defined
                String msg = "The endpointInterfaceName is not defined and the resource class " +
                "does not implement one - " + clazz.getName();
                log.error(msg);
                throw new DiscoveryException(msg);
            }
        }

        //if targetNamespace or name is empty,try to extract the info from SEI interface
        WebService eiJwsAnnotation = endpointInterface.getAnnotation(javax.jws.WebService.class);
        if (targetNamespace == null || targetNamespace.trim().isEmpty()) {
            targetNamespace = eiJwsAnnotation.targetNamespace();
        }
        if (name == null || name.trim().isEmpty()) {
            name = eiJwsAnnotation.name();
        }

        if (targetNamespace == null || targetNamespace.trim().isEmpty()) {
            targetNamespace = generateTargetNsFromInterfaceName(endpointInterface.getName());
        }

        if (name == null || name.trim().isEmpty()) {
            String tmpInterfaceName = endpointInterface.getName();
            name = tmpInterfaceName.substring(tmpInterfaceName.lastIndexOf('.') + 1);
        }

        return new QName(targetNamespace, name);
    }

    /**
     * Generate targetNamespace as per jax-ws 2.2 specification chapter 3.2.
     *
     * 1. The package name is tokenized using the “.” character as a delimiter.
     * 2. The order of the tokens is reversed.
     * 3. The value of the targetNamespace attribute is obtained by concatenating "http://"
     * to the list of tokens separated by "." and "/".
     *
     * @param sei fully qualified sei interface name
     * @return targetNamespace
     */
    private String generateTargetNsFromInterfaceName(String sei) {
        String[] arr = sei.substring(0, sei.lastIndexOf('.')).
                split("\\.");
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
        //todo get hostname and port using carbon apis.
        String wsdlEndpoint =  "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort) +
                context.getServletContext().getContextPath()  + "/" + jaxServletMapping + endpoints.get(0) + "?wsdl";

        return wsdlEndpoint;
    }

    private List getxAddrs(StandardContext context, String jaxServletMapping, List<String> endpoints) {
        List<String> xAddrs = new ArrayList<String>();

        String httpEndpoint = "http://" + System.getProperty(hostName) + ":" + System.getProperty(httpPort) +
                context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoints.get(0);
        String httpsEndpoint = "https://" + System.getProperty(hostName) + ":" + System.getProperty(httpsPort) +
                context.getServletContext().getContextPath() + "/" + jaxServletMapping + endpoints.get(0);

        xAddrs.add(httpEndpoint);
        xAddrs.add(httpsEndpoint);

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

    /**
     * Class that contains the Service Endpoint Interface info
     */
    private class SeiInfo {
        String wsdlPortType;
        String targetNamespace;

        public SeiInfo(String wsdlPortType, String targetNamespace) {
            this.wsdlPortType = wsdlPortType;
            this.targetNamespace = targetNamespace;
        }
    }
}
