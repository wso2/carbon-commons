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

package org.wso2.carbon.event.core.internal.delivery.inmemory;

import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.core.exception.EventBrokerException;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class is used to keep the details of the subscription storage. Simply this contains
 * maps to keep the subscrition object deatils with the topic details
 */
public class InMemorySubscriptionStorage {
    /**
     * map to keep the subscription details with the topics. This is important in finding subscriptions
     * for a pirticular topic when publishing a message to a topic.
     */
    private Map<String, Map<String, Subscription>> topicSubscriptionMap;

    /**
     * keep the subscription id with the topic name. This is important when subscribing the resouces.
     */
    private Map<String, String> subscriptionIDTopicNameMap;

    public InMemorySubscriptionStorage() {
        this.topicSubscriptionMap = new ConcurrentHashMap<String, Map<String, Subscription>>();
        this.subscriptionIDTopicNameMap = new ConcurrentHashMap<String, String>();
    }

    public void addSubscription(Subscription subscription) {
        String topicName = getTopicName(subscription.getTopicName());
        Map<String, Subscription> subscriptionsMap = this.topicSubscriptionMap.get(topicName);
        if (subscriptionsMap == null){
            subscriptionsMap = new ConcurrentHashMap<String, Subscription>();
            this.topicSubscriptionMap.put(topicName, subscriptionsMap);
        }
        subscriptionsMap.put(subscription.getId(), subscription);
        this.subscriptionIDTopicNameMap.put(subscription.getId(), topicName);
    }

    public List<Subscription> getMatchingSubscriptions(String topicName) {
        topicName = getTopicName(topicName);
        List<Subscription> subscriptions = new ArrayList();

        List<String> matchingTopicNames = getTopicMatchingNames(topicName);
        for (String matchingTopicName : matchingTopicNames){
             if (this.topicSubscriptionMap.get(matchingTopicName) != null){
                 subscriptions.addAll(this.topicSubscriptionMap.get(matchingTopicName).values());
             }
        }
        return subscriptions;
    }

    public void unSubscribe(String subscriptionID) throws EventBrokerException {
        String topicName = getTopicName(this.subscriptionIDTopicNameMap.get(subscriptionID));
        if (topicName == null){
            throw new EventBrokerException("Subscription with ID " + subscriptionID + " does not exits");
        }
        Map<String, Subscription> subscriptionsMap = this.topicSubscriptionMap.get(topicName);
        if (subscriptionsMap == null){
            throw new EventBrokerException("Subscription with ID " + subscriptionID + " does not exits");
        }
        subscriptionsMap.remove(subscriptionID);
        this.subscriptionIDTopicNameMap.remove(subscriptionID);
    }

    public void renewSubscription(Subscription subscription) throws EventBrokerException {
        String topicName = getTopicName(subscription.getTopicName());
        Map<String, Subscription> subscriptionsMap = this.topicSubscriptionMap.get(topicName);
        if (subscriptionsMap == null){
            throw new EventBrokerException("There is no subscriptions with topic " + topicName);
        }

        Subscription existingSubscription = subscriptionsMap.get(subscription.getId());

        if (existingSubscription == null){
            throw new EventBrokerException("There is no subscription with subscription id " + subscription.getId());
        }

        existingSubscription.setExpires(subscription.getExpires());
        existingSubscription.setProperties(subscription.getProperties());

    }

    private List<String> getTopicMatchingNames(String topicName) {
        List<String> matchingTopicNames = new ArrayList<String>();

        if (topicName.equals("/")) {
            matchingTopicNames.add("/#");
        } else {
            String currentTopicName = "";
            String[] topicParts = topicName.split("/");
            int i = 0; // the first part if the split parts are "" since always topics start with /
            while (i < (topicParts.length)) {
                currentTopicName = currentTopicName + topicParts[i] + "/";
                matchingTopicNames.add(currentTopicName + "#");
                if (i == (topicParts.length - 1)||i == (topicParts.length - 2)) {
                    matchingTopicNames.add(currentTopicName + "*");
                }
                i++;
            }
        }
        matchingTopicNames.add(topicName);
        return matchingTopicNames;
    }

    private String getTopicName(String topicName){
        if (!topicName.startsWith("/")){
            topicName = "/" + topicName;
        }

        if (topicName.endsWith("/") && (topicName.length() != 1)){
            topicName = topicName.substring(0, topicName.lastIndexOf("/"));
        }
        return topicName;
    }
}
