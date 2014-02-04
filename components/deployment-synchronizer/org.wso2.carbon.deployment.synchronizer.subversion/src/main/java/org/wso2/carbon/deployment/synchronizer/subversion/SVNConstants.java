/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.synchronizer.subversion;

import static org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER;

public class SVNConstants {

    // Configuration parameters for carbon.xml
    public static final String SVN_URL =
            DEPLOYMENT_SYNCHRONIZER + ".SvnUrl";
    public static final String SVN_USER =
            DEPLOYMENT_SYNCHRONIZER + ".SvnUser";
    public static final String SVN_PASSWORD =
            DEPLOYMENT_SYNCHRONIZER + ".SvnPassword";
    public static final String SVN_CLIENT =
            DEPLOYMENT_SYNCHRONIZER + ".SvnClient";
    public static final String SVN_IGNORE_EXTERNALS =
            DEPLOYMENT_SYNCHRONIZER + ".SvnIgnoreExternals";
    public static final String SVN_FORCE_UPDATE =
            DEPLOYMENT_SYNCHRONIZER + ".SvnForceUpdate";
    public static final String SVN_URL_APPEND_TENANT_ID =
            DEPLOYMENT_SYNCHRONIZER + ".SvnUrlAppendTenantId";

}
