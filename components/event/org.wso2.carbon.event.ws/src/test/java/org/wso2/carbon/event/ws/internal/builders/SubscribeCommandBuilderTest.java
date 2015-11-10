/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.ws.internal.builders;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidExpirationTimeException;
import org.wso2.carbon.event.ws.internal.utils.CommandBuilderTestUtils;
import org.wso2.carbon.event.core.subscription.Subscription;

import java.util.Calendar;
import java.util.TimeZone;

@Deprecated
public class SubscribeCommandBuilderTest extends TestCase {

     /**
     * The UnSubscribe request payload according to the WS-Eventing specification.
     * http://www.w3.org/Submission/WS-Eventing/#Table8
     */
    protected final String REQUEST_PAYLOAD_SOAP12 =
             "<s12:Envelope\n" +
             "    xmlns:s12=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
             "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
             "    xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\"\n" +
             "    xmlns:ew=\"http://www.example.com/warnings\" >\n" +
             "  <s12:Header>\n" +
             "    <wsa:Action>\n" +
             "      http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe\n" +
             "    </wsa:Action>\n" +
             "    <wsa:MessageID>\n" +
             "      uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180\n" +
             "    </wsa:MessageID>\n" +
             "    <wsa:ReplyTo>\n" +
             "     <wsa:Address>http://www.example.com/MyEvEntsink</wsa:Address>\n" +
             "     <wsa:ReferenceProperties>\n" +
             "       <ew:MySubscription>2597</ew:MySubscription>\n" +
             "     </wsa:ReferenceProperties>\n" +
             "    </wsa:ReplyTo>\n" +
             "    <wsa:To>http://www.example.org/oceanwatch/EventSource</wsa:To>\n" +
             "  </s12:Header>\n" +
             "  <s12:Body>\n" +
             "    <wse:Subscribe>\n" +
             "      <wse:EndTo>\n" +
             "        <wsa:Address>\n" +
             "          http://www.example.com/MyEventSink\n" +
             "        </wsa:Address>\n" +
             "        <wsa:ReferenceProperties>\n" +
             "          <ew:MySubscription>2597</ew:MySubscription>\n" +
             "        </wsa:ReferenceProperties>\n" +
             "      </wse:EndTo>\n" +
             "      <wse:Delivery>\n" +
             "        <wse:NotifyTo>\n" +
             "          <wsa:Address>\n" +
             "            http://www.other.example.com/OnStormWarning\n" +
             "          </wsa:Address>\n" +
             "          <wsa:ReferenceProperties>\n" +
             "            <ew:MySubscription>2597</ew:MySubscription>\n" +
             "          </wsa:ReferenceProperties>\n" +
             "        </wse:NotifyTo>\n" +
             "      </wse:Delivery>\n" +
             "      <wse:Expires>2034-06-26T21:07:00.000-08:00</wse:Expires>\n" +
             "      <wse:Filter xmlns:ow=\"http://www.example.org/oceanwatch\"\n" +
             "          Dialect=\"http://www.example.org/topicFilter\" >\n" +
             "        weather.storms\n" +
             "      </wse:Filter>\n" +
             "    </wse:Subscribe>\n" +
             "  </s12:Body>\n" +
             "</s12:Envelope>";

