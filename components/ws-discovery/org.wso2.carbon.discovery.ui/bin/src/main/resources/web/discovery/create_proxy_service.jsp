<%@ page import="org.wso2.carbon.proxyadmin.ui.client.ProxyServiceAdminClient" %>
<%@ page import="org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData" %>
<%@ page import="java.net.URL" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="java.net.MalformedURLException" %>
<%
    String proxyServiceName = request.getParameter("proxyServiceName");
    String proxyServiceAddr = request.getParameter("proxyServiceAddress");
    boolean isDynamic = Boolean.valueOf(request.getParameter("isDynamic"));

    if (proxyServiceName == null || "".equals(proxyServiceName)) {
        request.setAttribute("wsd.proxy.svc.error", "invalid.name");
    } else if (proxyServiceAddr == null || "".equals(proxyServiceAddr)) {
        request.setAttribute("wsd.proxy.svc.error", "invalid.address");
    } else {
        ConfigurationContext configCtx =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        try {
            ProxyServiceAdminClient proxyAdminClient =
                    new ProxyServiceAdminClient(configCtx, backendServerURL, cookie, request.getLocale());
            ProxyData proxyData = new ProxyData();
            proxyData.setStartOnLoad(true);
            proxyData.setName(proxyServiceName);
            proxyData.setOutSeqXML("<outSequence xmlns=\"http://ws.apache.org/ns/synapse\"><send/></outSequence>");
            URL url = new URL(proxyServiceAddr);
            if (isDynamic) {
                String service = request.getParameter("service").trim();
                proxyData.setEndpointKey("wsdd://" + service + "/" + url.getProtocol());
            } else {
                proxyData.setEndpointXML("<endpoint xmlns=\"http://ws.apache.org/ns/synapse\"><address uri=\"" +
                        url.toString() + "\"/></endpoint>");
            }

            proxyAdminClient.addProxy(proxyData);
            request.setAttribute("wsd.proxy.svc.created", "true");
        } catch (MalformedURLException e) {
            request.setAttribute("wsd.proxy.svc.error", "malformed.url");
        } catch (Exception e) {
            request.setAttribute("wsd.proxy.svc.error", "unexpected.error");
            String cause;
            if (e.getCause() != null) {
                cause = e.getCause().getMessage();
            } else {
                cause = e.getMessage();
            }
            request.setAttribute("wsd.proxy.svc.error.cause", cause);
        }
    }
%>