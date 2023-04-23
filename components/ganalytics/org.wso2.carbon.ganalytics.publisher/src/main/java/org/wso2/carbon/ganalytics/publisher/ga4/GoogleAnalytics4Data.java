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

package org.wso2.carbon.ganalytics.publisher.ga4;

import org.wso2.carbon.ganalytics.publisher.ga4.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the data to be sent via the Google Analytics 4 Measurement Protocol.
 * See: https://developers.google.com/analytics/devguides/collection/protocol/ga4/reference
 *
 * {@link GoogleAnalytics4DataPublisher} uses this to create the HTTPS request and publish GA4 data.
 */
public class GoogleAnalytics4Data {

    /**
     * API Secret that is generated through the Google Analytics UI.
     */
    private String apiSecret;

    /**
     * Measurement ID of a web data stream.
     */
    private String measurementId;

    /**
     * Additional query parameters sent in the URL of the request.
     */
    private Map<String, String> queryParams;

    /**
     * The 'client_id' property, which is sent in the body payload.
     */
    private String clientId;

    /**
     * The 'events' array, which is sent in the body payload.
     */
    private List<Event> events;

    /**
     * Additional properties that are sent in the body payload.
     */
    private Map<String, Object> payloadProperties;

    public GoogleAnalytics4Data(String apiSecret, String measurementId) {
        this.apiSecret = apiSecret;
        this.measurementId = measurementId;
        this.events = new ArrayList<>(1);
        this.payloadProperties = new HashMap<>(1);
        this.queryParams = new HashMap<>(1);
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getMeasurementId() {
        return measurementId;
    }

    public Map<String, Object> getPayloadProperties() {
        return payloadProperties;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    public void putPayloadProperty(String key, Object value) {
        this.payloadProperties.put(key, value);
    }

    public void putQueryParam(String key, String value) {
        this.queryParams.put(key, value);
    }

}
