<%@ page import="java.net.URL" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.net.URLDecoder" %>
<%@ page import="java.io.*" %>
<%@ page import="org.json.*" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.methods.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ page trimDirectiveWhitespaces="true" %>

<%


    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
        InputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } else {
            stringBuilder.append("");
        }
    } catch (IOException ex) {
        throw ex;
    } finally {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    body = stringBuilder.toString();
    String requestedUrl = "";
    String requestHttpMethod = "";
    String bodyParam = "";
    String contentType = "";
    String decodedString = "";
    StringRequestEntity entity = null;
    int responseCode = -1;

    try {
        JSONObject job = new JSONObject(body.toString());

        if (job.get("requestedUrl") != null) {
            requestedUrl = (String) job.get("requestedUrl");
        }
        if (job.get("requestHttpMethod") != null) {
            requestHttpMethod = (String) job.get("requestHttpMethod");
        }
        try {

            if (job.get("bodyParam") != null) {
                bodyParam = (String) job.get("bodyParam");
            }
        } catch (Exception e) {

        }
        try {
            if (job.get("contentType") != null) {
                contentType = (String) job.get("contentType");
            }

        } catch (Exception e) {

        }
        if (contentType.equalsIgnoreCase("application/octet-stream") || contentType.equalsIgnoreCase("")) {
            contentType = "text/html";
        }

        bodyParam = URLDecoder.decode(bodyParam, "UTF-8");

    } catch (Exception e) {
        e.printStackTrace();
    }
    HttpClient client = new HttpClient();


    if (requestHttpMethod.equalsIgnoreCase("post")) {
        PostMethod post = new PostMethod(requestedUrl);
        entity = new StringRequestEntity(bodyParam, contentType, "utf-8");
        post.setRequestEntity(entity);
        responseCode = client.executeMethod(post);
        decodedString = post.getResponseBodyAsString();
    }
    if (requestHttpMethod.equalsIgnoreCase("get")) {
        if (!bodyParam.equalsIgnoreCase("null") && !bodyParam.equalsIgnoreCase("undefined")) {
            requestedUrl = requestedUrl + "?" + bodyParam;
        }
        GetMethod getMethod = new GetMethod(requestedUrl);
        responseCode = client.executeMethod(getMethod);
        decodedString = getMethod.getResponseBodyAsString();
    }
    if (requestHttpMethod.equalsIgnoreCase("put")) {
        PutMethod put = new PutMethod(requestedUrl);
        entity = new StringRequestEntity(bodyParam, contentType, "utf-8");
        put.setRequestEntity(entity);
        responseCode = client.executeMethod(put);
        decodedString = put.getResponseBodyAsString();
    }
    if (requestHttpMethod.equalsIgnoreCase("delete")) {
        if (!bodyParam.equalsIgnoreCase("null") && !bodyParam.equalsIgnoreCase("undefined")) {
            requestedUrl = requestedUrl + "?" + bodyParam;
        }
        DeleteMethod del = new DeleteMethod(requestedUrl);
        responseCode = client.executeMethod(del);
        decodedString = del.getResponseBodyAsString();
    }
%>
<%=decodedString%>