    protected final String REQUEST_PAYLOAD_SOAP12_EXPIRED =
             "<s12:Envelope\n" +
             "    xmlns:s12=\"http://www.w3.org/2003/05/soap-envelope\"\n" +
             "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
             "    xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\"\n" +
             "    xmlns:ew=\"http://www.example.com/warnings\" >\n" +
             "  <s12:Header>\n" +
             "    <wsa:Action>\n" +
             "      http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe\n" +
             "    </wsa:Action>\n" +
             "    <wsa:MessageID>\n" +
             "      uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180\n" +
             "    </wsa:MessageID>\n" +
             "    <wsa:ReplyTo>\n" +
             "     <wsa:Address>http://www.example.com/MyEvEntsink</wsa:Address>\n" +
             "     <wsa:ReferenceProperties>\n" +
             "       <ew:MySubscription>2597</ew:MySubscription>\n" +
             "     </wsa:ReferenceProperties>\n" +
             "    </wsa:ReplyTo>\n" +
             "    <wsa:To>http://www.example.org/oceanwatch/EventSource</wsa:To>\n" +
             "  </s12:Header>\n" +
             "  <s12:Body>\n" +
             "    <wse:Subscribe>\n" +
             "      <wse:EndTo>\n" +
             "        <wsa:Address>\n" +
             "          http://www.example.com/MyEventSink\n" +
             "        </wsa:Address>\n" +
             "        <wsa:ReferenceProperties>\n" +
             "          <ew:MySubscription>2597</ew:MySubscription>\n" +
             "        </wsa:ReferenceProperties>\n" +
             "      </wse:EndTo>\n" +
             "      <wse:Delivery>\n" +
             "        <wse:NotifyTo>\n" +
             "          <wsa:Address>\n" +
             "            http://www.other.example.com/OnStormWarning\n" +
             "          </wsa:Address>\n" +
             "          <wsa:ReferenceProperties>\n" +
             "            <ew:MySubscription>2597</ew:MySubscription>\n" +
             "          </wsa:ReferenceProperties>\n" +
             "        </wse:NotifyTo>\n" +
             "      </wse:Delivery>\n" +
             "      <wse:Expires>2004-06-26T21:07:00.000-08:00</wse:Expires>\n" +
             "      <wse:Filter xmlns:ow=\"http://www.example.org/oceanwatch\"\n" +
             "          Dialect=\"http://www.example.org/topicFilter\" >\n" +
             "        weather.storms\n" +
             "      </wse:Filter>\n" +
             "    </wse:Subscribe>\n" +
             "  </s12:Body>\n" +
             "</s12:Envelope>";

