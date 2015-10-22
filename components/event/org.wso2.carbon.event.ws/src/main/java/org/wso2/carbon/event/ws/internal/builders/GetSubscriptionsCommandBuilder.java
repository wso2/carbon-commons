package org.wso2.carbon.event.ws.internal.builders;

import java.util.Calendar;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.ws.internal.util.EventingConstants;

@Deprecated
public class GetSubscriptionsCommandBuilder {
    private static SOAPFactory fac = OMAbstractFactory.getSOAP12Factory();

    public static SOAPEnvelope buildResponseforGetSubscriptions(List<Subscription> subscriptions, int maxResultCount, int startIndex){
        OMNamespace wseexns = fac.createOMNamespace(EventingConstants.WSE_EXTENDED_EVENTING_NS,"wseex");
        
        OMElement response = fac.createOMElement("getSubscriptionsResponse",wseexns);
        
        int addedCount = 0;
        
        for(int i = startIndex; i< Math.min(subscriptions.size(), startIndex+ maxResultCount); i++){
            Subscription subscription = subscriptions.get(i);
            OMElement subsciptionEle = fac.createOMElement("subscriptionDetail", wseexns, response);
            //fac.createOMElement("dialect", wseexns,subsciptionEle).setText("");
            fac.createOMElement("subscriptionId", wseexns,subsciptionEle).setText(subscription.getId());
            fac.createOMElement("eventSinkAddress", wseexns,subsciptionEle).setText(subscription.getEventSinkURL());
            if(subscription.getExpires() != null){
                //Time time = new Ti
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(subscription.getExpires().getTimeInMillis());
                fac.createOMElement("subscriptionEndingTime", wseexns,subsciptionEle).setText(ConverterUtil.convertToString(calendar));    
            }
            fac.createOMElement("topic", wseexns,subsciptionEle).setText(subscription.getTopicName());
            if(subscription.getCreatedTime() != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(subscription.getCreatedTime().getTime());
                fac.createOMElement("createdTime", wseexns,subsciptionEle)
                    .setText(ConverterUtil.convertToString(calendar));
            }
            addedCount++;
        }
        
        response.addAttribute("allRequestCount", String.valueOf(subscriptions.size()), null);
        if(startIndex+ maxResultCount < subscriptions.size()){
            response.addAttribute("hasMoreResults", "true", null);
        }
        
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        envelope.getBody().addChild(response);
        return envelope;
    }
}
