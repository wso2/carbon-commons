/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.wsdl2form;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class WSDL2FormGenerator {

    public static final String TRYIT_SG_NAME = "TryItMockServiceGroup";
    private static final String PROXY_TIMER = "_PROXY_TIMER_";
    public static final String LAST_TOUCH_TIME = "_LAST_TOUCH_TIME_";
    public static final String SERVICE_NOT_FOUND = "SERVICE_NOT_FOUND";
    public static final String SERVICE_INACTIVE = "SERVICE_INACTIVE";
    public static final String UNSUPPORTED_LANG = "UNSUPPORTED_LANG";
    public static final String SUCCESS = "SUCCESSFULL";

    public static final String SERVICE_QUERY_PARAM = "service";
    public static final String OPERATION_PARAM = "operation";
    public static final String ENDPOINT_QUERY_PARAM = "endpoint";
    public static final String LANGUAGE_QUERY_PARAM = "lang";
    public static final String LOCALHOST_QUERY_PARAM = "localhost";
    public static final String CONTENT_TYPE_QUERY_PARAM = "content-type";

    /*Touch time.*/
    public final static long PERIOD = 20 * 60 * 1000;
    private static final String PORT = "port";

    private static final WSDL2FormGenerator INSTANCE = new WSDL2FormGenerator();

    private static Log log = LogFactory.getLog(WSDL2FormGenerator.class);

    public static WSDL2FormGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * @param result        Result object containing the generated form
     * @param configCtx     ConfigurationContext of the tenant. In standalone mode, this should be
     *                      main ConfigurationContext.
     * @param tryitURL      URL of the service that need to be tried out.
     * @param serviceName   Name of the service when multiple services are available in the WSDL. If
     *                      {@code serviceName} is {@code null}, then the fist service in the WSDL
     *                      will be used.
     * @param operationName Operation of the service that need to be tried out. If this is {@code
     *                      null}, all operations will be available.
     * @param endpointName  If you need to generate get the tryit form only for specific endpoint
     *                      only, then you can specify it here. It could be <i>SOAP11Endpoint,
     *                      SOAP12Endpoint</i> or <i>HTTPEndpoint etc</i>.
     * @param fullPage      set {@code true} if you want to get a full html page. {@code false} will
     *                      create a div.
     *
     * @return URL for the mocked service.
     * @throws CarbonException Throws when an error occurred during the execution
     */
    public String getInternalTryit(Result result, ConfigurationContext configCtx, String tryitURL,
                                   String serviceName, String operationName, String endpointName,
                                   boolean fullPage) throws CarbonException {
        try {
            URL url = new URL(tryitURL);
            String serviceContextRoot = configCtx.getServiceContextPath();
            String contextRoot = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");
            if (!serviceContextRoot.endsWith("/")) {
                serviceContextRoot = serviceContextRoot + "/";
            }
            if (!contextRoot.endsWith("/")) {
                contextRoot = contextRoot + "/";
            }
            AxisService axisService = getAxisService(tryitURL, serviceContextRoot, configCtx);
            if (axisService != null) {
                if (axisService.isActive()) {

                    // This is a fix for Mashup-861. If the transport is disabled we redirect the user to
                    // the alternative transport
                    if (!axisService.isEnableAllTransports() &&
                            !axisService.isExposedTransport(url.getProtocol())) {
                        String redirectURL;
                        if (ServerConstants.HTTP_TRANSPORT.equals(url.getProtocol()) &&
                                axisService.isExposedTransport(ServerConstants.HTTPS_TRANSPORT)) {
                            TransportInDescription httpsTransport = configCtx.getAxisConfiguration()
                                    .getTransportIn(ServerConstants.HTTPS_TRANSPORT);
                            Parameter parameter = httpsTransport.getParameter("proxyPort");
                            String port = "";
                            if (parameter != null && !"-1".equals(parameter.getValue())) {
                                String value = (String) parameter.getValue();
                                if (!"443".equals(value)) {
                                    port = value;
                                }
                            } else {
                                port = Integer.toString(CarbonUtils.getTransportPort(configCtx,
                                        ServerConstants.HTTPS_TRANSPORT));
                            }
                            redirectURL =
                                    ServerConstants.HTTPS_TRANSPORT + "://" + url.getHost() + ":" +
                                            port + url.getPath() + "?" + url.getQuery();
                            return redirectURL;
                        } else if (ServerConstants.HTTPS_TRANSPORT.equals(url.getProtocol()) &&
                                axisService.isExposedTransport(ServerConstants.HTTP_TRANSPORT)) {
                            TransportInDescription httpsTransport = configCtx.getAxisConfiguration()
                                    .getTransportIn(ServerConstants.HTTP_TRANSPORT);
                            Parameter parameter = httpsTransport.getParameter("proxyPort");
                            String port = "";
                            if (parameter != null && !"-1".equals(parameter.getValue())) {
                                String value = (String) parameter.getValue();
                                if (!"80".equals(value)) {
                                    port = value;
                                }
                            } else {
                                port = Integer.toString(CarbonUtils.getTransportPort(configCtx,
                                        ServerConstants.HTTP_TRANSPORT));
                            }
                            redirectURL =
                                    ServerConstants.HTTP_TRANSPORT + "://" + url.getHost() + ":" +
                                            port + url.getPath() + "?" + url.getQuery();
                            return redirectURL;
                        }
                    }

                    Map<String, String> paramMap = new HashMap<String, String>();
                    DOMSource xmlSource = Util.getSigStream(axisService, null);

                    String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(tryitURL);
                    String multitenantPrefix = contextRoot;
                    if(tenantDomain != null) {
                         multitenantPrefix = contextRoot + "t/" + tenantDomain + "/";
                    }

                    paramMap.put("js-global-params", multitenantPrefix + "carbon/global-params.js");
                    paramMap.put("proxyAddress",
                            contextRoot + "carbon/admin/jsp/WSRequestXSSproxy_ajaxprocessor.jsp");
                    paramMap.put("js-service-stub", url.getPath() + "?stub");
                    paramMap.put("services-path", serviceContextRoot);

                    Util.generateTryit(xmlSource, result, paramMap);

                    return WSDL2FormGenerator.SUCCESS;
                } else {
                    throw new CarbonException(WSDL2FormGenerator.SERVICE_INACTIVE);
                }
            } else {
                throw new CarbonException(WSDL2FormGenerator.SERVICE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (OMException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }
    }

    /**
     * Main ConfigurationContext is used here to get the serviceContext path etc. as tenant specific
     * ConfigurationContext uses local transport(when server is configured to use a proxy)
     *
     * @param tryitWSDL     URL of the service that need to be tried out.
     * @param serviceName   Name of the service when multiple services are available in the WSDL. If
     *                      {@code serviceName} is {@code null}, then the fist service in the WSDL
     *                      will be used.
     * @param operationName Operation of the service that need to be tried out. If this is {@code
     *                      null}, all operations will be available.
     * @param endpointName  If you need to generate get the tryit form only for specific endpoint
     *                      only, then you can specify it here. It could be <i>SOAP11Endpoint,
     *                      SOAP12Endpoint</i> or <i>HTTPEndpoint etc</i>.
     * @param hostName      Hostname of the front end server
     * @param configCtx     ConfigurationContext
     *
     * @return URL for the mocked service.
     * @throws CarbonException Throws when an error occurred during the execution.
     */
    public String getExternalTryit(String tryitWSDL, String serviceName, String operationName,
                                   String endpointName, String hostName, ConfigurationContext configCtx)
            throws CarbonException {

        Map fileResourcesMap =
                (Map) MessageContext.getCurrentMessageContext().getConfigurationContext()
                        .getProperty(ServerConstants.FILE_RESOURCE_MAP);
        if (fileResourcesMap == null) {
            fileResourcesMap = new Hashtable();
            MessageContext.getCurrentMessageContext().getConfigurationContext()
                    .setProperty(ServerConstants.FILE_RESOURCE_MAP, fileResourcesMap);
        }
        InputStream inXMLStream = null;
        try {
            File location = Util.writeWSDLToFileSystemHelpler(tryitWSDL);
            inXMLStream = new FileInputStream(location);
            AxisService axisService;
            WSDLToAxisServiceBuilder builder;

            XMLStreamReader streamReader =
                    XMLInputFactory.newInstance().createXMLStreamReader(inXMLStream);
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(streamReader);
            OMElement docEle = stAXOMBuilder.getDocumentElement();
            //switch
            if (docEle.getQName().getLocalPart().equals("definitions")) {
                builder = new WSDL11ToAxisServiceBuilder(new FileInputStream(location));
                builder.setBaseUri(getBaseURI(tryitWSDL));
                ((WSDL11ToAxisServiceBuilder) builder).setAllPorts(true);
            } else if (docEle.getQName().getLocalPart().equals("description")) {
                builder = new WSDL20ToAxisServiceBuilder(new FileInputStream(location), null, null);
                builder.setBaseUri(getBaseURI(tryitWSDL));
            } else {
                String msg =
                        WSDL2FormGenerator.class.getName() + " standard WSDL document is not found";
                log.error(msg);
                throw new CarbonException(msg);
            }

            axisService = builder.populateService();
            updateMockProxyServiceGroup(axisService, configCtx, MessageContext.getCurrentMessageContext()
                    .getConfigurationContext().getAxisConfiguration());
            List<String> exposeTxList = new ArrayList<String>();
            exposeTxList.add(ServerConstants.HTTP_TRANSPORT);
            axisService.setExposedTransports(exposeTxList);
            ByteArrayOutputStream wsdl2Bos = new ByteArrayOutputStream();
            if (hostName == null || hostName.length() == 0) {
                hostName = NetworkUtils.getLocalHostname();
            }

            // Endpoints are changed to those of proxy before printing WSDL to generate tryit.
            Map endpointMap = axisService.getEndpoints();
            HashMap<String, String> origUrlMap = new HashMap<String, String>();
            Set endKeySet = endpointMap.keySet();

            // Calculate endpoint for proxy service.
            TransportInDescription inDescription =
                    configCtx.getAxisConfiguration().getTransportIn(ServerConstants.HTTP_TRANSPORT);
            Parameter proxyParameter = inDescription.getParameter("proxyPort");

            String proxyEndpointPrefix = ServerConstants.HTTP_TRANSPORT + "://" + hostName;
            String proxyEndpointPort = ":" + inDescription.getParameter(PORT).getValue();

            // We check whether there is a proxyPort specified in the http Endpoint. If there is
            // one we need to use that port, but if its 80 we just ignore it.
            if (proxyParameter != null && !"-1".equals(proxyParameter.getValue())) {
                if (!"80".equals(proxyParameter.getValue())) {
                    proxyEndpointPort = ":" + proxyParameter.getValue();
                } else {
                    proxyEndpointPort = "";
                }
            }
            String serviceContextRoot = configCtx.getServiceContextPath();
            if (!serviceContextRoot.endsWith("/")) {
                serviceContextRoot = serviceContextRoot + "/";
            }
            String proxyEndpoint = proxyEndpointPrefix + proxyEndpointPort + serviceContextRoot +
                    axisService.getName() + "/";

            // Update all endpoints of proxy service.
            for (Object anEndKeySet : endKeySet) {
                String key = (String) anEndKeySet;
                AxisEndpoint ep = (AxisEndpoint) endpointMap.get(key);
                origUrlMap.put(key, ep.getEndpointURL());
                ep.setEndpointURL(proxyEndpoint);
            }


            // Original endpoints are restored in order to make call from proxy.
            for (Object anEndKeySet : endKeySet) {
                String key = (String) anEndKeySet;
                AxisEndpoint ep = (AxisEndpoint) endpointMap.get(key);
                ep.setEndpointURL(origUrlMap.get(key));
            }
            axisService.printWSDL2(wsdl2Bos, hostName);

            return generateTryitExternal(axisService, configCtx, wsdl2Bos, serviceName, operationName,
                    endpointName, fileResourcesMap, true, false);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (XMLStreamException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (OMException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } finally {
            if (inXMLStream != null) {
                try {
                    inXMLStream.close();
                } catch (IOException e) {
                    log.error("Error closing I/O streams", e);
                }
            }
        }
    }

    /**
     * This method should be used if the mocking service is an internal service. If the service is
     * an external one, use {@code getExternalMockit} method
     *
     * @param result        Result object containing the generated form
     * @param configCtx     ConfigurationContext of the tenant. In standalone mode, this should be
     *                      main ConfigurationContext.
     * @param mockitWSDL    URL of the service that need to be tried out.
     * @param serviceName   Name of the service when multiple services are available in the WSDL. If
     *                      {@code serviceName} is {@code null}, then the fist service in the WSDL
     *                      will be used.
     * @param operationName Operation of the service that need to be tried out. If this is {@code
     *                      null}, all operations will be available.
     * @param taskID        Task ID value to be sent along with form submission.
     * @param proxyUrl      Proxy url where the form response will be posted.
     * @param fullPage      set {@code true} if you want to get a full html page. {@code false} will
     *                      create a div.
     *
     * @return URL for the mocked service.
     * @throws CarbonException Throws when an error occurred during the execution
     */
    public String getInternalMockit(Result result, ConfigurationContext configCtx,
                                    String mockitWSDL, String serviceName, String operationName,
                                    String taskID, String proxyUrl, boolean fullPage)
            throws CarbonException {
        try {
            URL url = new URL(mockitWSDL);
            String serviceContextRoot = configCtx.getServiceContextPath();
            if (!serviceContextRoot.endsWith("/")) {
                serviceContextRoot = serviceContextRoot + "/";
            }

            AxisService axisService = getAxisService(mockitWSDL, serviceContextRoot, configCtx);
            if (axisService != null) {
                if (axisService.isActive()) {

                    // This is a fix for Mashup-861. If the transport is disabled we redirect the user to
                    // the alternative transport
                    if (!axisService.isEnableAllTransports() &&
                            !axisService.isExposedTransport(url.getProtocol())) {
                        String redirectURL;
                        if (ServerConstants.HTTP_TRANSPORT.equals(url.getProtocol()) &&
                                axisService.isExposedTransport(ServerConstants.HTTPS_TRANSPORT)) {
                            TransportInDescription httpsTransport = configCtx.getAxisConfiguration()
                                    .getTransportIn(ServerConstants.HTTPS_TRANSPORT);
                            Parameter parameter = httpsTransport.getParameter("proxyPort");
                            String port = "";
                            if (parameter != null && !"-1".equals(parameter.getValue())) {
                                String value = (String) parameter.getValue();
                                if (!"443".equals(value)) {
                                    port = ":" + value;
                                }
                            } else {
                                port = ":" + CarbonUtils.getTransportPort(configCtx,
                                        ServerConstants.HTTPS_TRANSPORT);
                            }
                            redirectURL =
                                    ServerConstants.HTTPS_TRANSPORT + "://" + url.getHost() + ":" +
                                            port + url.getPath() + "?" + url.getQuery();
                            return redirectURL;
                        } else if (ServerConstants.HTTPS_TRANSPORT.equals(url.getProtocol()) &&
                                axisService.isExposedTransport(ServerConstants.HTTP_TRANSPORT)) {
                            TransportInDescription httpsTransport = configCtx.getAxisConfiguration()
                                    .getTransportIn(ServerConstants.HTTP_TRANSPORT);
                            Parameter parameter = httpsTransport.getParameter("proxyPort");
                            String port = "";
                            if (parameter != null && !"-1".equals(parameter.getValue())) {
                                String value = (String) parameter.getValue();
                                if (!"80".equals(value)) {
                                    port = ":" + value;
                                }
                            } else {
                                port = ":" + CarbonUtils.getTransportPort(configCtx,
                                        ServerConstants.HTTP_TRANSPORT);
                            }
                            redirectURL =
                                    ServerConstants.HTTPS_TRANSPORT + "://" + url.getHost() + ":" +
                                            port + url.getPath() + "?" + url.getQuery();
                            return redirectURL;
                        }
                    }
                    Map<String, String> paramMap = new HashMap<String, String>();

                    DOMSource xmlSource = Util.getSigStream(axisService, null);

                    paramMap.put("image-path", "?wsdl2form&type=image&resource=");
                    paramMap.put("show-alternate", "false");
                    paramMap.put("fixendpoints", "true");
                    paramMap.put("task-id", taskID);
                    paramMap.put("proxyAddress", proxyUrl);

                    if (serviceName != null && !serviceName.equals("")) {
                        paramMap.put("service", serviceName);
                    }
                    if (operationName != null && !operationName.equals("")) {
                        paramMap.put("operation", operationName);
                    }
                    if (fullPage) {
                        paramMap.put("full-page", "true");
                    }

                    Util.generateMockit(xmlSource, result, paramMap);

                    return WSDL2FormGenerator.SUCCESS;
                } else {
                    throw new CarbonException(WSDL2FormGenerator.SERVICE_INACTIVE);
                }
            } else {
                throw new CarbonException(WSDL2FormGenerator.SERVICE_NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (OMException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }
    }


    /**
     * @param result       Result object containing the generated form
     * @param configCtx    ConfigurationContext of the tenant. In standalone mode, this should be
     *                     main ConfigurationContext.
     * @param stubURL      URL of the service that need to be tried out.
     * @param serviceName  Name of the service when multiple services are available in the WSDL. If
     *                     {@code serviceName} is {@code null}, then the fist service in the WSDL
     *                     will be used.
     * @param endpointName If you need to generate get the tryit form only for specific endpoint
     *                     only, then you can specify it here. It could be <i>SOAP11Endpoint,
     *                     SOAP12Endpoint</i> or <i>HTTPEndpoint etc</i>.
     * @param language     Language of the generated javascript stub. Default language is Javascript
     *                     while you can use {@code javascript, ecmascript or js} for
     *                     <i>application/javascript</i> and {@code e4x} for
     *                     <i>text/javascript</i>,
     *
     * @throws CarbonException Throws when an error occurred during the execution.
     */
    public String getJSStub(Result result, ConfigurationContext configCtx, String stubURL,
                            String serviceName, String endpointName, String language,
                            String localhost, String contentType) throws CarbonException {
        try {
            String type;
            String serviceContextRoot = configCtx.getServiceContextPath();
            if (!serviceContextRoot.endsWith("/")) {
                serviceContextRoot = serviceContextRoot + "/";
            }
            AxisService axisService = getAxisService(stubURL, serviceContextRoot, configCtx);
            if (axisService != null) {
                if (axisService.isActive()) {

                    DOMSource xmlSource = Util.getSigStream(axisService, null);

                    Map<String, String> parameterMap = new HashMap<String, String>();

                    /*
                    * Handling the "lang" parameter
                    */
                    if ((language == null) || (language.equals("")) ||
                            (language.equalsIgnoreCase("javascript")) ||
                            (language.equalsIgnoreCase("ecmascript")) ||
                            (language.equalsIgnoreCase("js"))) {
                        type = "application/javascript";
                    } else if (language.equalsIgnoreCase("e4x")) {
                        parameterMap.put("e4x", "true");
                        type = "text/javascript";
                    } else {
                        throw new CarbonException(WSDL2FormGenerator.UNSUPPORTED_LANG);
                    }

                    if (contentType != null && !"".equals(contentType.trim())) {
                        type = contentType;
                    }
                    type += "; charset=utf-8";
                    // Handling the localhost parameter. If set to true the stub will use localhost instead of the IP.
                    // Needed when stubs are needed fir inclusion in a mashup.
                    if (localhost != null && "true".equals(localhost)) {
                        parameterMap.put("localhost-endpoints", "true");
                    }
                    Util.generateStub(xmlSource, result, parameterMap);
                    return type;

                } else {
                    throw new CarbonException(WSDL2FormGenerator.SERVICE_INACTIVE);
                }
            } else {
                throw new CarbonException(WSDL2FormGenerator.SERVICE_NOT_FOUND);
            }
        } catch (OMException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        } catch (AxisFault e) {
            log.error(e.getMessage(), e);
            throw new CarbonException(e);
        }
    }

    private String generateTryitExternal(AxisService service, ConfigurationContext configCtx,
                                         ByteArrayOutputStream wsdl2Bos, String serviceName,
                                         String operationName, String endpointName,
                                         Map fileResourcesMap, boolean fullPage,
                                         boolean needSOAPAction)
            throws ParserConfigurationException, TransformerException, FileNotFoundException,
            CarbonException {
        if (log.isDebugEnabled()) {
            try {
                XMLStreamReader streamReader = XMLInputFactory.newInstance()
                        .createXMLStreamReader(new ByteArrayInputStream(wsdl2Bos.toByteArray()));
                StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(streamReader);
                OMElement ele = stAXOMBuilder.getDocumentElement();
                log.debug("output ==> " + ele.toString());
            } catch (XMLStreamException e) {
                log.error(e.getMessage(), e);
            }
        }

        String serviceContextRoot = configCtx.getServiceContextPath();
        if (!serviceContextRoot.endsWith("/")) {
            serviceContextRoot = serviceContextRoot + "/";
        }
        Map<String, String> paramMap = new HashMap<String, String>();

        DOMSource xmlSource = Util.getSigStream(service, wsdl2Bos, null);

        Util.FileInfo jsFileLocation = Util.getOutputFileLocation(".stub.js");
        File jsFile = jsFileLocation.getFile();
        OutputStream jsStubOutputStream = new FileOutputStream(jsFile);
        Result jsStubResult = new StreamResult(jsStubOutputStream);
        Util.generateStub(xmlSource, jsStubResult, null);

        File[] files0 = jsFile.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".stub.js");
            }
        });
        if ((files0 != null) && (files0[0] != null) && (files0[0].getAbsoluteFile() != null)) {
            String absolutePath = files0[0].getAbsoluteFile().getAbsolutePath();
            fileResourcesMap.put(jsFileLocation.getUuid(), absolutePath);
        }
        Util.FileInfo htmlFileLocation = Util.getOutputFileLocation(".html");
        File tryItOutFile = htmlFileLocation.getFile();
        OutputStream tryItOutFileStream = new FileOutputStream(tryItOutFile);
        Result tryItResult = new StreamResult(tryItOutFileStream);

        paramMap.put("js-global-params", "carbon/global-params.js");
        paramMap.put("proxyAddress", "carbon/admin/jsp/WSRequestXSSproxy_ajaxprocessor.jsp");
        paramMap.put("js-service-stub", "filedownload?id=" + jsFileLocation.getUuid());
        paramMap.put("js-WSRequest", "carbon/admin/js/WSRequest.js");
        paramMap.put("js-jQuery", "carbon/admin/js/jquery-1.5.2.min.js");
        paramMap.put("js-jQueryUI", "carbon/admin/js/jquery-ui-1.8.11.custom.min.js");
        paramMap.put("js-corners", "carbon/tryit/extras/jquery.corner.js");
        paramMap.put("js-editArea", "carbon/editarea/edit_area_full.js");
        paramMap.put("xslt-location", "carbon/tryit/xslt/prettyprinter.xslt");
        paramMap.put("css-images", "carbon/tryit/images/");
        paramMap.put("css-jQueryUI", "carbon/admin/css/smoothness/jquery-ui-1.8.11.custom.css");
        paramMap.put("show-alternate", "false");
        paramMap.put("fixendpoints", "true");

        Util.generateTryit(xmlSource, tryItResult, paramMap);

        File[] files1 = tryItOutFile.getParentFile().listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".html");
            }
        });
        if ((files1 != null) && (files1[0] != null) && (files1[0].getAbsoluteFile() != null)) {
            fileResourcesMap
                    .put(htmlFileLocation.getUuid(), files1[0].getAbsoluteFile().getAbsolutePath());
        }

        return "../.." + ServerConstants.ContextPaths.DOWNLOAD_PATH + "?id=" +
                htmlFileLocation.getUuid();
    }


    private synchronized void updateMockProxyServiceGroup(AxisService axisService, ConfigurationContext configCtx,
                                                          AxisConfiguration axisConfig)
            throws AxisFault {
        /*axisService.addParameter("supportSingleOperation", Boolean.TRUE);
        AxisOperation singleOP = new InOutAxisOperation(new QName("invokeTryItProxyService"));
        singleOP.setDocumentation("This operation is a 'passthrough' for all operations in " +
                                  " TryIt proxy service.");
        axisService.addOperation(singleOP);*/
        ProxyMessageReceiver receiver = new ProxyMessageReceiver(configCtx);
        PhasesInfo phaseInfo = axisConfig.getPhasesInfo();
        for (Iterator i = axisService.getOperations(); i.hasNext();) {
            AxisOperation op = (AxisOperation) i.next();
            op.setMessageReceiver(receiver);
            phaseInfo.setOperationPhases(op);
        }
        AxisServiceGroup serviceGroup;
        synchronized (axisConfig) {
            serviceGroup = axisConfig.getServiceGroup(TRYIT_SG_NAME);
            if (serviceGroup == null) {
                serviceGroup = new AxisServiceGroup();
                serviceGroup.setServiceGroupName(TRYIT_SG_NAME);
                serviceGroup.addParameter("DoAuthentication", "false");
                serviceGroup.addParameter("adminService", "true");
            }

            // resolving Axis service name conflicts.
            AxisService testService = axisConfig.getService(axisService.getName());
            if (testService != null) {
                for (int loop = 1; ; loop++) {
                    String testName = axisService.getName() + "_" + loop;
                    if (axisConfig.getService(testName) == null) {
                        axisService.setName(testName);
                        break;
                    }
                }
            }
            serviceGroup.addService(axisService);
            axisConfig.addServiceGroup(serviceGroup);
            axisService.addParameter(LAST_TOUCH_TIME, System.currentTimeMillis());
            axisService.addParameter("modifyUserWSDLPortAddress", "false");
            // Set the timer.
            Parameter parameter = axisConfig.getParameter(PROXY_TIMER);
            if (parameter == null) {
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new ProxyTimerTask(axisConfig), PERIOD, PERIOD);
                parameter = new Parameter(PROXY_TIMER, timer);
                axisConfig.addParameter(parameter);
            }
        }
    }

    private String getBaseURI(String currentURI) {
        String uriFragment = currentURI.substring(0, currentURI.lastIndexOf("/"));
        return uriFragment + (uriFragment.endsWith("/") ? "" : "/");
    }

    private AxisService getAxisService(String url, String serviceContextRoot,
                                       ConfigurationContext configCtx) throws AxisFault {
        String service =
                url.substring(url.indexOf(serviceContextRoot) + serviceContextRoot.length(),
                        url.indexOf("?"));
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        AxisService axisService = axisConfig.getServiceForActivation(service);
        if (axisService == null) {
            // Try to see whether the service is available in a tenant
            axisService = TenantAxisUtils.getAxisService(url, configCtx);
            axisConfig = TenantAxisUtils.getTenantAxisConfiguration(TenantAxisUtils
                    .getTenantDomain(url), configCtx);
        }
        if (GhostDeployerUtils.isGhostService(axisService) && axisConfig != null) {
            // if the existing service is a ghost service, deploy the actual one
            axisService = GhostDeployerUtils.deployActualService(axisConfig, axisService);
        }
        return axisService;
    }
}

