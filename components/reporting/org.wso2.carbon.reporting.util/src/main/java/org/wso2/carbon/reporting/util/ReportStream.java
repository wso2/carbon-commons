package org.wso2.carbon.reporting.util;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.util.types.ExcelReport;
import org.wso2.carbon.reporting.util.types.HtmlReport;
import org.wso2.carbon.reporting.util.types.PdfReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ReportStream {
    public ByteArrayOutputStream getReportStream(JasperPrint jasperPrint, String reportType)
            throws ReportingException, JRException {
        ByteArrayOutputStream outputStream = null;
        try {
            try {

                if (reportType.equalsIgnoreCase("pdf")) {

                    // used PdfReport to generate PDF report
                    PdfReport pdfReport = new PdfReport();
                    try {
                        outputStream = pdfReport.generatePdfReport(jasperPrint);
                    } catch (JRException e) {
                        throw new JRException("Failed to generate PDF format report ", e);
                    }
                } else if (reportType.equalsIgnoreCase("excel") |reportType.equals("xls")) {
                    //use  ExcelReport to generate Excel report
                    ExcelReport excelReport = new ExcelReport();
                    outputStream = excelReport.generateExcelReport(jasperPrint);
                } else if (reportType.equalsIgnoreCase("html")) {
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
        }
        return outputStream;
    }
}
