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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles sending the GA4 request. Uses {@link GoogleAnalytics4Data} to construct the payload.
 */
public class GoogleAnalytics4DataPublisher {

    private static final Log log = LogFactory.getLog(GoogleAnalytics4DataPublisher.class.getName());

    private static final String GA4_COLLECTION_ENDPOINT_URL = "https://www.google-analytics.com/mp/collect";
    private static final String API_SECRET = "api_secret";
    private static final String MEASUREMENT_ID = "measurement_id";
    private static final String EVENTS_PAYLOAD_PROPERTY_KEY = "events";
    private static final String CLIENT_ID_PAYLOAD_PROPERTY_KEY = "client_id";

    private GoogleAnalytics4DataPublisher() {
        // Prevents Instantiation
    }

    /**
     * Publishes the request - which is generated with the given googleAnalytics4Data, to Google Analytics.
     * @param googleAnalytics4Data  GoogleAnalytics4Data object containing data to be sent.
     * @param userAgent             Value that will be put against the "User-Agent" header.
     * @return                      Whether the request was successful or not.
     */
    public static boolean publishData(GoogleAnalytics4Data googleAnalytics4Data, String userAgent) {
        HttpClient client = new DefaultHttpClient();
        try {
            HttpPost post = new HttpPost(getUri(googleAnalytics4Data));
            post.setHeader(HttpHeaders.USER_AGENT, userAgent);
            post.setEntity(new StringEntity(getJsonBodyPayload(googleAnalytics4Data)));
            HttpResponse response = client.execute(post);
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (log.isDebugEnabled()) {
                    log.debug("GA4 Measurement Protocol HTTP call returned response with status code: " + statusCode);
                }
                // The Measurement Protocol (GA4) always returns a 2xx status code if the HTTP request was received
                return 200 <= statusCode && statusCode < 300;
            }
            if (log.isDebugEnabled()) {
                log.debug("GA4 Measurement Protocol HTTP call returned a null response");
            }
        } catch (URISyntaxException e) {
            log.error("Google Analytics 4 collection URI that begins with '" + GA4_COLLECTION_ENDPOINT_URL +
                    "' could not be parsed as a URI reference.", e);
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding for Google Analytics 4 collection JSON payload", e);
        } catch (IOException e) {
            log.error("Failed to perform the POST request for Google Analytics 4 event collection", e);
        }
        return false;
    }

    private static String getUri(GoogleAnalytics4Data googleAnalytics4Data) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(GA4_COLLECTION_ENDPOINT_URL);
        uriBuilder.addParameter(API_SECRET, googleAnalytics4Data.getApiSecret());
        uriBuilder.addParameter(MEASUREMENT_ID, googleAnalytics4Data.getMeasurementId());
        for (Map.Entry<String, String> queryParam : googleAnalytics4Data.getQueryParams().entrySet()) {
            uriBuilder.addParameter(queryParam.getKey(), queryParam.getValue());
        }
        return uriBuilder.build().toString();
    }

    private static String getJsonBodyPayload(GoogleAnalytics4Data googleAnalytics4Data) {
        Map<String, Object> payload = new HashMap<>();
        addPayloadValueIfNotNull(CLIENT_ID_PAYLOAD_PROPERTY_KEY, googleAnalytics4Data.getClientId(), payload);
        addPayloadValueIfNotNull(EVENTS_PAYLOAD_PROPERTY_KEY, googleAnalytics4Data.getEvents(), payload);
        payload.putAll(googleAnalytics4Data.getPayloadProperties());
        return new Gson().toJson(payload);
    }

    private static void addPayloadValueIfNotNull(String key, Object value, Map<String, Object> payload) {
        if (value != null) {
            payload.put(key, value);
        }
    }

}
