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

import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class TestJAXB {

    public static void main(String[] args) {
        try {
            File file = new File("/home/sinthuja/projects/my-git-repo/wso2/carbon-commons/components/data-bridge/org.wso2.carbon.databridge.core/src/main/resources/data-bridge-config.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(DataBridgeConfiguration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            DataBridgeConfiguration customer = (DataBridgeConfiguration) jaxbUnmarshaller.unmarshal(file);
            System.out.println(customer);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
