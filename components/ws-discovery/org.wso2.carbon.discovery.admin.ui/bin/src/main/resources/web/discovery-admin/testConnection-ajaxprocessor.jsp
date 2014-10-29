<%@ page import="java.net.URL" %>
<%@ page import="java.rmi.UnknownHostException" %>
<%@ page import="java.net.MalformedURLException" %>
<%@ page import="java.net.ConnectException" %>
<%@ page import="java.net.UnknownServiceException" %>
<%@ page import="javax.net.ssl.SSLHandshakeException" %>

<%!
    private String testURL(String url) {
        if (url != null && !url.equals("")) {
            try {
                URL conn = new URL(url);
                conn.getContent();
                return "success";
            } catch (UnknownHostException e) {
                return "unknown_host";
            } catch (MalformedURLException e) {
                return "malformed";
            } catch (ConnectException e) {
                return "connect_error";
            } catch (UnknownServiceException e) {
                return "unknown_service";
            } catch (SSLHandshakeException e) {
                return "ssl_error";
            } catch (Exception e) {
                // A HTTP 500 may result in an error here - so try to fetch the WSDL of endpoint
                if (!url.endsWith("?wsdl")) {
                    return testURL(url + "?wsdl");
                } else {
                    return "unexpected_error";
                }
            }

        } else {
            return "Invalid address specified";
        }
    }
%>

<%
    String url = request.getParameter("url");
    String returnValue = null;

    //we cannot validate a URL other than HTTP and HTTPS. Also check for ':' in first few chars to distinguish
    // unsupported protocol and missing protocol identifier(malformed)
    if (url != null && !url.toUpperCase().startsWith("HTTP") && !url.toUpperCase().startsWith("HTTPS")) {
        if (url.contains(":") && url.indexOf(':') < 6) {
            returnValue = "unsupported";
        }
    } else {
        returnValue = testURL(url);
    }
    out.write(returnValue);
%>
