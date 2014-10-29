/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.tryit;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.core.transports.CarbonHttpRequest;
import org.wso2.carbon.core.transports.CarbonHttpResponse;
import org.wso2.carbon.core.transports.HttpGetRequestProcessor;
import org.wso2.carbon.tryit.wadl.generator.WADLReader;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.wsdl2form.WSDL2FormGenerator;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.net.URLEncoder;

public class WADLTryItRequestProcessor implements HttpGetRequestProcessor {
    @Override
    public void process(CarbonHttpRequest request, CarbonHttpResponse response, ConfigurationContext configurationContext) throws Exception {
        OutputStream outputStream = response.getOutputStream();
        response.addHeader(HTTP.CONTENT_TYPE, "text/html; charset=utf-8");
        response.addHeader("Access-Control-Allow-Origin", "*");

        Result result = new StreamResult(outputStream);
        String url = request.getParameter("resourceurl");
        url = URLEncoder.encode(url.toString(), "UTF-8");
        String contextRoot = CarbonUtils.getServerConfiguration().getFirstProperty("WebContextRoot");

        outputStream.write(("<meta content=\"text/html;charset=utf-8\" http-equiv=\"Content-Type\">\n" +
                "<meta content=\"utf-8\" http-equiv=\"encoding\">").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/jquery-1.8.0.min.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/jquery.slideto.min.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/jquery.wiggle.min.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/jquery.ba-bbq.min.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/handlebars-1.0.rc.1.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/underscore-min.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/backbone-min.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/swagger.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/swagger-ui.js'></script>").getBytes());
        outputStream.write(("<script type='text/javascript' src='?wadl2form&contentType=text/javascript&resource=js/jquery.base64.js'></script>").getBytes());
        outputStream.write(("<link rel='stylesheet' type='text/css' href='?wadl2form&contentType=text/css&resource=css/screen.css'/>").getBytes());

        outputStream
                .write(("\n" +
                        "<style type='text/css'>\n" +
                        "    .swagger-ui-wrap {\n" +
                        "        max-width: 960px;\n" +
                        "        margin-left: auto;\n" +
                        "        margin-right: auto;\n" +
                        "    }\n" +
                        "\n" +
                        "    .icon-btn {\n" +
                        "        cursor: pointer;\n" +
                        "    }\n" +
                        "\n" +
                        "    #message-bar {\n" +
                        "        min-height: 30px;\n" +
                        "        text-align: center;\n" +
                        "        padding-top: 10px;\n" +
                        "    }\n" +
                        "\n" +
                        "    .message-success {\n" +
                        "        color: #89BF04;\n" +
                        "    }\n" +
                        "\n" +
                        "    .message-fail {\n" +
                        "        color: #cc0000;\n" +
                        "    }\n" +
                        "</style>\n" +
                        "\n" +
                        "<script type='text/javascript'>\n" +
                        "    $(function () {\n" +
                        "        window.swaggerUi = new SwaggerUi({\n" +
                        "            discoveryUrl: '?wadl2form&contentType=application/json&resource=swaggerurl&wadldocurl="+url+"',\n" +
                        "            dom_id: 'swagger-ui-container',\n" +
                        "            apiKeyName: 'authorization',\n" +
                        "            supportHeaderParams: true,\n" +
                        "            supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'options'],\n" +
                        "            onComplete: function (swaggerApi, swaggerUi) {\n" +
                        "                if (console) {\n" +
                        "                    console.log('Loaded SwaggerUI');\n" +
                        "                    console.log(swaggerApi);\n" +
                        "                    console.log(swaggerUi);\n" +
                        "                }\n" +
                        "                $('ul.endpoints').show();\n" +
                        "            },\n" +
                        "            onFailure: function (data) {\n" +
                        "                if (console) {\n" +
                        "                    console.log('Unable to Load SwaggerUI');\n" +
                        "                    console.log(data);\n" +
                        "                }\n" +
                        "            },\n" +
                        "            docExpansion: 'none'\n" +
                        "        });\n" +
                        "        window.swaggerUi.load();\n" +
                        "    });\n" +
                        "</script>\n" +
                        "<div id='swagger-ui-container'>Please wait while loading try it page....</div>\n" +
                        "<style>\n" +
                        "    html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, center, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td, article, aside, canvas, details, embed, figure, figcaption, footer, header, hgroup, menu, nav, output, ruby, section, summary, time, mark, audio, video {\n" +
                        "        font-family: Arial, Helvetica, Verdana, monospace, san-serif;\n" +
                        "        font-size: 12px;\n" +
                        "        line-height: 20px;\n" +
                        "    }\n" +
                        "\n" +
                        "    div#overview ul {\n" +
                        "        list-style: disc;\n" +
                        "        margin: 5px 5px 5px 18px;\n" +
                        "    }\n" +
                        "\n" +
                        "    div#overview li {\n" +
                        "        padding-bottom: 5px;\n" +
                        "    }\n" +
                        "\n" +
                        "    h2 {\n" +
                        "        font-size: 24px;\n" +
                        "        line-height: normal;\n" +
                        "        font-weight: bold;\n" +
                        "    }\n" +
                        "\n" +
                        "    .search-back body {\n" +
                        "        line-hieight: 18px;\n" +
                        "    }\n" +
                        "\n" +
                        "    body ul#resources li.resource div.heading h2 a {\n" +
                        "        color: #111111;\n" +
                        "    }\n" +
                        "\n" +
                        "    .search-back {\n" +
                        "        padding: 0 0 0 20px;\n" +
                        "    }\n" +
                        "\n" +
                        "    ul.endpoints {\n" +
                        "        padding: 10px;\n" +
                        "        border: solid 1px #efefef;\n" +
                        "    }\n" +
                        "\n" +
                        "    body ul#resources li.resource div.heading {\n" +
                        "        background: #EFEFEF;\n" +
                        "        padding: 0 10px;\n" +
                        "    }\n" +
                        "\n" +
                        "    body ul#resources li.resource div.heading ul.options {\n" +
                        "        margin: 23px 10px 0 0;\n" +
                        "    }\n" +
                        "\n" +
                        "    h6 {\n" +
                        "        color: inherit;\n" +
                        "        font-family: inherit;\n" +
                        "        font-weight: bold;\n" +
                        "        line-height: 20px;\n" +
                        "        margin: 0 0 10px 0;\n" +
                        "        text-rendering: optimizelegibility;\n" +
                        "    }\n" +
                        "\n" +
                        "    h5 {\n" +
                        "        color: inherit;\n" +
                        "        font-family: inherit;\n" +
                        "        font-size: 18px;\n" +
                        "        font-weight: bold;\n" +
                        "        line-height: 20px;\n" +
                        "        margin: 10px 0;\n" +
                        "        text-rendering: optimizelegibility;\n" +
                        "    }\n" +
                        "</style>\n").getBytes());
        outputStream.write(("<input type='hidden' name='contextRoot_name' id='contextRoot' value='"+contextRoot+"'>").getBytes());
        outputStream.flush();

    }
}
