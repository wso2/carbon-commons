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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.File;

public class S3Utils {

    public static String getBucketNamePrefix() {
        String prefix = S3Constants.DEFAULT_BUCKET_NAME_PREFIX;
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String value = serverConfig.getFirstProperty(S3Constants.BUCKET_NAME_PREFIX);
        if (value != null) {
            prefix = value;
        }
        return prefix;
    }

    public static ClientConfiguration getClientConfiguration() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();

        ClientConfiguration clientConfig = new ClientConfiguration();
        String value = serverConfig.getFirstProperty(S3Constants.AWS_CONNECTION_TIMEOUT);
        if (value != null) {
            clientConfig.setConnectionTimeout(Integer.parseInt(value));
        }

        value = serverConfig.getFirstProperty(S3Constants.AWS_SOCKET_TIMEOUT);
        if (value != null) {
            clientConfig.setSocketTimeout(Integer.parseInt(value));
        }

        value = serverConfig.getFirstProperty(S3Constants.AWS_MAX_CONNECTIONS);
        if (value != null) {
            clientConfig.setMaxConnections(Integer.parseInt(value));
        }

        value = serverConfig.getFirstProperty(S3Constants.AWS_PROTOCOL);
        if (value != null) {
            clientConfig.setProtocol(Protocol.valueOf(value));
        }

        value = serverConfig.getFirstProperty(S3Constants.AWS_USER_AGENT);
        if (value != null) {
            clientConfig.setUserAgent(value);
        }
        return clientConfig;
    }

    public static String getKeyFromFile(File root, File child) {
        String relativePath = root.toURI().relativize(child.toURI()).getPath();
        return S3Constants.OBJECT_KEY_PREFIX + relativePath;
    }

    public static File getFileFromKey(File root, String s3key) {
        String relativePath = s3key.substring(S3Constants.OBJECT_KEY_PREFIX.length());
        return new File(root, relativePath);
    }
}
