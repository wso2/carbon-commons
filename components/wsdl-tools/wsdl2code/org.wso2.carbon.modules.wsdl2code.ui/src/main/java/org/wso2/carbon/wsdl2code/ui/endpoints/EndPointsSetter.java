/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.wsdl2code.ui.endpoints;

import java.util.regex.PatternSyntaxException;

public class EndPointsSetter {

    public String getEndPoints(String str) {
        String retVal = "<select name='pn' class='toolsClass' id='id_pn'>";
        try {
            String[] splitArray = str.split(",");
            for (String st : splitArray) {
                if (!st.toLowerCase().contains("local")) {
                    retVal += "<option value='" + st + "'>" + st + "</option>";
                }
            }
        } catch (PatternSyntaxException ex) {
            ex.printStackTrace();
        }
        retVal += "</select>";
        return retVal;
    }
}
