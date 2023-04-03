/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.logging.service;

public final class LoggingConstants {

    public static final String AUDIT_LOGFILE = "AUDIT_LOGFILE";

    public static final String API_LOGFILE = "API_LOGFILE";

    public static final String CARBON_LOGFILE = "CARBON_LOGFILE";
    public static final String NAME_SUFFIX = ".name";
    public static final String APPENDER_PREFIX = "appender.";
    public static final String URL_SUFFIX = ".url";
    public static final String TYPE_SUFFIX = ".type";
    public static final String LAYOUT_SUFFIX = ".layout";
    public static final String PATTERN_SUFFIX = ".pattern";
    public static final String FILTER_SUFFIX = ".filter";
    public static final String THRESHOLD_SUFFIX = ".threshold";
    public static final String LEVEL_SUFFIX = ".level";
    public static final String CONNECTION_TIMEOUT_SUFFIX = ".connectTimeoutMillis";
    public static final String PATTERN_LAYOUT_TYPE = "PatternLayout";
    public static final String THRESHOLD_FILTER_TYPE = "ThresholdFilter";
    public static final String THRESHOLD_FILTER_LEVEL = "INFO";
    public static final String HTTP_APPENDER_TYPE = "http";
    public static final String DEFAULT_LAYOUT_PATTERN = "TID: [%tenantId] [%d] %5p {%c} - %m%ex%n";
}
