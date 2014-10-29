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


import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.statistics.webapp.data.StatisticData;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import javax.servlet.ServletException;
import java.io.IOException;

public class RequestIntercepterValve extends ValveBase {


    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {

        Long startTime = System.currentTimeMillis();

        getNext().invoke(request, response);

        if(CarbonUtils.isWorkerNode()){
            return;
        }

        Long responseTime = System.currentTimeMillis() - startTime;

        String requestURI = request.getRequestURI().trim();

        //Extracting the tenant domain using the requested url.
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);


        StatisticData statisticData = new StatisticData();

        int firstDigit = Integer.parseInt(Integer.toString(response.getStatus()).substring(0, 1));

        statisticData.setRequstCount(1);
        if (firstDigit == 2 || firstDigit == 3) {
            statisticData.setResponseCount(1);
            statisticData.setFaultCount(0);
        } else if (firstDigit == 4 || firstDigit == 5) {
            statisticData.setResponseCount(0);
            statisticData.setFaultCount(1);
        }

        String[] requestedUriParts = requestURI.split("/");
        if(requestedUriParts == null){
            return;
        }
        if (requestedUriParts.length >= 5 && requestURI.startsWith("/t/")) {
            statisticData.setWebappName(requestedUriParts[4]);
            statisticData.setTenantName(requestedUriParts[2]);
        } else if (requestedUriParts.length >= 2 && !requestURI.startsWith("/t/")){
            statisticData.setWebappName(requestedUriParts[1]);
            statisticData.setTenantName(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } else{
            return;
        }

        //Extracting the configuration context. if tenant domain is null then main carbon server configuration is loaded
       /* ConfigurationContext currentCtx;
        if (tenantDomain != null) {
            currentCtx = getTenantConfigurationContext(tenantDomain);
        } else {
            currentCtx = CarbonDataHolder.getServerConfigContext();
        }*/

        //Requesting the tenant id, if this main carbon context id will be -1234
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        statisticData.setTenantId(tenantID);
        statisticData.setResponseTime(responseTime);

        ComputeData cd = new ComputeData();
        cd.setRequestData(statisticData);

    }



}
