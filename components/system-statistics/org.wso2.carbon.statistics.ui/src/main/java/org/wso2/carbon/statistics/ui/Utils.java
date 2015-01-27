/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.statistics.ui;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class Utils {
    public static int getPositiveIntegerValue(HttpSession session, HttpServletRequest request,
                        int defaultValue, String keyName) {
        if (request.getParameter(keyName) != null) {
            try {
                defaultValue = Integer.parseInt(request.getParameter(keyName));
                if (defaultValue > 0) {
                    session.setAttribute(keyName, String.valueOf(defaultValue));
                } else {
                    defaultValue = 1;
                }
            } catch (NumberFormatException ignored) {
                if(session.getAttribute(keyName) != null){
                    defaultValue = Integer.parseInt((String) session.getAttribute(keyName));
                }
            }
        } else if(session.getAttribute(keyName) != null){
            defaultValue = Integer.parseInt((String) session.getAttribute(keyName));
        } else {
            session.setAttribute(keyName, String.valueOf(defaultValue));
        }
        return defaultValue;
    }
}
