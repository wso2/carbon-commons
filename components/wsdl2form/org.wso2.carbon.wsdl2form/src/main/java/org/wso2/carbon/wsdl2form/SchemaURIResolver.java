package org.wso2.carbon.wsdl2form;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class SchemaURIResolver implements URIResolver {

    private static Log log = LogFactory.getLog(SchemaURIResolver.class);

    private AxisService axisService;

    SchemaURIResolver(AxisService axisService) {
        this.axisService = axisService;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String xsd = href.substring(href.toLowerCase().indexOf("?xsd=") + 5);
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            axisService.printXSD(outputStream, xsd);
            return new StreamSource(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            log.error("Error while printing xsd : " + xsd, e);
            throw new TransformerException(e);
        }
    }
}
