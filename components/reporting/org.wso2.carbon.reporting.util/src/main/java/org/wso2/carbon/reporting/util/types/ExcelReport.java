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
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import org.wso2.carbon.reporting.api.ReportingException;

import java.io.ByteArrayOutputStream;


/**
 * Generate Excel report using given jasperPrint object
 */
public class ExcelReport {

    /**
     * generate a ByteArrayOutputStream from given JasperPrint object for the Excel report
     *
     * @param jasperPrint transform to excel report
     * @return reporting ByteArrayOutputStream
     * @throws ReportingException when the JasperPrint null
     * @throws JRException
     */
    public ByteArrayOutputStream generateExcelReport(JasperPrint jasperPrint) throws ReportingException, JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (jasperPrint == null) {
            throw new ReportingException("jasperPrint null, can't convert to excel report");
        }
        try {
            // Remove the pageHeader from pages except starting page
            jasperPrint.setProperty("net.sf.jasperreports.export.xls.exclude.origin.keep.first.band.1", "pageHeader");
 	    // Remove the column headers except the first one
            jasperPrint.setProperty("net.sf.jasperreports.export.xls.exclude.origin.keep.first.band.2", "columnHeader");

            //  Remove the pageFooter from all the pages
            jasperPrint.setProperty("net.sf.jasperreports.export.xls.exclude.origin.band.2", "pageFooter");
            //  set the JXL parameters to generate Excel report
            JExcelApiExporter jExcelApiExporter = new JExcelApiExporter();
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.JASPER_PRINT, jasperPrint);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.OUTPUT_STREAM, outputStream);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.IS_IGNORE_CELL_BORDER,Boolean.TRUE);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.IS_ONE_PAGE_PER_SHEET,Boolean.FALSE);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS,Boolean.TRUE);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.OFFSET_X,0);
            jExcelApiExporter.setParameter(JExcelApiExporterParameter.OFFSET_Y,0 );
            jExcelApiExporter.exportReport();

        } catch (JRException e) {
            throw new JRException("Error occurred exporting Excel report ", e);
        }
        return outputStream;
    }
}
