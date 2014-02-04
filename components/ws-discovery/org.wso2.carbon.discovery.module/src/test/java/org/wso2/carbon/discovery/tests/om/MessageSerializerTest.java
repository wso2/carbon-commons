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
import org.wso2.carbon.discovery.messages.QueryMatch;
import org.wso2.carbon.discovery.messages.TargetService;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.DiscoveryException;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;

import javax.xml.namespace.QName;

public class MessageSerializerTest extends TestCase {

    public void testProbeMatchSerializer() {
        TargetService service = new TargetService(new EndpointReference("http://hello-world.com"));
        service.setMetadataVersion(5000);
        service.setTypes(getTypes());
        QueryMatch match = new QueryMatch(DiscoveryConstants.RESULT_TYPE_PROBE_MATCH,
                new TargetService[]{ service });
        try {
            OMElement matchElem = DiscoveryOMUtils.toOM(match, OMAbstractFactory.getSOAP11Factory());
            System.out.println(matchElem);
            QueryMatch copy = DiscoveryOMUtils.getProbeMatchFromOM(matchElem);
            System.out.println(copy.getTargetServices()[0].getMetadataVersion());
        } catch (DiscoveryException e) {
            fail("Error while serializing: " + e.getMessage());
        }
    }

    private QName[] getTypes() {
        return new QName[]{ new QName("http://uri1.com", "MyType"),
                                        new QName("http://test.org", "YourType", "yt")};
    }
}
