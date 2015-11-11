/**
 * Copyright (c) 2009-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.reporting.api.ReportingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    public ByteArrayOutputStream generateHtmlReport(JasperPrint jasperPrint) throws ReportingException, JRException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (jasperPrint == null) {
            throw new ReportingException("jasperPrint null, can't convert to HTML report");
        }

        try {
            // exclude the repeating (paged) column headers
            jasperPrint
                    .setProperty("net.sf.jasperreports.export.html.exclude.origin.keep.first.band.2", "columnHeader");
            // exclude the page footers
            jasperPrint.setProperty("net.sf.jasperreports.export.html.exclude.origin.band.2", "pageFooter");
            final Map<String, String> images = new HashMap<>();
            JRHtmlExporter exporterHTML = new JRHtmlExporter();
            //setting up an input stream to JRHtmlExporter object
            SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);
            exporterHTML.setExporterInput(exporterInput);
            //setting up an output stream to JRHtmlExporter object
            SimpleHtmlExporterOutput simpleHtmlExporterOutput = new SimpleHtmlExporterOutput(outputStream);
            //To generate a html report we need to embed images using base64 encoding.
            //Otherwise it shows corrupted images.
            simpleHtmlExporterOutput.setImageHandler(new HtmlResourceHandler() {
                @Override public void handleResource(String id, byte[] data) {
                    images.put(id, "data:image/gif;base64," + new String(Base64.encodeBase64(data)));
                }
                @Override public String getResourcePath(String id) {
                    return images.get(id);
                }
            });

            exporterHTML.setExporterOutput(simpleHtmlExporterOutput);
            exporterHTML.exportReport();

        } catch (JRException e) {
            throw new JRException("Error occurred exporting HTML report ", e);
        }
        return outputStream;

    }
}
