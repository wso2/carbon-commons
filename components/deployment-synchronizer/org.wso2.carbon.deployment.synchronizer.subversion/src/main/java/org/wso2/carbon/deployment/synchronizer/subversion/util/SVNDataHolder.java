/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.deployment.synchronizer.subversion.util;

import org.wso2.carbon.tomcat.api.CarbonTomcatService;

/**
 * This singleton data holder contains the data required to read webapp deployment directories
 *
 */
public class SVNDataHolder {

    private static SVNDataHolder svnDataHolderInstance = new SVNDataHolder();
    private CarbonTomcatService carbonTomcatService;

    public static SVNDataHolder getInstance(){
        return svnDataHolderInstance;
    }
    public void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        this.carbonTomcatService = carbonTomcatService;
    }

    public CarbonTomcatService getCarbonTomcatService() {
        return this.carbonTomcatService;
    }
}