/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.logging.view.ui.util;

/**
 * Constants used in log view feature.
 */
public final class LoggingConstants {

    public static final String URL_SEPARATOR = "/";
    public static final String WSO2_STRATOS_MANAGER = "manager";
    public static final String CONFIG_FILENAME = "cloud-services-desc.xml";
    public static final String MULTITENANCY_CONFIG_FOLDER = "multitenancy";

    public static final class RegexPatterns {

        public static final String LOCAL_CARBON_LOG_PATTERN = "wso2carbon*.log";
        public static final String LOG_FILE_DATE_SEPARATOR = "wso2carbon-";
        public static final String CURRENT_LOG = "0_Current Log";
    }
}

