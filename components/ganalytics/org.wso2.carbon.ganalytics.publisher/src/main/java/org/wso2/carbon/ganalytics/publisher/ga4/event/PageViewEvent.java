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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ganalytics.publisher.ga4.event;

/**
 * Represents a Google Analytics 4 Measurement Protocol 'page_view' event.
 * See: https://developers.google.com/tag-platform/gtagjs/reference/events#page_view
 */
public class PageViewEvent extends Event {

    private static final String PAGE_VIEW = "page_view";
    private static final String PAGE_LOCATION = "page_location";
    private static final String CLIENT_ID = "client_id";
    private static final String LANGUAGE = "language";
    private static final String PAGE_ENCODING = "page_encoding";
    private static final String PAGE_TITLE = "page_title";
    private static final String USER_AGENT = "user_agent";

    public PageViewEvent() {
        super(PAGE_VIEW);
    }

    public void setPageLocation(String pageLocation) {
        putParam(PAGE_LOCATION, pageLocation);
    }

    public String getPageLocation() {
        return (String) getParam(PAGE_LOCATION);
    }

    public void setClientId(String clientId) {
        putParam(CLIENT_ID, clientId);
    }

    public String getClientId() {
        return (String) getParam(CLIENT_ID);
    }

    public void setLanguage(String language) {
        putParam(LANGUAGE, language);
    }

    public String getLanguage() {
        return (String) getParam(LANGUAGE);
    }

    public void setPageEncoding(String pageEncoding) {
        putParam(PAGE_ENCODING, pageEncoding);
    }

    public String getPageEncoding() {
        return (String) getParam(PAGE_ENCODING);
    }

    public void setPageTitle(String pageTitle) {
        putParam(PAGE_TITLE, pageTitle);
    }

    public String getPageTitle() {
        return (String) getParam(PAGE_TITLE);
    }

    public void setUserAgent(String userAgent) {
        putParam(USER_AGENT, userAgent);
    }

    public String getUserAgent() {
        return (String) getParam(USER_AGENT);
    }

}
