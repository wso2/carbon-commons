package org.wso2.carbon.event.client.broker;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.databinding.types.URI;
import org.apache.axis2.databinding.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.client.AuthenticationClient;
import org.wso2.carbon.event.client.stub.generated.*;
import org.wso2.carbon.event.client.stub.generated.addressing.AttributedURI;
import org.wso2.carbon.event.client.stub.generated.addressing.EndpointReferenceType;
import org.wso2.carbon.event.client.stub.generated.addressing.ReferenceParametersType;
import org.wso2.carbon.event.client.stub.generated.authentication.AuthenticationExceptionException;
import org.wso2.carbon.event.common.Constants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.GregorianCalendar;

@Deprecated
public class BrokerClient {
    private static final Log log = LogFactory.getLog(BrokerClient.class);
    
    private static final String TOPIC_HEADER_NAME = "topic";
    
    private static final  String TOPIC_HEADER_NS = "http://wso2.org/ns/2009/09/eventing/notify";

    public static final String WSE_EVENTING_NS = "http://schemas.xmlsoap.org/ws/2004/08/eventing";
    public static final String WSE_EN_IDENTIFIER = "Identifier";
    
    private String brokerUrl;
    private static OMFactory fac = OMAbstractFactory.getOMFactory();
    private String cookie;
    private String propertyToSortBy;
    
    private ConfigurationContext configurationContext;

    public BrokerClient(String brokerUrl, String userName, String password)
            throws AxisFault, AuthenticationExceptionException {
        this(null, brokerUrl, userName, password);
    }


    public BrokerClient(ConfigurationContext configurationContext, String brokerUrl, String userName, String password)
            throws AxisFault, AuthenticationExceptionException {
        try {
            this.configurationContext = configurationContext;
            this.brokerUrl = brokerUrl;
            URL url = new URL(brokerUrl);

            String urlPath = url.getPath();

            //urlContext contains "/" as a prefix (e.g. /wsas)
            String urlContext = urlPath.substring(0, urlPath.indexOf("/services/"));
            String authAdminUrl = "https://" + url.getHost() + ":" + url.getPort()  /* "https://localhost:9443" */
                    + urlContext + "/services/AuthenticationAdmin";                 /* "/wsas/services/AuthenticationAdmin" */

            AuthenticationClient authenticationClient = new AuthenticationClient(configurationContext, authAdminUrl);

            if (authenticationClient.authenticate(userName, password)) {
                this.cookie = authenticationClient.getSessionCookie();
            } else {
                throw new AuthenticationExceptionException("authentication Fault");
            }
        } catch (RemoteException e) {
            throw AxisFault.makeFault(e);
        } catch (MalformedURLException e) {
            throw AxisFault.makeFault(e);
        }
    }

    public BrokerClient(ConfigurationContext configurationContext, String brokerUrl, String cookie) {
        this.configurationContext = configurationContext;
        this.brokerUrl = brokerUrl;
        this.cookie = cookie;
    }

    public BrokerClient(String brokerUrl, String cookie) {
        this.brokerUrl = brokerUrl;
        this.cookie = cookie;
    }

