package org.wso2.carbon.wsdl2form;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

class XSLTURIResolver implements URIResolver {
    public Source resolve(String href, String base) {
        InputStream is = Util.class.getResourceAsStream(href);
        return new StreamSource(is);
    }
}
