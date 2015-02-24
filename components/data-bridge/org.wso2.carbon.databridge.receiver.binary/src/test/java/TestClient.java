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

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;

public class TestClient {

    public static void main(String[] args) {
        try {
            System.setProperty("javax.net.ssl.trustStore", "/data/packs/test/bam250/incre/wso2bam-2.5.0/repository/resources/security/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket("localhost", 13000);

//            SocketFactory sslsocketfactory = SocketFactory.getDefault();
//            Socket sslsocket = sslsocketfactory.createSocket("localhost", 14000);

//            InputStream inputstream = System.in;
//            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

            OutputStream outputstream = sslsocket.getOutputStream();
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter(outputstream);
            BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);

            DataInputStream is = new DataInputStream(new BufferedInputStream(sslsocket.getInputStream()));


            String string = null;
//            while (true) {
            string = "xyzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz";
//                Thread.sleep(100);
            bufferedwriter.write(string + '\n');
            bufferedwriter.flush();

            byte[] receivedData = receive(is);
            String responseData = new String(receivedData);
            System.out.println("Server Response = " + responseData.trim());
//            }


        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static byte[] receive(DataInputStream is) throws Exception {
        try {
            byte[] inputData = new byte[1024];
            is.read(inputData);
            return inputData;
        } catch (Exception exception) {
            throw exception;
        }
    }
}
