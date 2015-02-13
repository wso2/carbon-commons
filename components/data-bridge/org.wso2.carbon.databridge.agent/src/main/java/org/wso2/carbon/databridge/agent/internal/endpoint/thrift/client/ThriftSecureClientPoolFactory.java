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
package org.wso2.carbon.databridge.agent.internal.endpoint.thrift.client;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentSecurityException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.internal.client.AbstractSecureClientPoolFactory;
import org.wso2.carbon.databridge.agent.internal.conf.DataEndpointConfiguration;
import org.wso2.carbon.databridge.commons.thrift.service.secure.ThriftSecureEventTransmissionService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ThriftSecureClientPoolFactory extends AbstractSecureClientPoolFactory {
    private TSSLTransportFactory.TSSLTransportParameters params;

    public ThriftSecureClientPoolFactory(String trustStore, String trustStorePassword) {
        super(trustStore, trustStorePassword);
    }

    @Override
    public Object createClient(String protocol, String hostName, int port) throws
            DataEndpointAgentSecurityException{
        String trustStore, trustStorePw;
        if (protocol.equalsIgnoreCase(DataEndpointConfiguration.Protocol.SSL.toString())) {
            if (params == null) {
                if (getTrustStore() == null) {
                    trustStore = System.getProperty("javax.net.ssl.trustStore");
                    if (trustStore == null) {
                        throw new DataEndpointAgentSecurityException("No trustStore found");
                    } else {
                        setTrustStore(trustStore);
                    }
                }

                if (getTrustStorePassword() == null) {
                    trustStorePw = System.getProperty("javax.net.ssl.trustStorePassword");
                    if (trustStorePw == null) {
                        throw new DataEndpointAgentSecurityException("No trustStore password found");
                    } else {
                        setTrustStorePassword(trustStorePw);
                    }
                }


                params = new TSSLTransportFactory.TSSLTransportParameters();
                params.setTrustStore(getTrustStore(), getTrustStorePassword());
            }

            TTransport receiverTransport = null;
            try {
                receiverTransport = TSSLTransportFactory.
                        getClientSocket(hostName, port, 0, params);
                TProtocol tProtocol = new TBinaryProtocol(receiverTransport);
                return new ThriftSecureEventTransmissionService.Client(tProtocol);
            } catch (TTransportException e) {
                throw new DataEndpointAgentSecurityException("Error while trying to connect to " + protocol + "://" + hostName + ":" + port,
                        e);
            }
        }
        throw new DataEndpointAgentSecurityException("Unsupported protocol :"+protocol
                +" used to authenticate the client, only "+ DataEndpointConfiguration.Protocol.SSL.toString()
                +" is supported");
//        else {
//            //TODO:Error  thrown when connecting in http in tests...
//            try {
//                TrustManager easyTrustManager = new X509TrustManager() {
//                    public void checkClientTrusted(
//                            java.security.cert.X509Certificate[] x509Certificates,
//                            String s)
//                            throws java.security.cert.CertificateException {
//                    }
//
//                    public void checkServerTrusted(
//                            java.security.cert.X509Certificate[] x509Certificates,
//                            String s)
//                            throws java.security.cert.CertificateException {
//                    }
//
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return null;
//                    }
//                };
//                SSLContext sslContext = SSLContext.getInstance("TLS");
//                sslContext.init(null, new TrustManager[]{easyTrustManager}, null);
//                SSLSocketFactory sf = new SSLSocketFactory(sslContext);
//                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//                Scheme httpsScheme = new Scheme("https", sf, port);
//
//                DefaultHttpClient client = new DefaultHttpClient();
//                client.getConnectionManager().getSchemeRegistry().register(httpsScheme);
//
//                THttpClient tclient = new THttpClient("https://" + hostName + ":" + port + "/securedThriftReceiver", client);
//                TProtocol tProtocol = new TCompactProtocol(tclient);
//                ThriftSecureEventTransmissionService.Client authClient = new ThriftSecureEventTransmissionService.Client(tProtocol);
//                tclient.open();
//                return authClient;
//            } catch (Exception e) {
//                throw new DataEndpointAgentSecurityException("Cannot create Secure client for " +
//                        "https://" + hostName + ":" + port + "/securedThriftReceiver", e);
//            }
//        }
    }

    @Override
    public boolean validateClient(Object client) {
        ThriftSecureEventTransmissionService.Client thriftClient = (ThriftSecureEventTransmissionService.Client) client;
        return thriftClient.getOutputProtocol().getTransport().isOpen();
    }

    @Override
    public void terminateClient(Object client) {
        ThriftSecureEventTransmissionService.Client thriftClient = (ThriftSecureEventTransmissionService.Client) client;
        thriftClient.getOutputProtocol().getTransport().close();
    }
}
