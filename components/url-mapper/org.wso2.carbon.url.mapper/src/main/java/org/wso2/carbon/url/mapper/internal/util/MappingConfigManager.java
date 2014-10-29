package org.wso2.carbon.url.mapper.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.url.mapper.data.MappingConfig;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingConfigManager {
	
	private static final Log log = LogFactory.getLog(MappingConfigManager.class);
	private static BundleContext bundleContext;
	
	  public static MappingConfig loadMappingConfiguration() {
			String configFilename = CarbonUtils.getCarbonConfigDirPath()
			+ RegistryConstants.PATH_SEPARATOR
			+ UrlMapperConstants.MappingConfigs.ETC
			+ RegistryConstants.PATH_SEPARATOR
			+ UrlMapperConstants.MappingConfigs.MAPPING_CONF_FILE;
		
		  MappingConfig config = new MappingConfig();
	        InputStream inputStream = null;
			try {
				inputStream = new MappingConfigManager().getInputStream(configFilename);
			} catch (IOException e1) {
				  log.error("Could not close the Configuration File " + configFilename);
			}
	        if (inputStream != null) {
	            try {
	                XMLStreamReader parser =
	                        XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
	                StAXOMBuilder builder = new StAXOMBuilder(parser);
	                OMElement documentElement = builder.getDocumentElement();
	                @SuppressWarnings("rawtypes")
					Iterator it = documentElement.getChildElements();
	                while (it.hasNext()) {
	                    OMElement element = (OMElement) it.next();
	                    if (UrlMapperConstants.MappingConfigs.MAPPINGS.equals(element.
                                getLocalName())) {
	                        String mappings = element.getText();
	                        int noOfMappings = Integer.parseInt(mappings);
	                        config.setNoOfMappings(noOfMappings);
	                    } else if (UrlMapperConstants.MappingConfigs.PREFIX.equals(element.
                                getLocalName())) {
                            String prefix = element.getText();
                            String suffixPattern = "[a-zA-z0-9_\\-][a-zA-z0-9\\._\\-]+[a-zA-z0-9_\\-]";
                            Pattern pattern = Pattern.compile(suffixPattern);
                            Matcher matcher;
                            matcher = pattern.matcher(prefix);
                            if(matcher.matches()) {
                                prefix = "." + prefix;
                            } else {
                                log.error("please specify correct mapping suffix instead of" +
                                        "********" + prefix + "******** in" +
                                        "/repository/conf/etc/url-mapping-config.xml where " +
                                        "alphabets, numbers, - and _ are allowed everywhere but . " +
                                        "is allowed except in the start and end.");
                                log.error("For incorrect mappings localhost is taken as default " +
                                        "mapping suffix");
                                prefix = ".localhost";
                            }
                            config.setPrefix(prefix);
                        }
	                }
	              
	            } catch (Exception e) {
	                String msg = "Error in loading Stratos Configurations File: " + configFilename +  ". Default Settings will be used.";
	                log.error(msg, e);
	            } finally {
	                if (inputStream != null) {
	                    try {
	                        inputStream.close();
	                    } catch (IOException e) {
	                        log.error("Could not close the Configuration File " + configFilename);
	                    }
	                }
	            }
	        }
	        return config;
	    }
	  
	  private InputStream getInputStream(String configFilename) throws IOException {
			InputStream inStream = null;
			 File configFile = new File(configFilename);
			if (configFile.exists()) {
				inStream = new FileInputStream(configFile);
			}
			String warningMessage = "";
			if (inStream == null) {
				URL url;
				if (bundleContext != null) {
					if ((url = bundleContext.getBundle().getResource(UrlMapperConstants.MappingConfigs.MAPPING_CONF_FILE)) != null) {
						inStream = url.openStream();
					} else {
						warningMessage = "Bundle context could not find resource "
								+ UrlMapperConstants.MappingConfigs.MAPPING_CONF_FILE
								+ " or user does not have sufficient permission to access the resource.";
						log.error(warningMessage);
					}

				} else {
					if ((url = this.getClass().getClassLoader()
							.getResource(UrlMapperConstants.MappingConfigs.MAPPING_CONF_FILE)) != null) {
						inStream = url.openStream();
					} else {
						warningMessage = "Could not find resource "
								+ UrlMapperConstants.MappingConfigs.MAPPING_CONF_FILE
								+ " or user does not have sufficient permission to access the resource.";
						log.error(warningMessage);
					}
				}
			}
			return inStream;
		}
}
