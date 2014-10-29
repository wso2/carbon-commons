package org.wso2.carbon.event.ui;

import java.util.regex.Pattern;

public class ErrorHandler {
    private static Pattern P1 = Pattern.compile("Failed to add new Collection.*already exist");
    
    public static String getErrorMessage(Exception e){
//        Matcher matcher = P1.matcher(e.getMessage());
//        if(matcher.matches()){
//            
//        }
        return e.getMessage();
    }
}
