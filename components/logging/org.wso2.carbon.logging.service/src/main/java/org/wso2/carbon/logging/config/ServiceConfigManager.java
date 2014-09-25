package org.wso2.carbon.logging.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.service.LogViewerException;
import org.wso2.carbon.logging.util.LoggingConstants;
import org.wso2.carbon.utils.CarbonUtils;

public class ServiceConfigManager {
	
	private static final Log log = LogFactory.getLog(ServiceConfigManager.class);

	public static String[] getServiceNames() throws LogViewerException {
		String configFileName = CarbonUtils.getCarbonConfigDirPath() + File.separator + 
                LoggingConstants.MULTITENANCY_CONFIG_FOLDER + File.separator + LoggingConstants.CONFIG_FILENAME;
		ArrayList<String> serviceNames = new ArrayList<String>();
		File configFile = new File(configFileName);
		if (configFile.exists()) {
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(configFile);
				XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(
						inputStream);
				StAXOMBuilder builder = new StAXOMBuilder(parser);
				OMElement documentElement = builder.getDocumentElement();
				@SuppressWarnings("unchecked")
				Iterator<OMElement> properties = documentElement.getChildrenWithName(new QName(
						"cloudService"));
				while (properties.hasNext()) {
					OMElement element = properties.next();
                    Iterator<OMElement> child = element.getChildElements();
                    while (child.hasNext()) {
                        OMElement element1 = (OMElement) child.next();
                        if("key".equalsIgnoreCase(element1.getLocalName())) {
                            serviceNames.add(element1.getText());
                        }
                    }
				}
			} catch (Exception e) {
				String msg = "Error in loading Stratos Configurations File: " + configFileName
						+ ".";
				throw new LogViewerException(msg, e);
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						log.error("Could not close the Configuration File " + configFileName);
					}
				}
			}
		}
		return serviceNames.toArray(new String[serviceNames.size()]);
	}
	
	public static boolean isStratosService(String serviceName) throws LogViewerException {
		String services[] =  getServiceNames();
		for (String service : services) {
			if (service.equals(serviceName)) {
				return true;
			}
		}
		return false;
	}
}
