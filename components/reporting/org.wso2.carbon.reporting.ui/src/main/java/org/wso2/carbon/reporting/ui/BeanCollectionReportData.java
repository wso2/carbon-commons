package org.wso2.carbon.reporting.ui;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.wso2.carbon.reporting.api.ReportData;
import org.wso2.carbon.reporting.api.ReportingException;

import java.util.Collection;


public class BeanCollectionReportData implements ReportData {

    /**
     * This method used to generate JRDataSource
     * @param beanList bean collection
     * @return jrDataSource
     * @throws ReportingException
     */
    public JRDataSource getReportDataSource(Object beanList) throws ReportingException {
        JRDataSource jrDataSource;
        if (beanList instanceof Collection) {
            jrDataSource = new JRBeanCollectionDataSource((Collection) beanList);
        } else {
            throw new ReportingException("Data source is not a collection");
        }
        return jrDataSource;
    }
}