    protected final String REQUEST_PAYLOAD_SOAP11 =
            "<soapenv:Envelope\n" +
            "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
            "    xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\"\n" +
            "    xmlns:ew=\"http://www.example.com/warnings\" >\n" +
            "    xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/\n" +
            "                        http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soapenv:Header>\n" +
            "    <wsa:Action>\n" +
            "      http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe\n" +
            "    </wsa:Action>\n" +
            "    <wsa:MessageID>\n" +
            "      uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180\n" +
            "    </wsa:MessageID>\n" +
            "    <wsa:ReplyTo>\n" +
            "     <wsa:Address>http://www.example.com/MyEvEntsink</wsa:Address>\n" +
            "     <wsa:ReferenceProperties>\n" +
            "       <ew:MySubscription>2597</ew:MySubscription>\n" +
            "     </wsa:ReferenceProperties>\n" +
            "    </wsa:ReplyTo>\n" +
            "    <wsa:To>http://www.example.org/oceanwatch/EventSource</wsa:To>\n" +
            "  </soapenv:Header>\n" +
            "  <soapenv:Body>\n" +
            "    <wse:Subscribe>\n" +
            "      <wse:EndTo>\n" +
            "        <wsa:Address>\n" +
            "          http://www.example.com/MyEventSink\n" +
            "        </wsa:Address>\n" +
            "        <wsa:ReferenceProperties>\n" +
            "          <ew:MySubscription>2597</ew:MySubscription>\n" +
            "        </wsa:ReferenceProperties>\n" +
            "      </wse:EndTo>\n" +
            "      <wse:Delivery>\n" +
            "        <wse:NotifyTo>\n" +
            "          <wsa:Address>\n" +
            "            http://www.other.example.com/OnStormWarning\n" +
            "          </wsa:Address>\n" +
            "          <wsa:ReferenceProperties>\n" +
            "            <ew:MySubscription>2597</ew:MySubscription>\n" +
            "          </wsa:ReferenceProperties>\n" +
            "        </wse:NotifyTo>\n" +
            "      </wse:Delivery>\n" +
            "      <wse:Expires>2034-06-26T21:07:00.000-08:00</wse:Expires>\n" +
            "      <wse:Filter xmlns:ow=\"http://www.example.org/oceanwatch\"\n" +
            "          Dialect=\"http://www.example.org/topicFilter\" >\n" +
            "        weather.storms\n" +
            "      </wse:Filter>\n" +
            "    </wse:Subscribe>\n" +
            "  </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    protected final String REQUEST_PAYLOAD_SOAP11_EXPIRED =
            "<soapenv:Envelope\n" +
            "    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
            "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"\n" +
            "    xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\"\n" +
            "    xmlns:ew=\"http://www.example.com/warnings\" >\n" +
            "    xsi:schemaLocation=\"http://schemas.xmlsoap.org/soap/envelope/\n" +
            "                        http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            "  <soapenv:Header>\n" +
            "    <wsa:Action>\n" +
            "      http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe\n" +
            "    </wsa:Action>\n" +
            "    <wsa:MessageID>\n" +
            "      uuid:e1886c5c-5e86-48d1-8c77-fc1c28d47180\n" +
            "    </wsa:MessageID>\n" +
            "    <wsa:ReplyTo>\n" +
            "     <wsa:Address>http://www.example.com/MyEvEntsink</wsa:Address>\n" +
            "     <wsa:ReferenceProperties>\n" +
            "       <ew:MySubscription>2597</ew:MySubscription>\n" +
            "     </wsa:ReferenceProperties>\n" +
            "    </wsa:ReplyTo>\n" +
            "    <wsa:To>http://www.example.org/oceanwatch/EventSource</wsa:To>\n" +
            "  </soapenv:Header>\n" +
            "  <soapenv:Body>\n" +
            "    <wse:Subscribe>\n" +
            "      <wse:EndTo>\n" +
            "        <wsa:Address>\n" +
            "          http://www.example.com/MyEventSink\n" +
            "        </wsa:Address>\n" +
            "        <wsa:ReferenceProperties>\n" +
            "          <ew:MySubscription>2597</ew:MySubscription>\n" +
            "        </wsa:ReferenceProperties>\n" +
            "      </wse:EndTo>\n" +
            "      <wse:Delivery>\n" +
            "        <wse:NotifyTo>\n" +
            "          <wsa:Address>\n" +
            "            http://www.other.example.com/OnStormWarning\n" +
            "          </wsa:Address>\n" +
            "          <wsa:ReferenceProperties>\n" +
            "            <ew:MySubscription>2597</ew:MySubscription>\n" +
            "          </wsa:ReferenceProperties>\n" +
            "        </wse:NotifyTo>\n" +
            "      </wse:Delivery>\n" +
            "      <wse:Expires>2004-06-26T21:07:00.000-08:00</wse:Expires>\n" +
            "      <wse:Filter xmlns:ow=\"http://www.example.org/oceanwatch\"\n" +
            "          Dialect=\"http://www.example.org/topicFilter\" >\n" +
            "        weather.storms\n" +
            "      </wse:Filter>\n" +
            "    </wse:Subscribe>\n" +
            "  </soapenv:Body>\n" +
            "</soapenv:Envelope>\n";

    protected final String RESPONSE_PAYLOAD_SOAP11 = "<?xml version='1.0' encoding='utf-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>" +
            "<wse:SubscribeResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
            "<wse:SubscriptionManager>" +
            "<wsa:Address xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "http://www.example.org/oceanwatch/SubscriptionManager</wsa:Address>" +
            "<wsa:ReferenceParameters xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "<wse:Identifier>uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa</wse:Identifier>" +
            "</wsa:ReferenceParameters>" +
            "</wse:SubscriptionManager>" +
            "<wse:Expires>*</wse:Expires>" +
            "</wse:SubscribeResponse></soapenv:Body></soapenv:Envelope>";

    protected final String RESPONSE_PAYLOAD_SOAP12 = "<?xml version='1.0' encoding='utf-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
            "<soapenv:Body>" +
            "<wse:SubscribeResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
            "<wse:SubscriptionManager>" +
            "<wsa:Address xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "http://www.example.org/oceanwatch/SubscriptionManager</wsa:Address>" +
            "<wsa:ReferenceParameters xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "<wse:Identifier>uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa</wse:Identifier>" +
            "</wsa:ReferenceParameters>" +
            "</wse:SubscriptionManager>" +
            "<wse:Expires>*</wse:Expires>" +
            "</wse:SubscribeResponse></soapenv:Body></soapenv:Envelope>";

