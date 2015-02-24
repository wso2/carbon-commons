/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
//        String msg = "START\n" +
//                "SESSION_ID__12345679\n" +
//                "TENANT_ID__-1234\n" +
//                "publish\n" +
//                "START_EVENT\n" +
//                "STREAM_ID__org.wso2.test.stream:1.0.0\n" +
//                "TIME_STAMP__0\n" +
//                "START_META_DATA\n" +
//                "127.0.0.1\n" +
//                "END_META_DATA\n" +
//                "START_PAYLOAD\n" +
//                "WSO2\n" +
//                "123.4\n" +
//                "2\n" +
//                "12.4\n" +
//                "1.3\n" +
//                "END_PAYLOAD\n" +
//                "END_EVENT\n" +
//                "END";
//        msg = msg.replace("\n", "$$$__$$$");
//        String pattern1 = "START_EVENT";
//        String pattern2 = "END_EVENT";
//        Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
//        System.out.println(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
//        Matcher m = p.matcher(msg);
//        while (m.find()) {
//            String content = m.group(1);
//            content= content.replace("$$$__$$$", "\n");
//            System.out.println(content);
//        }

        String msg = "START\n" +
                "SESSION_ID__12345679\n" +
                "TENANT_ID__-1234\n" +
                "publish\n" +
                "START_EVENT\n" +
                "STREAM_ID__org.wso2.test.stream:1.0.0\n" +
                "TIME_STAMP__0\n" +
                "START_META_DATA\n" +
                "127.0.0.1\n" +
                "END_META_DATA\n" +
                "START_PAYLOAD\n" +
                "WSO2\n" +
                "123.4\n" +
                "2\n" +
                "12.4\n" +
                "1.3\n" +
                "END_PAYLOAD\n" +
                "END_EVENT\n" +
                "START_EVENT\n" +
                "STREAM_ID__org.wso2.test.stream:1.0.0\n" +
                "TIME_STAMP__0\n" +
                "START_META_DATA\n" +
                "127.0.0.1\n" +
                "END_META_DATA\n" +
                "START_PAYLOAD\n" +
                "WSO2\n" +
                "123.4\n" +
                "2\n" +
                "12.4\n" +
                "1.3\n" +
                "END_PAYLOAD\n" +
                "END_EVENT\n" +
                "END";
        String pattern1 = "START_EVENT";
        String pattern2 = "END_EVENT";
        Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2), Pattern.DOTALL);
        System.out.println(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
        Matcher m = p.matcher(msg);
        while (m.find()) {
            String content = m.group(1);
            System.out.println(content);
            System.out.println("******************************************");
        }

    }
}
