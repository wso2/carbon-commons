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

package org.wso2.carbon.reporting.util.types;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import org.wso2.carbon.reporting.api.ReportingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generate HTML report using given jasperPrint object
 */
public class HtmlReport {

    /**
     * generate a ByteArrayOutputStream from given JasperPrint object for the HTML report
     *
     * @param jasperPrint used to generate Html report
     * @return reporting ByteArrayOutputStream
     * @throws ReportingException when JasperPrint null
     * @throws IOException
     * @throws JRException
     */
    public ByteArrayOutputStream generateHtmlReport(JasperPrint jasperPrint)
            throws ReportingException, JRException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (jasperPrint == null) {
            throw new ReportingException("jasperPrint null, can't convert to HTML report");
        }
        try {
	    
	    // exclude the repeating (paged) column headers
	    jasperPrint.setProperty("net.sf.jasperreports.export.html.exclude.origin.keep.first.band.2", "columnHeader"); 
	    // exclude the page footers
	    jasperPrint.setProperty("net.sf.jasperreports.export.html.exclude.origin.band.2", "pageFooter");
 
            JRHtmlExporter jrHtmlExporter = new JRHtmlExporter();
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.JASPER_PRINT, jasperPrint);
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, true);
            // To generate a HTML report we want configure ImageServlet in component.xml file of reporting UI bundle
            // Then want to set the  IMAGES_URI parameter
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, "../servlets/image?image=");
	    // remove extra spaces between the report data
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.BETWEEN_PAGES_HTML, "");           
	   
 	    // remove empty spaces
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.TRUE);
            jrHtmlExporter.setParameter(JRHtmlExporterParameter.OUTPUT_STREAM, outputStream);
            jrHtmlExporter.exportReport();

        } catch (JRException e) {
            throw new JRException("Error occurred exporting HTML report ", e);
        }
        return outputStream;
    }
}
