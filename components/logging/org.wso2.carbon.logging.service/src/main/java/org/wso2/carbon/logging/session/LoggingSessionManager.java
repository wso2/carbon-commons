package org.wso2.carbon.logging.session;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;

public class LoggingSessionManager {
	public static void setSessionObject(String name, Object obj) {
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		if (messageContext != null) {
			ServiceContext serviceContext = messageContext.getServiceContext();
			if (serviceContext != null) {
				serviceContext.setProperty(name, obj);
			}
		}
	}

	public static Object getSessionObject(String name) {
		MessageContext messageContext = MessageContext.getCurrentMessageContext();
		if (messageContext != null) {
			ServiceContext serviceContext = messageContext.getServiceContext();
			if (serviceContext != null) {
				return serviceContext.getProperty(name);
			}
		}
		return null;
	}
}
