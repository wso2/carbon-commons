/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.reporting.util.types;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;



/**
 * used for report generation JasperPrint object it is special object to Jasper Report
 */
public class JPCreator {
    /**
     * generate report ByteArrayOutputStream
     *
     * @param jrDataSource data for report
     * @param template       report template
     * @return report ByteArrayOutputStream
     * @throws JRException will occurred generating report
     */
    public JasperPrint createJasperPrint(JRDataSource jrDataSource, String template)throws JRException {

        // create a byte array from given report template
        
        byte[] templateBytes = template.getBytes();
        InputStream templateInputStream = new ByteArrayInputStream(templateBytes);
        JasperPrint jasperPrint;
        try {
            // load JasperDesign
            JasperDesign jasperDesign = JRXmlLoader.load(templateInputStream);
            // compiling JasperDesign from JasperCompileManager
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
            // generate JasperPrint Object from given JasperReport file and JRDataSource by JasperFillManager
            jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), jrDataSource);
        } catch (JRException e) {
            throw new JRException("JasperPrint creation failed from " + template , e);
        }
        return jasperPrint;
    }



}
