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

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConstants;
import org.wso2.carbon.databridge.commons.binary.BinaryMessageConverterUtil;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class TestClient2 {
    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "/data/packs/test/bam250/incre/wso2bam-2.5.0/repository/resources/security/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

//            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("localhost", 13000);

            SocketFactory sslsocketfactory = SocketFactory.getDefault();
            Socket sslsocket = sslsocketfactory.createSocket("localhost", 14000);

//            InputStream inputstream = System.in;
//            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            String string = null;
//            while (true) {
                Event event = new Event();
                event.setStreamId(DataBridgeCommonsUtils.generateStreamId("org.wso2.test.stream", "1.0.0"));
                event.setMetaData(new Object[]{"127.0.0.1"});
                event.setCorrelationData(null);
                event.setPayloadData(new Object[]{"WSO2", 123.4, 2, 12.4, 1.3});

//                Thread.sleep(100);
//                String eventStr = BinaryMessageConverterUtil.convertBinaryMessage(event, "12345679", -1234,
//                        BinaryMessageConstants.PUBLISH_OPERATION);
//                System.out.println(eventStr);
//                bufferedwriter.write(eventStr);
//                bufferedwriter.flush();
//            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
