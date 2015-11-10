package org.wso2.carbon.event.client.broker;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.http.SimpleHTTPServer;

import javax.xml.namespace.QName;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Deprecated
public class SimpleMessageReceiver{
    private SimpleHTTPServer axis2Server;
    private ConfigurationContext configContext;
    private EndpointReference eventSinkUrl;
    
    private EventCallback callback;
    
    public SimpleMessageReceiver(String repoLocation, String confLocation, EventCallback callback) throws AxisFault{
        configContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repoLocation,
                        confLocation);
    }
    
    public void start()throws AxisFault {
        try {
            //Register the callback service
            AxisService messageCollectorService = new AxisService("MessageCollector");
            MessageReceiver messageReceiver = new MessageReceiver() {
                public void receive(MessageContext messageCtx) throws AxisFault {
                    if(callback != null){
                        callback.mesageReceived(messageCtx.getEnvelope());
                    }else{
                        System.out.println("Received " + messageCtx.getEnvelope());    
                    }
                }
            };
            InOutAxisOperation operation1 = new InOutAxisOperation(new QName("receive"));
            operation1.setMessageReceiver(messageReceiver);
            messageCollectorService.addOperation(operation1);
            
            configContext.getAxisConfiguration().addService(messageCollectorService);
            
            axis2Server = new SimpleHTTPServer(configContext, 7777);
            axis2Server.start();
            
            eventSinkUrl = axis2Server.getEPRForService(messageCollectorService.getName(), InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            throw AxisFault.makeFault(e);
        }
    }
    
    public String getListenerUrl(){
        return eventSinkUrl.getAddress()+"receive/";
    }
    
    public static interface EventCallback{
        public void mesageReceived(SOAPEnvelope envelope);
    }
    
    public static void main(String[] args) throws Exception {
        String topic = "statisticsPublishTopic";
        String repoLocation = "/home/hemapani/playground/events/wso2carbon-4.0.0-SNAPSHOT/repository";
        
        String confFile = repoLocation + "/conf/axis2_client.xml";

        SimpleMessageReceiver messageReceiver = new SimpleMessageReceiver(repoLocation, confFile,null);
        messageReceiver.start();
        
        Thread.sleep(2000);
        
        BrokerClient brokerClient = new BrokerClient("http://parakum:6666/axis2/services/MessageCollector/receive/");
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement ele = fac.createOMElement(new QName("http://wso2.org","foo"));
        ele.setText("hello");
        brokerClient.publish(topic, ele);
    }
}