    public BrokerClient(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String subscribe(String topic, String eventSinkUrl)throws BrokerClientException{
        return subscribe(topic, eventSinkUrl, -1, null);
    }
    
    public String subscribe(String topic, String eventSinkUrl, long expirationTime, OMElement[] extensions)throws BrokerClientException{
        log.debug("Subscribed to "+ topic + " in "+ eventSinkUrl);
        try {
            // append the topic name at the end of the broker URL
            // so that it seems there is a seperate uri each event source
            if (!topic.startsWith("/")){
               topic = "/" + topic;
            }
            EventBrokerServiceStub service = new EventBrokerServiceStub(configurationContext, brokerUrl + topic);
            configureCookie(service._getServiceClient());
            EndpointReferenceType epr = new EndpointReferenceType();
            epr.setAddress(createURI(eventSinkUrl));
            
            DeliveryType deliveryType = new DeliveryType();
            EndpointReferenceType eventSink = new EndpointReferenceType();
            eventSink.setAddress(createURI(eventSinkUrl));
            deliveryType.setNotifyTo(eventSink);

            
            ExpirationType expirationType = null;
            if(expirationTime > 0){
                expirationType = new ExpirationType();
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis(expirationTime);
                expirationType.setObject(calendar);
            }

            FilterType filterType = new FilterType();
            filterType.setDialect(new URI("urn:someurl"));
            filterType.setString(topic);
            
            SubscribeResponse subscribeResponse = service.subscribe(epr, deliveryType, expirationType, filterType, extensions);
            ReferenceParametersType referenceParameters = subscribeResponse.getSubscriptionManager().getReferenceParameters();
            OMElement[] properties = referenceParameters.getExtraElement();
            
            String id = null;
            for(OMElement property:properties){
                if(property.getLocalName().equals("Identifier")){
                    id = property.getText();
                }
            }
            return id;
        } catch (AxisFault e) {
            throw new BrokerClientException("Error While Subscribing :"+e.getMessage(),e);
        } catch (MalformedURIException e) {
            throw new BrokerClientException("Error While Subscribing :"+e.getMessage(),e);
        } catch (RemoteException e) {
            throw new BrokerClientException("Error While Subscribing :"+e.getMessage(),e);
        }
    }
    
    public void publish(String topic, OMElement element) throws AxisFault{
        log.debug("published element to "+ topic );
        EventBrokerServiceStub service = new EventBrokerServiceStub(configurationContext, brokerUrl+"/publish/"+topic);
        configureCookie(service._getServiceClient());
        ServiceClient serviceClient = service._getServiceClient();

        OMElement header = fac.createOMElement(new QName(TOPIC_HEADER_NS, TOPIC_HEADER_NAME));
        header.setText(topic);
        serviceClient.addHeader(header);
        serviceClient.getOptions().setTo(new EndpointReference(brokerUrl+"/publish"));
        //serviceClient.getOptions().setTo(new EndpointReference(brokerUrl));
        serviceClient.getOptions().setAction("urn:publish");
        serviceClient.sendRobust(element);
    }

    /**
     * Publishes a message to a topic. The message should be XML structured.
     *
     * @param topic The name of the topic which the message should be published.
     * @param messageContent The message content.
     * @throws BrokerClientException
     */
    public void publish(String topic, String messageContent) throws BrokerClientException {
        try {
            StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(messageContent.getBytes()));
            OMElement message = builder.getDocumentElement();

            EventBrokerServiceStub service = new EventBrokerServiceStub(configurationContext, brokerUrl + "/publish/"
                                                                                              + topic);
            configureCookie(service._getServiceClient());
            ServiceClient serviceClient = service._getServiceClient();

            OMElement header = fac.createOMElement(new QName(TOPIC_HEADER_NS, TOPIC_HEADER_NAME));
            header.setText(topic);
            serviceClient.addHeader(header);
            serviceClient.getOptions().setTo(new EndpointReference(brokerUrl + "/publish"));
            serviceClient.getOptions().setAction("urn:publish");
            serviceClient.sendRobust(message);
        } catch (XMLStreamException e) {
            throw new BrokerClientException("Unable to convert message to OMElement. Make sure the message is an XML " +
                                            "message. :" + e.getMessage(), e);
        } catch (AxisFault e) {
            throw new BrokerClientException("Error while publishing message  : " + e.getMessage(), e);
        }
    }
    
    public void unsubscribe(String subscriptionID) throws RemoteException{
        log.debug("Unsubscribed to "+ subscriptionID);
        EventBrokerServiceStub service = new EventBrokerServiceStub(configurationContext, brokerUrl);
        configureCookie(service._getServiceClient());
        ServiceClient serviceClient = service._getServiceClient();
        OMElement header = fac.createOMElement(new QName(WSE_EVENTING_NS, WSE_EN_IDENTIFIER));
        header.setText(subscriptionID);
        serviceClient.addHeader(header);
        service.unsubscribe(new OMElement[]{});
    }
    
    public GetSubscriptionsResponse getAllSubscriptions(int maxRequestCount, String resultFilter, int firstIndex) throws RemoteException{
        EventBrokerServiceStub service = new EventBrokerServiceStub(configurationContext, brokerUrl);
        configureCookie(service._getServiceClient());
        return service.getSubscriptions(maxRequestCount, resultFilter, firstIndex);
    }

    public SubscriptionDetails[] getAllSubscriptions() throws RemoteException{
        GetSubscriptionsResponse allSubscriptions = getAllSubscriptions(Integer.MAX_VALUE,null,0);
        return allSubscriptions.getSubscriptionDetail();
    }
    
    private void configureCookie(ServiceClient client) throws AxisFault {
        if(cookie != null){
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
        
        if(propertyToSortBy != null){
            client.addStringHeader(Constants.SORTING_DATA, propertyToSortBy);
        }
    }
    

    public void renewSubscription(String subscriptionID, long time) throws RemoteException{
        log.debug("Renewed subscription "+ subscriptionID + " " + time);
        EventBrokerServiceStub service = new EventBrokerServiceStub(configurationContext, brokerUrl);
        
        configureCookie(service._getServiceClient());
        
        ServiceClient serviceClient = service._getServiceClient();
        OMElement header = fac.createOMElement(new QName(WSE_EVENTING_NS, WSE_EN_IDENTIFIER));
        header.setText(subscriptionID);
        serviceClient.addHeader(header);
        
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(time);
        
        ExpirationType expirationType = new ExpirationType();
        expirationType.setObject(calendar);

              
        RenewResponse renewOp = service.renewOp(expirationType, null);
        //TODO I think there is nothing to do with renewOp response
        
        
    }

    private static AttributedURI createURI(String uriAddress) throws MalformedURIException{
        AttributedURI address = new AttributedURI();
        address.setAnyURI(new URI(uriAddress));
        return address;
    }

}