    protected final String RESPONSE_PAYLOAD_SOAP11_WITH_EXPIRY = "<?xml version='1.0' encoding='utf-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<soapenv:Body>" +
            "<wse:SubscribeResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
            "<wse:SubscriptionManager>" +
            "<wsa:Address xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "http://www.example.org/oceanwatch/SubscriptionManager</wsa:Address>" +
            "<wsa:ReferenceParameters xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "<wse:Identifier>uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa</wse:Identifier>" +
            "</wsa:ReferenceParameters>" +
            "</wse:SubscriptionManager>" +
            "<wse:Expires>2034-06-26T21:07:00.000-08:00</wse:Expires>" +
            "</wse:SubscribeResponse></soapenv:Body></soapenv:Envelope>";

    protected final String RESPONSE_PAYLOAD_SOAP12_WITH_EXPIRY = "<?xml version='1.0' encoding='utf-8'?>" +
            "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\">" +
            "<soapenv:Body>" +
            "<wse:SubscribeResponse xmlns:wse=\"http://schemas.xmlsoap.org/ws/2004/08/eventing\">" +
            "<wse:SubscriptionManager>" +
            "<wsa:Address xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "http://www.example.org/oceanwatch/SubscriptionManager</wsa:Address>" +
            "<wsa:ReferenceParameters xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">" +
            "<wse:Identifier>uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa</wse:Identifier>" +
            "</wsa:ReferenceParameters>" +
            "</wse:SubscriptionManager>" +
            "<wse:Expires>2034-06-26T21:07:00.000-08:00</wse:Expires>" +
            "</wse:SubscribeResponse></soapenv:Body></soapenv:Envelope>";

    public void testSOAP12EnvelopeToSubscription() throws Exception {
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP12Envelope();
        SubscribeCommandBuilder builder = new SubscribeCommandBuilder(mc);

        Subscription subscription = builder.toSubscription(
                CommandBuilderTestUtils.payloadToSOAP12Envelope(REQUEST_PAYLOAD_SOAP12));
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");

        assertNotNull("The subscription object is null", subscription);
        assertNotNull("The subscription id is null", subscription.getId());
        assertEquals("Invalid expiration time", 2034968820000L,
                subscription.getExpires().getTimeInMillis() +
                        TimeZone.getTimeZone("GMT-08:00").getOffset(0));
        assertEquals("Invalid time zone offset", TimeZone.getDefault().getOffset(0),
                subscription.getExpires().getTimeZone().getOffset(0));
        assertEquals("Invalid endpoint url", "http://www.other.example.com/OnStormWarning",
                        subscription.getEventSinkURL());

        Exception expected = null;
        try {
            builder.toSubscription(
                    CommandBuilderTestUtils.payloadToSOAP12Envelope(REQUEST_PAYLOAD_SOAP12_EXPIRED));
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull("The exception object is null", expected);
        assertTrue("Invalid exception generated for expired renew request",
                expected instanceof InvalidExpirationTimeException);
    }

    public void testSOAP11EnvelopeToSubscription() throws Exception {
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP11Envelope();
        SubscribeCommandBuilder builder = new SubscribeCommandBuilder(mc);

        Subscription subscription = builder.toSubscription(
                CommandBuilderTestUtils.payloadToSOAP11Envelope(REQUEST_PAYLOAD_SOAP11));
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");       

        assertNotNull("The subscription object is null", subscription);
        assertNotNull("The subscription id is null", subscription.getId());
        assertEquals("Invalid expiration time", 2034968820000L,
                subscription.getExpires().getTimeInMillis() +
                        TimeZone.getTimeZone("GMT-08:00").getOffset(0));
        assertEquals("Invalid time zone offset", TimeZone.getDefault().getOffset(0),
                subscription.getExpires().getTimeZone().getOffset(0));
        assertEquals("Invalid endpoint url", "http://www.other.example.com/OnStormWarning",
                        subscription.getEventSinkURL());


        Exception expected = null;
        try {
            builder.toSubscription(
                    CommandBuilderTestUtils.payloadToSOAP11Envelope(REQUEST_PAYLOAD_SOAP11_EXPIRED));
        } catch (Exception e) {
            expected = e;
        }
        assertNotNull("The exception object is null", expected);
        assertTrue("Invalid exception generated for expired renew request",
                expected instanceof InvalidExpirationTimeException);
    }

    public void testSubscriptionToSOAP12Envelope() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");
        subscription.setEventSinkURL("http://www.example.org/oceanwatch/SubscriptionManager");
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP12Envelope();
        SubscribeCommandBuilder builder = new  SubscribeCommandBuilder(mc);

        OMElement payload = builder.fromSubscription(subscription);

        assertEquals("Invalid response for the get status request", RESPONSE_PAYLOAD_SOAP12,
                payload.toString());
    }

