/*
 * Copyright The Apache Software Foundation.
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
package org.wso2.carbon.logging.service.data;

import java.util.HashMap;
import java.util.Map;

public class LoggingConfig {

    private String logProviderImplClassName;
    private String logFileProviderImplClassName;

    private Map<String, String> logProviderPropMap = new HashMap<String, String>();
    private Map<String, String> logFileProviderPropMap = new HashMap<String, String>();

    public LoggingConfig() {
    }


    public void setLogProviderProperty(String key, String value) {
        this.logProviderPropMap.put(key, value);
    }

    public void setLogFileProviderProperty(String key, String value) {
        this.logFileProviderPropMap.put(key, value);
    }

    public String getLogProviderProperty(String key) {
        return this.logProviderPropMap.get(key);
    }

    public String getLogFileProviderProperty(String key) {
        return this.logFileProviderPropMap.get(key);
    }

    public String getLogProviderImplClassName() {
        return logProviderImplClassName;
    }

    public void setLogProviderImplClassName(String logProviderImplClassName) {
        if (logProviderImplClassName == null | logProviderImplClassName.equals("")) {
            throw new IllegalArgumentException("LogProvider implementation class name should not be null or empty");
        }
        this.logProviderImplClassName = logProviderImplClassName;
    }

    public String getLogFileProviderImplClassName() {
        return logFileProviderImplClassName;
    }

    public void setLogFileProviderImplClassName(String logFileProviderImplClassName) {
        if (logFileProviderImplClassName == null | logFileProviderImplClassName.equals("")) {
            throw new IllegalArgumentException("LogProvider implementation class name should not be null or empty");
        }
        this.logFileProviderImplClassName = logFileProviderImplClassName;
    }
}
