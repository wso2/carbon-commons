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

package org.wso2.carbon.statistics.webapp;


import org.wso2.carbon.statistics.webapp.data.StatisticData;

import java.util.concurrent.ConcurrentHashMap;

public class ComputeData {

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, StatisticData>> map = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, StatisticData>>();

    public /*synchronized*/ void setRequestData(StatisticData requestData) {

        if (map.containsKey(requestData.getTenantId())) {
            ConcurrentHashMap<String, StatisticData> m = map.get(requestData.getTenantId());

            if (m.containsKey(requestData.getWebappName())) {
                    StatisticData st = updateRequestsData(requestData, m.get(requestData.getWebappName()));
                    m.put(requestData.getWebappName(), st);
                    map.put(requestData.getTenantId(),m);

            } else {
                m.put(requestData.getWebappName(), requestData);
                map.put(requestData.getTenantId(), m);
            }

        } else {
            ConcurrentHashMap<String, StatisticData> m = new ConcurrentHashMap<String, StatisticData>();
            m.put(requestData.getWebappName(), requestData);
            map.put(requestData.getTenantId(), m);
        }

    }

    private StatisticData updateRequestsData(StatisticData requestStatisticData, StatisticData storedStatisticData) {

        int reqCount = storedStatisticData.getRequstCount() + 1;
        int resCount = storedStatisticData.getResponseCount() + requestStatisticData.getResponseCount();
        int fauCount = storedStatisticData.getFaultCount() + requestStatisticData.getFaultCount();

        double averageResponseTime = (storedStatisticData.getAverageResponseTime() * (reqCount - 1) + requestStatisticData.getResponseTime()) / reqCount;

        if (storedStatisticData.getMaximumResponseTime() < requestStatisticData.getResponseTime()) {
            storedStatisticData.setMaximumResponseTime(requestStatisticData.getResponseTime());
        }

        if (storedStatisticData.getMinimumresponseTime() > requestStatisticData.getResponseTime()) {
            storedStatisticData.setMinimumresponseTime(requestStatisticData.getResponseTime());
        }

        storedStatisticData.setRequstCount(reqCount);
        storedStatisticData.setResponseCount(resCount);
        storedStatisticData.setFaultCount(fauCount);
        storedStatisticData.setAverageResponseTime(averageResponseTime);

        return storedStatisticData;
    }
}
