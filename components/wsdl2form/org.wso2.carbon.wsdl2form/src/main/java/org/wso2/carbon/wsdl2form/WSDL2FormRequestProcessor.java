package org.wso2.carbon.wsdl2form;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class helps to load needed Resources for the Tryit page. When FE/BE is separated we cannot
 * access the JavaScript files and images required for the proper operation of Tryit. This Request
 * Processor is used to load any needed content from BE to FE. We can load any content from
 * wsdl2form's resources using
 * ?wsdl2form&resource=foo.js&type=js or
 * ?wsdl2form&resource=bar.jpg&type=image
 */
public class WSDL2FormRequestProcessor implements HttpGetRequestProcessor {

    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
        OutputStream responseStream = response.getOutputStream();
        String resource = request.getParameter("resource");
        String contentType = request.getParameter("contentType");

        ClassLoader classLoader = WSDL2FormRequestProcessor.class.getClassLoader();

        InputStream resourceStream = classLoader.getResourceAsStream(resource);

        if (contentType != null && !contentType.equals("")) {
            response.addHeader(HTTP.CONTENT_TYPE, contentType);
        }
        if (resourceStream != null) {
            int n;
            byte[] b = new byte[1024];
            while ((n = resourceStream.read(b)) > 0) {
                responseStream.write(b, 0, n);
            }
            resourceStream.close();
        }
    }
}
