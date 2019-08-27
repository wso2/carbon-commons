/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.tryit;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;

import java.io.InputStream;

/**
 * This class generates a UI to test the Swagger capabilities.
 * URL :- http://localhost:8280/API_NAME?swaggertryit
 */
public class SwaggerTryitRequestProcessor implements HttpGetRequestProcessor {

    private Log log = LogFactory.getLog(SwaggerTryitRequestProcessor.class);

    @Override
    public void process(CarbonHttpRequest request, CarbonHttpResponse response,
                        ConfigurationContext configurationContext) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("index.html");
        if (inputStream != null) {
            String content = IOUtils.toString(inputStream);
            if (content != null && !content.isEmpty()) {
                response.setStatus(200);
                response.addHeader("Content-Type", "text/html");
                response.getOutputStream().write(content.getBytes());
            } else {
                log.error("Error occurred while reading the swagger try-it web page");
                response.setStatus(500);
            }
        } else {
            log.error("Couldn't find the swagger try-it web page in the jar");
            response.setStatus(500);
        }
    }
}
