/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.receiver.binary.BinaryDataReceiver;
import org.wso2.carbon.databridge.receiver.binary.conf.BinaryDataReceiverConfiguration;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class TestServer {
    public static void main(String[] args) throws IOException, DataBridgeException {
        System.setProperty("Security.KeyStore.Location", "/data/packs/test/bam250/incre/wso2bam-2.5.0/repository/resources/security/wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");

        BinaryDataReceiver binaryDataReceiver = new BinaryDataReceiver( new BinaryDataReceiverConfiguration(13000, 14000, 10, 10),null);
        binaryDataReceiver.start();
    }
}
