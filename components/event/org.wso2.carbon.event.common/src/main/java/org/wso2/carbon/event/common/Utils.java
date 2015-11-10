package org.wso2.carbon.event.common;

import static org.wso2.carbon.event.common.Constants.*;

@Deprecated
public class Utils {
    public static String getSecureTopicPermissionPath(String topicName){
        if(!topicName.startsWith("/")){
            return new StringBuffer().append(SECURE_TOPIC_RESOURCE_PREFIX).append("/").append(topicName).toString();    
        }else{
            return new StringBuffer().append(SECURE_TOPIC_RESOURCE_PREFIX).append(topicName).toString();
        }
    }
}
