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

package org.wso2.carbon.tryit.wadl.generator;


import com.google.gson.Gson;
import org.jvnet.ws.wadl.Param;
import org.jvnet.ws.wadl.ast.ApplicationNode;
import org.jvnet.ws.wadl.ast.MethodNode;
import org.jvnet.ws.wadl.ast.RepresentationNode;
import org.jvnet.ws.wadl.ast.ResourceNode;
import org.wso2.carbon.tryit.wadl.data.SwaggerImplementation;
import org.wso2.carbon.tryit.wadl.data.apis;
import org.wso2.carbon.tryit.wadl.data.operations;
import org.wso2.carbon.tryit.wadl.data.parameters;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApiGenerator {

    private SwaggerImplementation implementation = new SwaggerImplementation();
    private List<apis> apis = new ArrayList<apis>();

    public String CreateJsonString(ApplicationNode applicationNode) throws Exception {

        String basePath = applicationNode.getResources().get(0).getPathSegment().getTemplate();

        implementation.setBasePath(basePath);
        implementation.setApiVersion("1.0");
        implementation.setSwaggerVersion("1.1");
        implementation.setResourcePath(basePath);
        List<ResourceNode> nodeList = applicationNode.getResources().get(0).getChildResources();
        readResources(nodeList, "");
        implementation.setApis(apis);
        Gson gson = new Gson();
        return gson.toJson(implementation);
    }


    public void readResources(List<ResourceNode> nodeList, String parentPath) {
        boolean onlyOnce = true;
        for (ResourceNode rn : nodeList) {
            List<MethodNode> mnList = rn.getMethods();
            String path = parentPath + rn.getPathSegment().getTemplate();
            if (rn.getChildResources().isEmpty()) {

                onlyOnce = true;
                apis aps = new apis();
                aps.setPath(path);
                aps.setDescription("..............");
                List<operations> operationsList = new ArrayList<operations>();
                List<Param> templateParameters = rn.getPathSegment().getTemplateParameters();
                List<parameters> paramsList = new ArrayList<parameters>();

                for (Param p : templateParameters) {
                    parameters pm = new parameters();
                    pm.setName(p.getName());
                    pm.setAllowMultiple("false");
                    pm.setDataType(p.getType().getLocalPart());
                    pm.setRequired("true");
                    pm.setDescription("....");

                    if (rn.getPathSegment().getTemplate().contains("{")) {
                        pm.setParamType("path");
                    } else {
                        pm.setParamType("body");
                    }
                    paramsList.add(pm);
                }

                for (MethodNode mn : mnList) {
                    operations opr = new operations();
                    List<RepresentationNode> supportedInputs = mn.getSupportedInputs();
                    if (supportedInputs.size() > 0) {
                        String[] inputMethods = new String[supportedInputs.size()];
                        int x = 0;
                        for (RepresentationNode rs : supportedInputs) {
                            inputMethods[x] = rs.getMediaType();
                            x++;
                        }
                        opr.setSupportedContentTypes(inputMethods);
                    }
                    opr.setHttpMethod(mn.getName());
                    opr.setNickname("....");

                    opr.setNotes("....");
                    operationsList.add(opr);

                    List<Param> paramList = mn.getMatrixParameters();
                    for (Param p : paramList) {

                    }
                    if (onlyOnce) {
                        parameters pr = new parameters();
                        pr.setName("DefaultParam");
                        pr.setAllowMultiple("false");
                        pr.setDataType("string");
                        pr.setRequired("false");
                        pr.setDescription("Put request parameters here");
                        pr.setParamType("body");
                        paramsList.add(pr);

                        onlyOnce = false;
                    }
                    opr.setParameters(paramsList);
                }
                aps.setOperations(operationsList);
                apis.add(aps);
            }
            if (!rn.getChildResources().isEmpty()) {
                readResources(rn.getChildResources(), path);

            }

        }
    }

}
