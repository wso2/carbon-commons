package org.wso2.carbon.wsdl2form;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class helps to load needed Resources for the Tryit page. When FE/BE is separated we cannot
 * access the JavaScript files and images required for the proper operation of Tryit. This Request
 * Processor is used to load any needed content from BE to FE. We can load any content from
 * wsdl2form's resources using
 * ?wsdl2form&resource=foo.js&type=js or
 * ?wsdl2form&resource=bar.jpg&type=image
 */
public class WSDL2FormRequestProcessor implements HttpGetRequestProcessor {

    private static Log log = LogFactory.getLog(WSDL2FormRequestProcessor.class);
    private static final int BAD_REQUEST = 400;

    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
        OutputStream responseStream = response.getOutputStream();
        String resource = request.getParameter("resource");
        String contentType = request.getParameter("contentType");

        if (isEmptyString(resource)) {
            response.setStatus(BAD_REQUEST);
            return;
        }
        if (!isResourceFoundInJar(resource)) {
            log.warn("Resource " + resource + " was not found in JAR file " + getJarFilePath());
            response.setStatus(BAD_REQUEST);
            return;
        }

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

    private boolean isResourceFoundInJar(String resource) {
        JarFile jarFile = null;
        File jarFilePath = getJarFilePath();

        try {
            jarFile = new JarFile(jarFilePath);
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final String name = entries.nextElement().getName();
                if (resource.equals(name)) {
                    return true;
                }
            }
        } catch (IOException e) {
            log.error("Could not read JAR file " + jarFilePath.toString(), e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ignore) {
                }
            }
        }
        return false;
    }

    private File getJarFilePath() {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    private boolean isEmptyString(String value) {
        return ((value == null) || value.trim().length() == 0);
    }
}
