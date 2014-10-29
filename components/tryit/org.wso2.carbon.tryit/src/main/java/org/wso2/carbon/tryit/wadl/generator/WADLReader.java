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


import org.jvnet.ws.wadl.ast.ApplicationNode;
import org.jvnet.ws.wadl.ast.InvalidWADLException;
import org.jvnet.ws.wadl.ast.ResourceNode;
import org.jvnet.ws.wadl.ast.WadlAstBuilder;
import org.jvnet.ws.wadl.util.MessageListener;
import org.xml.sax.InputSource;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.URI;

public class WADLReader {

    public String readWADLFromUrl(String urlString) throws Exception {
        return validateWADL(urlString);
    }

    private String validateWADL(String uri) throws Exception {
        WadlAstBuilder builder = new WadlAstBuilder(
                new WadlAstBuilder.SchemaCallback() {

                    public void processSchema(InputSource is) {
                    }

                    @Override
                    public void processSchema(String s, org.w3c.dom.Element element) {

                    }

                },
                new MessageListener() {

                    public void warning(String message, Throwable throwable) {
                    }

                    public void info(String message) {
                    }

                    public void error(String message, Throwable throwable) {
                    }
                }
        );

        try {

            ApplicationNode applicationNode = builder.buildAst(new URI(uri));


            ApiGenerator apiGenerator = new ApiGenerator();
            return apiGenerator.CreateJsonString(applicationNode);

        } catch (ConnectException e) {
            String msg = "Invalid WADL uri found " + uri;
            throw new Exception(msg, e);
        } catch (InvalidWADLException e) {
            String msg = "Invalid WADL definition found";
            throw new Exception(msg, e);
        } catch (FileNotFoundException e) {
            String msg = "WADL not found/Invalid grammar import/s found";
            throw new Exception(msg, e);
        } catch (Exception e) {
            String msg = "Unexpected error occured while adding WADL from " + uri;
            throw new Exception(msg, e);
        }


    }
}