    public void testSubscriptionToSOAP11Envelope() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");
        subscription.setEventSinkURL("http://www.example.org/oceanwatch/SubscriptionManager");
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP11Envelope();
        SubscribeCommandBuilder builder = new  SubscribeCommandBuilder(mc);

        OMElement payload = builder.fromSubscription(subscription);

        assertEquals("Invalid response for the get status request", RESPONSE_PAYLOAD_SOAP11,
                payload.toString());
    }

    public void testSubscriptionToSOAP12EnvelopeWithExpiry() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");
        subscription.setEventSinkURL("http://www.example.org/oceanwatch/SubscriptionManager");
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP12Envelope();
        SubscribeCommandBuilder builder = new  SubscribeCommandBuilder(mc);
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("GMT-08:00");
        calendar.setTimeZone(tz);
        calendar.setTimeInMillis(2034968820000L - TimeZone.getTimeZone("GMT-08:00").getOffset(0));
        subscription.setExpires(calendar);
        OMElement payload = builder.fromSubscription(subscription);

        assertEquals("Invalid response for the get status request", RESPONSE_PAYLOAD_SOAP12_WITH_EXPIRY,
                payload.toString());
    }

    public void testSubscriptionToSOAP11EnvelopeWithExpiry() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");
        subscription.setEventSinkURL("http://www.example.org/oceanwatch/SubscriptionManager");
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP11Envelope();
        SubscribeCommandBuilder builder = new  SubscribeCommandBuilder(mc);
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("GMT-08:00");
        calendar.setTimeZone(tz);
        calendar.setTimeInMillis(2034968820000L - TimeZone.getTimeZone("GMT-08:00").getOffset(0));
        subscription.setExpires(calendar);
        OMElement payload = builder.fromSubscription(subscription);

        assertEquals("Invalid response for the get status request", RESPONSE_PAYLOAD_SOAP11_WITH_EXPIRY,
                payload.toString());
    }

    public void testCarbonSubscriptionToSOAP12Envelope() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");
        subscription.setEventSinkURL("http://www.example.org/oceanwatch/SubscriptionManager");
        String id = subscription.getId();
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP12Envelope();
        SubscribeCommandBuilder builder = new  SubscribeCommandBuilder(mc);

        OMElement payload = builder.fromSubscription(subscription);

        String expectPayload = RESPONSE_PAYLOAD_SOAP12.replace(
                "uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa", id);

        assertEquals("Invalid response for the get status request", expectPayload,
                payload.toString());
    }

    public void testCarbonSubscriptionToSOAP11Envelope() throws Exception {
        Subscription subscription = new Subscription();
        subscription.setId("uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa");
        subscription.setEventSinkURL("http://www.example.org/oceanwatch/SubscriptionManager");        
        String id = subscription.getId();
        MessageContext mc = CommandBuilderTestUtils.getMCWithSOAP11Envelope();
        SubscribeCommandBuilder builder = new  SubscribeCommandBuilder(mc);

        OMElement payload = builder.fromSubscription(subscription);

        String expectPayload = RESPONSE_PAYLOAD_SOAP11.replace(
                "uuid:22e8a584-0d18-4228-b2a8-3716fa2097fa", id);

        assertEquals("Invalid response for the get status request", expectPayload,
                payload.toString());
    }
}
