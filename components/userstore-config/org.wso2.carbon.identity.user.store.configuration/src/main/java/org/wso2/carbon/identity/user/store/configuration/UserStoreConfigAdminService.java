/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.identity.user.store.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.XMLProcessorUtils;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class UserStoreConfigAdminService extends AbstractAdmin {
    public static final Log log = LogFactory.getLog(UserStoreConfigAdminService.class);
    public static final String DISABLED = "Disabled";
    public static final String DESCRIPTION = "Description";
    public static final String USERSTORES = "userstores";
    private static final String deploymentDirectory = CarbonUtils.getCarbonRepository() + USERSTORES;
    XMLProcessorUtils xmlProcessorUtils = new XMLProcessorUtils();

    /**
     * Get details of current secondary user store configurations
     *
     * @return : Details of all the configured secondary user stores
     * @throws UserStoreException
     */
    public UserStoreDTO[] getSecondaryRealmConfigurations() throws UserStoreException {
        ArrayList<UserStoreDTO> domains = new ArrayList<UserStoreDTO>();

        RealmConfiguration secondaryRealmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                getRealmConfiguration().getSecondaryRealmConfig();

        //not editing primary store
        if (secondaryRealmConfiguration == null) {
            return null;
        } else {

            do {
            	Map<String, String> userStoreProperties = secondaryRealmConfiguration.getUserStoreProperties();
                UserStoreDTO userStoreDTO = new UserStoreDTO();
                if (userStoreProperties.containsKey(UserStoreConfigConstants.connectionPassword)) {
                    userStoreProperties.put(UserStoreConfigConstants.connectionPassword, "");
                }
                if (userStoreProperties.containsKey(JDBCRealmConstants.PASSWORD)) {
                    userStoreProperties.put(JDBCRealmConstants.PASSWORD, "");
                }
                String className = secondaryRealmConfiguration.getUserStoreClass();
                userStoreDTO.setClassName(secondaryRealmConfiguration.getUserStoreClass());
                userStoreDTO.setDescription(secondaryRealmConfiguration.getUserStoreProperty(DESCRIPTION));
                userStoreDTO.setDomainId(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants.DOMAIN_NAME));
                if (userStoreProperties.get(DISABLED) != null) {
                    userStoreDTO.setDisabled(Boolean.valueOf(userStoreProperties.get(DISABLED)));
                }
                userStoreProperties.put("Class", className);
                userStoreDTO.setProperties(convertMapToArray(userStoreProperties));

                domains.add(userStoreDTO);
                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();

            } while (secondaryRealmConfiguration != null);
        }
        return domains.toArray(new UserStoreDTO[domains.size()]);
    }

    /**
     * Get user store properties of a given active user store manager as an array
     *
     * @param properties: properties of the user store
     * @return key#value
     */
    private PropertyDTO[] convertMapToArray(Map<String, String> properties) throws UserStoreException {
        Set<Map.Entry<String, String>> propertyEntries = properties.entrySet();
        ArrayList<PropertyDTO> propertiesList = new ArrayList<PropertyDTO>();
        String key;
        String value;
        for (Map.Entry<String, String> entry : propertyEntries) {
            key = (String) entry.getKey();
            value = (String) entry.getValue();
            PropertyDTO propertyDTO = new PropertyDTO(key, value);

                propertiesList.add(propertyDTO);

        }
        return propertiesList.toArray(new PropertyDTO[propertiesList.size()]);
    }

    /**
     * Get available user store manager implementations
     *
     * @return: Available implementations for user store managers
     */
    public String[] getAvailableUserStoreClasses() throws UserStoreException {
        Set<String> classNames = UserStoreManagerRegistry.getUserStoreManagerClasses();
        return classNames.toArray(new String[classNames.size()]);
    }

    /**
     * Get User Store Manager default properties for a given implementation
     *
     * @param className:Implementation class name for the user store
     * @return : list of default properties(mandatory+optional)
     */
    public Properties getUserStoreManagerProperties(String className) throws UserStoreException {
        return UserStoreManagerRegistry.getUserStoreProperties(className);
    }

    /**
     * Save the sent configuration to xml file
     *
     * @param userStoreDTO: Represent the configuration of user store
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public void addUserStore(UserStoreDTO userStoreDTO) throws UserStoreException {
        String domainName = userStoreDTO.getDomainId();
        xmlProcessorUtils.isValidDomain(domainName,true);
        
        File userStoreConfigFile = createConfigurationFile(domainName);
        // This is a redundant check
        if (userStoreConfigFile.exists()) {
        	String msg = "Cannot add user store "+domainName+". User store already exists.";
        	log.error(msg);
        	throw new UserStoreException(msg);
        }
        writeUserMgtXMLFile(userStoreConfigFile,userStoreDTO);
        if(log.isDebugEnabled()) {
        	log.debug("New user store successfully written to the file" + userStoreConfigFile.getAbsolutePath());
        }
    }


    /**
     * Edit currently existing user store
     *
     * @param userStoreDTO: Represent the configuration of user store
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public void editUserStore(UserStoreDTO userStoreDTO) throws UserStoreException {
        String domainName = userStoreDTO.getDomainId();
        if (xmlProcessorUtils.isValidDomain(domainName, false)) {
            File userStoreConfigFile = createConfigurationFile(domainName);
            if (!userStoreConfigFile.exists()) {
                String msg = "Cannot edit user store " + domainName + ". User store cannot be edited.";
                log.error(msg);
                throw new UserStoreException(msg);
            }

            writeUserMgtXMLFile(userStoreConfigFile, userStoreDTO);
            if (log.isDebugEnabled()) {
                log.debug("Edited user store successfully written to the file" + userStoreConfigFile.getAbsolutePath());
            }
        } else {
            throw new UserStoreException("Trying to edit an invalid domain");
        }
    }

    /**
     * Edit currently existing user store with a change of its domain name
     *
     * @param userStoreDTO:      Represent the configuration of new user store
     * @param previousDomainName
     * @throws TransformerException
     * @throws ParserConfigurationException
     */
    public void editUserStoreWithDomainName(String previousDomainName, UserStoreDTO userStoreDTO) throws UserStoreException {
        boolean isDebugEnabled = log.isDebugEnabled();
        String domainName = userStoreDTO.getDomainId();

        if (isDebugEnabled) {
            log.debug("Changing user store " + previousDomainName + " to " + domainName);
        }

        File userStoreConfigFile = null;
        File previousUserStoreConfigFile = null;

        String fileName = domainName.replace(".", "_");
        String previousFileName = previousDomainName.replace(".", "_");

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            File userStore = new File(deploymentDirectory);
            if (!userStore.exists()) {
                if (new File(deploymentDirectory).mkdir()) {
                    //folder 'userstores' created
                } else {
                    log.error("Error at creating 'userstores' directory to store configurations for super tenant");
                }
            }
            userStoreConfigFile = new File(deploymentDirectory + File.separator + fileName + ".xml");
            previousUserStoreConfigFile = new File(deploymentDirectory + File.separator + previousFileName + ".xml");
        } else {
            String tenantFilePath = CarbonUtils.getCarbonTenantsDirPath();
            tenantFilePath = tenantFilePath + File.separator + tenantId + File.separator + USERSTORES;
            File userStore = new File(tenantFilePath);
            if (!userStore.exists()) {
                if (new File(tenantFilePath).mkdir()) {
                    //folder 'userstores' created
                } else {
                    log.error("Error at creating 'userstores' directory to store configurations for tenant:" + tenantId);
                }
            }
            userStoreConfigFile = new File(tenantFilePath + File.separator + fileName + ".xml");
            previousUserStoreConfigFile = new File(tenantFilePath + File.separator + previousFileName + ".xml");
        }

        if (!previousUserStoreConfigFile.exists()) {
            String msg = "Cannot update user store domain name. Previous domain name " + previousDomainName + " does not exists.";
            log.error(msg);
            throw new UserStoreException(msg);
        }

        if (userStoreConfigFile.exists()) {
            String msg = "Cannot update user store domain name. An user store already exists with new domain " + domainName + ".";
            log.error(msg);
            throw new UserStoreException(msg);
        }

        // Update persisted domain name
        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
        userStoreManager.updatePersistedDomain(previousDomainName, domainName);
        if (log.isDebugEnabled()) {
            log.debug("Renamed persisted domain name from" + previousDomainName + " to " + domainName + " of tenant:" + tenantId + " from UM_DOMAIN.");
        }

        previousUserStoreConfigFile.delete();
        writeUserMgtXMLFile(userStoreConfigFile,userStoreDTO);
    }

    /**
     * Deletes the user store specified
     *
     * @param domainName: domain name of the user stores to be deleted
     */
    public void deleteUserStore(String domainName) throws UserStoreException {
        if (!isAuthorized()) {
            throw new UserStoreException("Logged user is not authorized to delete user stores");
        }
        deleteUserStoresSet(new String[]{domainName});
    }

    /**
     * Delete the given list of user stores
     *
     * @param domains: domain names of user stores to be deleted
     */
    public void deleteUserStoresSet(String[] domains) throws UserStoreException {
        boolean isDebugEnabled = log.isDebugEnabled();

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String path;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            path = deploymentDirectory;
        } else {
            path = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId + File.separator + USERSTORES;

        }
        File file = new File(path);
        for (String domainName : domains) {
            if (isDebugEnabled) {
                log.debug("Deleting, .... " + domainName + " domain.");
            }
            // Delete persisted domain name
            AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            //userStoreManager.deletePersistedDomain(domainName);

            userStoreManager.deletePersistedDomain(domainName);
            if (isDebugEnabled) {
                log.debug("Removed persisted domain name: " + domainName
                        + " of tenant:" + tenantId + " from UM_DOMAIN.");
            }

            // Delete file
            deleteFile(file, domainName.replace(".", "_").concat(".xml"));
        }
    }

    /**
     * Adds an array of properties
     *
     * @param propertyDTOs : List of user store properties
     * @param doc:         Document
     * @param parent       : Parent element of the properties to be added
     */
    private void addProperties(PropertyDTO[] propertyDTOs, Document doc, Element parent) {
        for (PropertyDTO propertyDTO : propertyDTOs) {
            if (propertyDTO.getValue() != null) {
                addProperty(propertyDTO.getName(), propertyDTO.getValue(), doc, parent);
            }
        }
    }

    /**
     * Adds a property
     *
     * @param name:   Name of property
     * @param value:  Value
     * @param doc:    Document
     * @param parent: Parent element of the property to be added as a child
     */
    private void addProperty(String name, String value, Document doc, Element parent) {
        Element property = doc.createElement("Property");

        Attr attr = doc.createAttribute("name");
        attr.setValue(name);
        property.setAttributeNode(attr);

        property.setTextContent(value);
        parent.appendChild(property);
    }


    private void deleteFile(File file, final String userStoreName) {
        File[] deleteCandidates = file.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(userStoreName);
            }
        });
        for (File file1 : deleteCandidates) {
            if (file1.delete()) {
                //file deleted successfully
            } else {
                log.error("error at deleting file:" + file.getName());
            }
        }
    }

    /**
     * Update a domain to be disabled/enabled
     *
     * @param domain:   Name of the domain to be updated
     * @param isDisable : Whether to disable/enable domain(true/false)
     */
    public void changeUserStoreState(String domain, Boolean isDisable) throws UserStoreException, Exception {

        File userStoreConfigFile = createConfigurationFile(domain);
        StreamResult result = new StreamResult(userStoreConfigFile);
        if (!userStoreConfigFile.exists()) {
            String msg = "Cannot edit user store." + domain + " does not exist.";
            throw new UserStoreException(msg);
        }

        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(userStoreConfigFile);

        NodeList elements = doc.getElementsByTagName("Property");
        for (int i = 0; i < elements.getLength(); i++) {
            //Assumes a property element only have attribute 'name'
            if ("Disabled".compareToIgnoreCase(elements.item(i).getAttributes().item(0).getNodeValue()) == 0) {
                elements.item(i).setTextContent(String.valueOf(isDisable));
                break;
            }
        }

        DOMSource source = new DOMSource(doc);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "6");
        transformer.transform(source, result);

        if (log.isDebugEnabled()) {
            log.debug("New state :" + isDisable + " of the user store \'" + domain + "\' successfully written to the file system");
        }
    }

    private boolean isAuthorized() throws UserStoreException {
        String loggeduser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        String admin = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName();

        if (loggeduser != null && loggeduser.equals(admin)) {
            return true;
        }
        log.error("Logged user '" + loggeduser + "', not the authorized admin user '" + admin + "'.");
        return false;
    }

    private File createConfigurationFile(String domainName){
        String fileName = domainName.replace(".", "_");
        File userStoreConfigFile=null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            File userStore = new File(deploymentDirectory);
            if (!userStore.exists()) {
                if (new File(deploymentDirectory).mkdir()) {
                    //folder 'userstores' created
                } else {
                    log.error("Error at creating 'userstores' directory to store configurations for super tenant");
                }
            }
            userStoreConfigFile = new File(deploymentDirectory + File.separator + fileName + ".xml");
        } else {
            String tenantFilePath = CarbonUtils.getCarbonTenantsDirPath();
            tenantFilePath = tenantFilePath + File.separator + tenantId + File.separator + USERSTORES;
            File userStore = new File(tenantFilePath);
            if (!userStore.exists()) {
                if (new File(tenantFilePath).mkdir()) {
                    //folder 'userstores' created
                } else {
                    log.error("Error at creating 'userstores' directory to store configurations for tenant:" + tenantId);
                }
            }
            userStoreConfigFile = new File(tenantFilePath + File.separator + fileName + ".xml");
        }
        return userStoreConfigFile;
    }


    private void writeUserMgtXMLFile(File userStoreConfigFile,UserStoreDTO userStoreDTO) throws UserStoreException{
        StreamResult result = new StreamResult(userStoreConfigFile);
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            //create UserStoreManager element
            Element userStoreElement = doc.createElement(UserCoreConstants.RealmConfig.LOCAL_NAME_USER_STORE_MANAGER);
            doc.appendChild(userStoreElement);

            Attr attrClass = doc.createAttribute("class");
            attrClass.setValue(userStoreDTO.getClassName());
            userStoreElement.setAttributeNode(attrClass);

            addProperties(userStoreDTO.getProperties(), doc, userStoreElement);
            addProperty(UserStoreConfigConstants.DOMAIN_NAME, userStoreDTO.getDomainId(), doc, userStoreElement);
            addProperty(DESCRIPTION, userStoreDTO.getDescription(), doc, userStoreElement);
            DOMSource source = new DOMSource(doc);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "6");
            transformer.transform(source, result);
        } catch (Exception ex) {
            throw new UserStoreException(ex);
        }
    }
}
