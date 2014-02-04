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

package org.wso2.carbon.deployment.synchronizer.s3;

import org.wso2.carbon.deployment.synchronizer.DeploymentSynchronizerConstants;

public class S3Constants {

    public static final String AWS_ACCESS_KEY_ID =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSAccessKeyId";
    public static final String AWS_SECRET_KEY =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSSecretKey";

    public static final String AWS_CONNECTION_TIMEOUT =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSConnectionTimeout";
    public static final String AWS_SOCKET_TIMEOUT =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSSocketTimeout";
    public static final String AWS_MAX_CONNECTIONS =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSMaxConnections";
    public static final String AWS_PROTOCOL =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSProtocol";
    public static final String AWS_USER_AGENT =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".AWSUserAgent";
    public static final String BUCKET_NAME_PREFIX =
            DeploymentSynchronizerConstants.DEPLOYMENT_SYNCHRONIZER + ".BucketNamePrefix";

    public static final String DEFAULT_BUCKET_NAME_PREFIX =
            "wso2-carbon-deployment-synchronizer-tenant-";

    public static final String OBJECT_KEY_PREFIX = "dsync:";

}
