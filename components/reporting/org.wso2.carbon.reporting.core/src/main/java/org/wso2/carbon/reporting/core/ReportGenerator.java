/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.reporting.core;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRFontUtil;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.util.types.ExcelReport;
import org.wso2.carbon.reporting.util.types.HtmlReport;
import org.wso2.carbon.reporting.util.types.PdfReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class used to generate from given JasperPrint object relevant to the report type
 */

public class ReportGenerator {
    public byte[] getReportStream(JasperPrint jasperPrint, String reportType) throws ReportingException, JRException {
        ByteArrayOutputStream outputStream = null;
        try {
            try {
                if (reportType.equals("pdf")) {
                    // used PdfReport to generate PDF report
                    PdfReport pdfReport = new PdfReport();
                    outputStream = pdfReport.generatePdfReport(jasperPrint);
                } else if (reportType.equals("excel")) {
                    //use  ExcelReport to generate Excel report
                    ExcelReport excelReport = new ExcelReport();
                    outputStream = excelReport.generateExcelReport(jasperPrint);
                } else if (reportType.equals("html")) {
                    // used HtmlReport to generate HTML report
                    HtmlReport htmlReport = new HtmlReport();
                    outputStream = htmlReport.generateHtmlReport(jasperPrint);
                } else {
                    throw new ReportingException("requested report type " + reportType + " invalid");
                }
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            throw new ReportingException(" Error occurred when closing report stream", e);
        }catch (JRException e){
            throw new JRException("Failed to generate "+ reportType + " report" ,e);
        }
        if (outputStream == null) {
            throw new ReportingException("generated report byte stream null");
        }
        return outputStream.toByteArray();
    }
}
