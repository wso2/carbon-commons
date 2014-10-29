/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery.tests.om;

import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.messages.Probe;
import org.wso2.carbon.discovery.messages.Notification;
import org.wso2.carbon.discovery.messages.Resolve;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.util.UIDGenerator;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.net.URI;
import java.io.IOException;

public class MessageSerializerTest extends XMLTestCase {

    private static final URI[] scopes = new URI[] {
            URI.create("http://wso2.org/engineering"),
            URI.create("http://wso2.org/engineering/qa"),
            URI.create("http://wso2.org/marketing")
    };

    private static final URI[] xaddr = new URI[] {
            URI.create("http://10.100.3.25:9763/services/FooService"),
            URI.create("https://10.100.3.25:9443/services/FooService"),
            URI.create("jms:/FooService?param1=value1&param2=value2")
    };

    private static final QName[] types = new QName[] {
            new QName("http://uri1.com", "MyType"),
            new QName("http://test.org", "YourType", "yt")
    };

    private static final String uuid = UIDGenerator.generateURNString();

    public void testHelloSerialization() throws Exception {
        String input =
                "<wsd:Hello xmlns:wsd=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\">" +
                        "<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                        "<wsa:Address>" + uuid + "</wsa:Address>" +
                        "</wsa:EndpointReference>" +
                        "<wsd:Types " +
                        "xmlns:yt=\"http://test.org\" xmlns:axis2ns1=\"http://uri1.com\">" +
                        "axis2ns1:MyType yt:YourType</wsd:Types>" +
                        "<wsd:Scopes>" + getURIList(scopes) + "</wsd:Scopes>" +                        
                        "<wsd:XAddrs>" + getURIList(xaddr) + "</wsd:XAddrs>" +
                        "<wsd:MetadataVersion>1</wsd:MetadataVersion>" +
                        "</wsd:Hello>";
        System.out.println(getURIList(xaddr));
        OMElement original = AXIOMUtil.stringToOM(input);
        Notification hello = DiscoveryOMUtils.getHelloFromOM(original);
        assertEquals(DiscoveryConstants.NOTIFICATION_TYPE_HELLO, hello.getType());
        TargetService service = hello.getTargetService();
        assertEquals(1, service.getMetadataVersion());
        assertEquals(uuid, service.getEpr().getAddress());
        assertURIArrayEquals(scopes, service.getScopes());
        assertURIArrayEquals(xaddr, service.getXAddresses());
        assertQNameArrayEquals(types, service.getTypes());

        OMElement serialization = DiscoveryOMUtils.toOM(hello, OMAbstractFactory.getSOAP11Factory());
        System.out.println(serialization.toString());
        assertXMLEquals(input, serialization);
    }

     public void testByeSerialization() throws Exception {
        String input =
                "<wsd:Bye xmlns:wsd=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\">" +
                        "<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                        "<wsa:Address>" + uuid + "</wsa:Address>" +
                        "</wsa:EndpointReference>" +
                        "<wsd:Types " +
                        "xmlns:yt=\"http://test.org\" xmlns:axis2ns1=\"http://uri1.com\">" +
                        "axis2ns1:MyType yt:YourType</wsd:Types>" +
                        "<wsd:Scopes>" + getURIList(scopes) + "</wsd:Scopes>" +
                        "<wsd:XAddrs>" + getURIList(xaddr) + "</wsd:XAddrs>" +
                        "<wsd:MetadataVersion>1</wsd:MetadataVersion>" +
                        "</wsd:Bye>";

        OMElement original = AXIOMUtil.stringToOM(input);
        Notification bye = DiscoveryOMUtils.getByeFromOM(original);
        assertEquals(DiscoveryConstants.NOTIFICATION_TYPE_BYE, bye.getType());
        TargetService service = bye.getTargetService();
        assertEquals(1, service.getMetadataVersion());
        assertEquals(uuid, service.getEpr().getAddress());
        assertURIArrayEquals(scopes, service.getScopes());
        assertURIArrayEquals(xaddr, service.getXAddresses());
        assertQNameArrayEquals(types, service.getTypes());

        OMElement serialization = DiscoveryOMUtils.toOM(bye, OMAbstractFactory.getSOAP11Factory());
        System.out.println(serialization.toString());
        assertXMLEquals(input, serialization);
    }

    public void testProbeSerialization() throws Exception {
        String input =
                "<wsd:Probe xmlns:wsd=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\">" +
                        "<wsd:Types " +
                        "xmlns:yt=\"http://test.org\" xmlns:axis2ns1=\"http://uri1.com\">" +
                        "axis2ns1:MyType yt:YourType</wsd:Types>" +
                        "<wsd:Scopes>" + getURIList(scopes) + "</wsd:Scopes>" +
                        "</wsd:Probe>";
        System.out.println(getURIList(xaddr));
        OMElement original = AXIOMUtil.stringToOM(input);
        Probe probe = DiscoveryOMUtils.getProbeFromOM(original);
        assertURIArrayEquals(scopes, probe.getScopes());
        assertQNameArrayEquals(types, probe.getTypes());

        OMElement serialization = DiscoveryOMUtils.toOM(probe, OMAbstractFactory.getSOAP11Factory());
        System.out.println(serialization.toString());
        assertXMLEquals(input, serialization);   
    }

    public void testResolveSerialization() throws Exception {
        String input =
                "<wsd:Resolve xmlns:wsd=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\">" +
                        "<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
                        "<wsa:Address>" + uuid + "</wsa:Address>" +
                        "</wsa:EndpointReference>" +
                        "</wsd:Resolve>";

        OMElement original = AXIOMUtil.stringToOM(input);
        Resolve resolve = DiscoveryOMUtils.getResolveFromOM(original);
        assertEquals(uuid, resolve.getEpr().getAddress());

        OMElement serialization = DiscoveryOMUtils.toOM(resolve, OMAbstractFactory.getSOAP11Factory());
        System.out.println(serialization.toString());
        assertXMLEquals(input, serialization);
    }

    private void assertXMLEquals(String expected, OMElement actual) {
        try {
            assertXMLEqual(expected, actual.toString());
        } catch (SAXException e) {
            fail("Error while parsing the XML content: " + e.getMessage());
        } catch (IOException e) {
            fail("IO error while parsing the XML content: " + e.getMessage());
        }
    }

    private void assertURIArrayEquals(URI[] expected, URI[] actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    private void assertQNameArrayEquals(QName[] expected, QName[] actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i]);
        }
    }

    private String getURIList(URI[] list) {
        StringBuffer str = new StringBuffer();
        boolean first = true;
        for (URI uri : list) {
            if (!first) {
                str.append(" ");
            }
            str.append(uri.toString());
            first = false;
        }
        return str.toString().replace("&", "&amp;");
    }
}
