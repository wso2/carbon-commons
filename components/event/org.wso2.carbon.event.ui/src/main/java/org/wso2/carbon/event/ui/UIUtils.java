package org.wso2.carbon.event.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.event.client.broker.BrokerClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.um.ws.api.stub.RemoteAuthorizationManagerServiceStub;
import org.wso2.carbon.user.mgt.ui.UserAdminClient;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class UIUtils {
    
    public static RemoteAuthorizationManagerServiceStub getAuthManagementClient(ServletConfig config, HttpSession session, HttpServletRequest request) throws AxisFault{
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        
        //backendServerURL = backendServerURL.replaceAll("https://", "http://");
        //backendServerURL = backendServerURL.replaceAll("9443", "9763");
        backendServerURL = backendServerURL + "RemoteAuthorizationManagerService";
        
        //String backendServerURL = CarbonUIUtil.getServerURL(request.get, session);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        
        RemoteAuthorizationManagerServiceStub stub = new RemoteAuthorizationManagerServiceStub(backendServerURL);
        if(cookie != null){
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
        return stub;
    }

    
    public static BrokerClient getBrokerClient(ServletConfig config, HttpSession session, HttpServletRequest request){
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext()
                                        .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        //backendServerURL = backendServerURL.replaceAll("https://", "http://");
        //backendServerURL = backendServerURL.replaceAll("9443", "9763");
        backendServerURL = backendServerURL + "EventBrokerService";
        
        //String backendServerURL = CarbonUIUtil.getServerURL(request.get, session);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new BrokerClient(configContext,backendServerURL,cookie);
    }
    
    public static String getBackendServerUrl(ServletConfig config, HttpSession session, HttpServletRequest request){
        return CarbonUIUtil.getServerURL(config.getServletContext(), session);
    }
    
    public static UserAdminClient getUserAdminClient(ServletConfig config, HttpSession session, HttpServletRequest request) throws Exception{
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        if(!backendServerURL.endsWith("/")){
            backendServerURL = backendServerURL + "/";
        }
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        return new UserAdminClient(cookie, backendServerURL,null);
    }
    
    public static String getValue(HttpServletRequest request,String name){
        String value = request.getParameter(name);
        if("null".equals(value)){
            return "";
        }
        return value != null? value : "";
    }
    
    public static String getDialog(String message){
        return "CARBON.showErrorDialog('"+message+"');";
    }
    
    public static String getSubscriptionMode(String serverMode){
         if (serverMode.equals(UIConstants.SUBSCRIPTION_MODE_1)){
             return UIConstants.SUBSCRIPTION_MODE_1_DESCRIPTION;
        }else if(serverMode.equals(UIConstants.SUBSCRIPTION_MODE_2)){
            return UIConstants.SUBSCRIPTION_MODE_2_DESCRIPTION;
        }else {
            return UIConstants.SUBSCRIPTION_MODE_0_DESCRIPTION;
        }
    }
    
}
