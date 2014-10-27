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

import junit.framework.TestCase;
import org.wso2.carbon.discovery.messages.*;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class MessageProcessingTest extends TestCase {

    public void testHelloMessage() {
        String hello = "<dis:Hello xmlns:dis=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\"" +
                        "        xmlns:a=\"http://www.w3.org/2005/08/addressing\"" +
                        "        xmlns:i=\"http://some.ns.url.org\">\n" +
                        "   <a:EndpointReference>\n" +
                        "       <a:Address>urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119</a:Address>\n" +
                        "   </a:EndpointReference>\n" +
                        "   <dis:Types>i:PrintBasic i:PrintAdvanced</dis:Types>\n" +
                        "   <dis:Scopes>\n" +
                        "       ldap:///ou=engineering,o=exampleorg,c=us\n" +
                        "       ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=us\n" +
                        "       http://itdept/imaging/deployment/2004-12-04\n" +
                        "   </dis:Scopes>\n" +
                        "   <dis:XAddrs>http://prn-example/PRN42/b42-1668-a</dis:XAddrs>\n" +
                        "   <dis:MetadataVersion>75965</dis:MetadataVersion>\n" +
                        "</dis:Hello>";

        InputStream in = new ByteArrayInputStream(hello.getBytes());

        try {
            OMElement helloElement = new StAXOMBuilder(in).getDocumentElement();
            Notification notification = DiscoveryOMUtils.getHelloFromOM(helloElement);
            assertEquals(DiscoveryConstants.NOTIFICATION_TYPE_HELLO, notification.getType());
            assertEquals("urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119",
                    notification.getTargetService().getEpr().getAddress());
            assertEquals(75965, notification.getTargetService().getMetadataVersion());
            assertEquals(getScopes(), notification.getTargetService().getScopes());
            assertEquals(getTypes(), notification.getTargetService().getTypes());
            assertEquals(getXAddresses(), notification.getTargetService().getXAddresses());

        } catch (Exception e) {
            fail("Error while parsing the Hello element: " + e.getMessage());
        }
    }

    public void testByeMessage() {
        String hello = "<dis:Bye xmlns:dis=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\"" +
                        "        xmlns:a=\"http://www.w3.org/2005/08/addressing\"" +
                        "        xmlns:i=\"http://some.ns.url.org\">\n" +
                        "   <a:EndpointReference>\n" +
                        "       <a:Address>urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119</a:Address>\n" +
                        "   </a:EndpointReference>\n" +
                        "   <dis:Types>i:PrintBasic i:PrintAdvanced</dis:Types>\n" +
                        "   <dis:Scopes>\n" +
                        "       ldap:///ou=engineering,o=exampleorg,c=us\n" +
                        "       ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=us\n" +
                        "       http://itdept/imaging/deployment/2004-12-04\n" +
                        "   </dis:Scopes>\n" +
                        "   <dis:XAddrs>http://prn-example/PRN42/b42-1668-a</dis:XAddrs>\n" +
                        "   <dis:MetadataVersion>75965</dis:MetadataVersion>\n" +
                        "</dis:Bye>";

        InputStream in = new ByteArrayInputStream(hello.getBytes());

        try {
            OMElement byeElement = new StAXOMBuilder(in).getDocumentElement();
            Notification notification = DiscoveryOMUtils.getByeFromOM(byeElement);
            assertEquals(DiscoveryConstants.NOTIFICATION_TYPE_BYE, notification.getType());
            assertEquals("urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119",
                    notification.getTargetService().getEpr().getAddress());
            assertEquals(75965, notification.getTargetService().getMetadataVersion());
            assertEquals(getScopes(), notification.getTargetService().getScopes());
            assertEquals(getTypes(), notification.getTargetService().getTypes());
            assertEquals(getXAddresses(), notification.getTargetService().getXAddresses());

        } catch (Exception e) {
            fail("Error while parsing the Bye element: " + e.getMessage());
        }
    }

    public void testProbeMessage() {
        String probe = "<dis:Probe xmlns:dis=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\"" +
                        "        xmlns:i=\"http://some.ns.url.org\">\n" +
                        "   <dis:Types>i:PrintBasic i:PrintAdvanced</dis:Types>\n" +
                        "   <dis:Scopes>\n" +
                        "       ldap:///ou=engineering,o=exampleorg,c=us\n" +
                        "       ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=us\n" +
                        "       http://itdept/imaging/deployment/2004-12-04\n" +
                        "   </dis:Scopes>\n" +
                        "</dis:Probe>";

        InputStream in = new ByteArrayInputStream(probe.getBytes());

        try {
            OMElement helloElement = new StAXOMBuilder(in).getDocumentElement();
            Probe probeObj = DiscoveryOMUtils.getProbeFromOM(helloElement);
            assertEquals(getScopes(), probeObj.getScopes());
            assertEquals(getTypes(), probeObj.getTypes());            

        } catch (Exception e) {
            fail("Error while parsing the Probe element: " + e.getMessage());
        }
    }

    public void testResolveMessage() {
        String resolve = "<dis:Resolve xmlns:dis=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\"" +
                        "        xmlns:a=\"http://www.w3.org/2005/08/addressing\">" +
                        "   <a:EndpointReference>\n" +
                        "       <a:Address>urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119</a:Address>\n" +
                        "   </a:EndpointReference>\n" +
                        "</dis:Resolve>";

        InputStream in = new ByteArrayInputStream(resolve.getBytes());

        try {
            OMElement resolveElement = new StAXOMBuilder(in).getDocumentElement();
            Resolve resolveObj = DiscoveryOMUtils.getResolveFromOM(resolveElement);
            assertEquals("urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119",
                    resolveObj.getEpr().getAddress());
        } catch (Exception e) {
            fail("Error while parsing the Resolve element: " + e.getMessage());
        }
    }

    public void testResolveMatchMessage() {
        String resolveMatch ="<dis:ResolveMatches xmlns:dis=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\"" +
                            "        xmlns:a=\"http://www.w3.org/2005/08/addressing\"" +
                            "        xmlns:i=\"http://some.ns.url.org\">\n" +
                            "   <dis:ResolveMatch>\n" +
                            "     <a:EndpointReference>\n" +
                            "         <a:Address>urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119</a:Address>\n" +
                            "     </a:EndpointReference>\n" +
                            "     <dis:Types>i:PrintBasic i:PrintAdvanced</dis:Types>\n" +
                            "     <dis:Scopes>\n" +
                            "         ldap:///ou=engineering,o=exampleorg,c=us\n" +
                            "         ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=us\n" +
                            "         http://itdept/imaging/deployment/2004-12-04\n" +
                            "     </dis:Scopes>\n" +
                            "     <dis:XAddrs>http://prn-example/PRN42/b42-1668-a</dis:XAddrs>\n" +
                            "     <dis:MetadataVersion>75965</dis:MetadataVersion>\n" +
                            "   </dis:ResolveMatch>\n" +
                            "</dis:ResolveMatches>";

        InputStream in = new ByteArrayInputStream(resolveMatch.getBytes());
        try {
            OMElement resolveMatchElement = new StAXOMBuilder(in).getDocumentElement();
            QueryMatch match = DiscoveryOMUtils.getResolveMatchFromOM(resolveMatchElement);
            assertEquals(1, match.getTargetServices().length);
            TargetService service = match.getTargetServices()[0];
            assertEquals("urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119", service.getEpr().getAddress());
            assertEquals(getTypes(), service.getTypes());
            assertEquals(getXAddresses(), service.getXAddresses());
        } catch (Exception e) {
            fail("Error while parsing the ResolveMatches element: " + e.getMessage());
        }
    }

    public void testProbeMatchMessage() {
        String probeMatch = "<dis:ProbeMatches xmlns:dis=\"http://docs.oasis-open.org/ws-dd/ns/discovery/2009/01\"" +
                            "        xmlns:a=\"http://www.w3.org/2005/08/addressing\"" +
                            "        xmlns:i=\"http://some.ns.url.org\">\n" +
                            "   <dis:ProbeMatch>\n" +
                            "     <a:EndpointReference>\n" +
                            "         <a:Address>urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119</a:Address>\n" +
                            "     </a:EndpointReference>\n" +
                            "     <dis:Types>i:PrintBasic i:PrintAdvanced</dis:Types>\n" +
                            "     <dis:Scopes>\n" +
                            "         ldap:///ou=engineering,o=exampleorg,c=us\n" +
                            "         ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=us\n" +
                            "         http://itdept/imaging/deployment/2004-12-04\n" +
                            "     </dis:Scopes>\n" +
                            "     <dis:XAddrs>http://prn-example/PRN42/b42-1668-a</dis:XAddrs>\n" +
                            "     <dis:MetadataVersion>75965</dis:MetadataVersion>\n" +
                            "   </dis:ProbeMatch>\n" +
                            "   <dis:ProbeMatch>\n" +
                            "     <a:EndpointReference>\n" +
                            "         <a:Address>urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6120</a:Address>\n" +
                            "     </a:EndpointReference>\n" +
                            "     <dis:Types>i:PrintBasic i:PrintAdvanced</dis:Types>\n" +
                            "     <dis:Scopes>\n" +
                            "         ldap:///ou=engineering,o=exampleorg,c=uk\n" +
                            "         ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=uk\n" +
                            "         http://itdept/imaging/deployment/2004-12-04\n" +
                            "     </dis:Scopes>\n" +
                            "     <dis:XAddrs>http://prn-example/PRN42/b42-1668-b</dis:XAddrs>\n" +
                            "     <dis:MetadataVersion>75966</dis:MetadataVersion>\n" +
                            "   </dis:ProbeMatch>\n" +
                            "</dis:ProbeMatches>";

        InputStream in = new ByteArrayInputStream(probeMatch.getBytes());

        try {
            OMElement probeMatchElement = new StAXOMBuilder(in).getDocumentElement();
            QueryMatch match = DiscoveryOMUtils.getProbeMatchFromOM(probeMatchElement);
            assertEquals(2, match.getTargetServices().length);
            TargetService service1 = match.getTargetServices()[0];
            assertEquals("urn:uuid:98190dc2-0890-4ef8-ac9a-5940995e6119", service1.getEpr().getAddress());
            assertEquals(getTypes(), service1.getTypes());
            assertEquals(getScopes(), service1.getScopes());
            assertEquals(getXAddresses(), service1.getXAddresses());
        } catch (Exception e) {
            fail("Error while parsing the ProbeMatches element: " + e.getMessage());
        }
    }

    private void assertEquals(Object[] expected, Object[] found) {
        if (expected == null) {
            assertNull(found);
            return;
        } else {
            assertNotNull(found);
        }

        if (expected.length != found.length) {
            fail("The array lengths do not match");
        }

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], found[i]);
        }
    }

    private URI[] getScopes() {
        URI[] scopes = new URI[3];
        try {
            scopes[0] = new URI("ldap:///ou=engineering,o=exampleorg,c=us");
            scopes[1] = new URI("ldap:///ou=floor1,ou=b42,ou=anytown,o=exampleorg,c=us");
            scopes[2] = new URI("http://itdept/imaging/deployment/2004-12-04");
        } catch (URISyntaxException e) {
            // ignore
        }
        return scopes;
    }

    private QName[] getTypes() {
        QName[] types = new QName[2];
        types[0] = new QName("http://some.ns.url.org", "PrintBasic");
        types[1] = new QName("http://some.ns.url.org", "PrintAdvanced");
        return types;
    }

    private URI[] getXAddresses() {

        URI[] xaddr = new URI[1];
        try {
            xaddr[0] = new URI("http://prn-example/PRN42/b42-1668-a");
        } catch (URISyntaxException e) {
            // ignore
        }
        return xaddr;
    }

        
}
