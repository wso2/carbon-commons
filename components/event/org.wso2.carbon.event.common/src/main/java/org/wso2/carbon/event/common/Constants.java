package org.wso2.carbon.event.common;

import javax.xml.namespace.QName;

public class Constants {
    public static final String EXTSNSIONS_URI = "http://wso2.org/Services/extensions";
    public static QName SORTING_DATA = new QName(EXTSNSIONS_URI,"sortby");
    public static String SORTING_STYLE = "style";
    public static enum SORTING_STYLES{ascending, decending};
    
    public static final String AUTH_WRITE_ACTION = "write";
    
    public static final String MESSAGEBOX_AUTH_PERMISSION_SPACE = "/Permission/Messagebox";
    public static final String SECURE_TOPIC_RESOURCE_PREFIX = "/SecureTopic";

}
