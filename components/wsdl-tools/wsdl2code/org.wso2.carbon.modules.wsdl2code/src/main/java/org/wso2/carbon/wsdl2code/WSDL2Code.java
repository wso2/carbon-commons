/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.wsdl2code;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.CommandLineOption;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.util.WSDL2JavaOptionsValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.core.transports.http.HttpTransportListener;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.ServerConstants;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Tool that generate code for the given options
 */
public class WSDL2Code extends AbstractAdmin {

    private static Log log = LogFactory.getLog(WSDL2Code.class);
    private static final String CODEGEN_POM_XSL = "org/wso2/carbon/wsdl2code/codegen-pom.xsl";

    /**
     * This method will generate the code based on the options array. Options
     * arrya should be as follows, new String[] {"-uri", "location of wsdl",
     * "-g" ...}. Thus, the incoming XML should be as follows,
     * <p/>
     * <ns:codegenRequest xmlns:ns="http://org.wso2.wsf/tools">
     * <options>-uri</options> <options>file://foo</options> ...
     * </ns:codegenRequest>
     * <p/>
     * Once codegenerated, location of genereated code will be send as an ID,
     * thus, one could easily download artifact as a zip file or jar file.
     *
     * @param options
     * @return String
     * @throws AxisFault
     */
    public CodegenDownloadData codegen(String[] options) throws AxisFault {

        String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
        ConfigurationContext configContext = getConfigContext();
        String codegenOutputDir = configContext.getProperty(ServerConstants.WORK_DIR) + File.separator
                + "tools_codegen" + File.separator + uuid + File.separator;
        System.getProperties().remove("project.base.dir");
        System.getProperties().remove("name");
        System.setProperty("project.base.dir", codegenOutputDir);

        String projectName = "";

        ArrayList<String> optionsList = new ArrayList<String>();
        HashMap<String, String> projOptionsList = new HashMap<String, String>();
        // adding default configurations
        
        projOptionsList.put("-gid", "org.wso2.carbon");
        projOptionsList.put("-vn", "0.0.1-SNAPSHOT");
        projOptionsList.put("-aid", "wso2-axis2-client");
        for (int j = 0; j < options.length; j++) {
            String option = options[j];
            if (option.equalsIgnoreCase("-gid") || option.equalsIgnoreCase("-vn") || option.equalsIgnoreCase("-aid")) {
                projOptionsList.put(option, options[j + 1]);
                j++;
            } else {
                optionsList.add(option);
            }
        }
        optionsList.add("--noBuildXML");

        String[] args = optionsList.toArray(new String[optionsList.size()]);
        Map allOptions;
        try {
            CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(args);
            allOptions = commandLineOptionParser.getAllOptions();
            //validation
            List list = commandLineOptionParser.getInvalidOptions(new WSDL2JavaOptionsValidator());
            if (list.size() > 0) {
                String faultOptions = "";
                for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                    CommandLineOption commandLineOption = (CommandLineOption) iterator.next();
                    String optionValue = commandLineOption.getOptionValue();
                    faultOptions += "Invalid input for [ " + commandLineOption.getOptionType()
                            + (optionValue != null ? " : " + optionValue + " ]" : " ]")
                            + "\n";

                }

                log.error(faultOptions);
                throw new AxisFault(faultOptions);
            }
            CommandLineOption commandLineOption = (CommandLineOption) allOptions.get("uri");
            if (commandLineOption == null) {
                throw new AxisFault("WSDL URI or Path Cannot be empty");
            }
            String uriValue = commandLineOption.getOptionValue().trim();
            projectName = getProjectName(uriValue);
            if ("".equals(uriValue)) {
                throw new AxisFault("WSDL URI or Path Cannot be empty");
            } else if (!(uriValue.startsWith("https://") || uriValue.startsWith("http://"))) {
                File file = new File(uriValue);
                if (!(file.exists() && file.isFile())) {
                    throw new AxisFault("The wsdl uri should be a URL or a valid path on the file system");
                }
            }
//            new CodeGenerationEngine(commandLineOptionParser).generate();
            (new POMGenerator()).generateAxis2Client(allOptions, codegenOutputDir, projOptionsList);
        } catch (Exception e) {
            String rootMsg = "Code generation failed";
            Throwable throwable = e.getCause();
            if (throwable != null) {
                String msg = throwable.getMessage();
                if (msg != null) {
                    log.error(rootMsg + " " + msg, throwable);
                    throw new AxisFault(throwable.toString());
                }
            }
            log.error(rootMsg, e);
            throw AxisFault.makeFault(e);
        }
        //set the output name
        CommandLineOption option =
                (CommandLineOption) allOptions.
                get(CommandLineOptionConstants.WSDL2JavaConstants.SERVICE_NAME_OPTION);

        try {
            //achive destination
            uuid = String.valueOf(System.currentTimeMillis() + Math.random());
            File destDir = new File(configContext.getProperty(ServerConstants.WORK_DIR) + File.separator
                    + "tools_codegen"
                    + File.separator
                    + uuid);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            String destFileName = projectName + "-client.zip";
            String destArchive = destDir.getAbsolutePath() + File.separator + destFileName;


            new ArchiveManipulator().archiveDir(destArchive, new File(codegenOutputDir).getPath());
            FileManipulator.deleteDir(new File(codegenOutputDir));

            DataHandler handler;
            if (destArchive != null) {
                File file = new File(destArchive);
                FileDataSource datasource = new FileDataSource(file);
                handler = new DataHandler(datasource);

                CodegenDownloadData data = new CodegenDownloadData();
                data.setFileName(file.getName());
                data.setCodegenFileData(handler);
                return data;
            } else {
                return null;
            }

        } catch (IOException e) {
            String msg = WSDL2Code.class.getName() + " IOException has occured.";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    /**
     * Generates code for CXF
     *
     * @param options
     * @return
     * @throws AxisFault
     */
    public CodegenDownloadData codegenForCXF(String[] options) throws AxisFault {
        boolean isJaxWs = true; //Otherwise JaxRS
        String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
        ConfigurationContext configContext = getConfigContext();
        String codegenOutputDir = configContext.getProperty(ServerConstants.WORK_DIR) + File.separator
                + "tools_codegen" + File.separator + uuid + File.separator;
        System.getProperties().remove("project.base.dir");
        System.getProperties().remove("name");
        System.setProperty("project.base.dir", codegenOutputDir);

        ArrayList<String> optionsList = new ArrayList<String>();
        HashMap<String, String> projOptionsList = new HashMap<String, String>();
        // adding default configurations

        projOptionsList.put("-gid", "WSO2");
        projOptionsList.put("-vn", "0.0.1-SNAPSHOT");
        projOptionsList.put("-aid", "WSO2-Axis2-Client");

                    int i = 0;
        for (String s : options) {

               if (s.equalsIgnoreCase("-jaxws") || s.equalsIgnoreCase("-jaxrs")) {
                if (s.equals("-jaxrs")) {
                    isJaxWs = false;
                    //This is only to distinguish jaxws and jaxrs. Therefore no need of adding this to options list
                    i++;
                    continue;
                }
            }
            if (s.equalsIgnoreCase("-gid") || s.equalsIgnoreCase("-vn") || s.equalsIgnoreCase("-aid")) {
                projOptionsList.put(s, options[i + 1]);
            } else {
                optionsList.add(s);
            }
            i++;
        }

        if (isJaxWs) {
            return getJaxWSCodegenDownloadData(configContext, codegenOutputDir, optionsList, projOptionsList);
        } else {
            return getJaxRSCodegenDownloadData(configContext, codegenOutputDir, optionsList, projOptionsList);
        }
    }

    private CodegenDownloadData getJaxWSCodegenDownloadData(ConfigurationContext configContext, String codegenOutputDir, ArrayList<String> optionsList, HashMap<String, String> projOptionsList) throws AxisFault {
        String uuid;
        optionsList.add("-frontend");
        optionsList.add("jaxws21");
        optionsList.add("-client");

        String[] args = optionsList.toArray(new String[optionsList.size()]);

        try {
            (new POMGenerator()).generateJaxWSClient(optionsList, codegenOutputDir, projOptionsList);
        } catch (Exception e) {
            String rootMsg = "Code generation failed";
            Throwable throwable = e.getCause();
            if (throwable != null) {
                String msg = throwable.getMessage();
                if (msg != null) {
                    log.error(rootMsg + " " + msg, throwable);
                    throw new AxisFault(throwable.toString());
                }
            }
            log.error(rootMsg, e);
            throw AxisFault.makeFault(e);
        }

        try {
            //achive destination
            uuid = String.valueOf(System.currentTimeMillis() + Math.random());
            File destDir = new File(configContext.getProperty(ServerConstants.WORK_DIR) + File.separator
                    + "tools_codegen"
                    + File.separator
                    + uuid);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            //String destFileName = uuid.substring(2) + ".zip";
            String destFileName = getJAXWSRSProjectname(optionsList) + "-Client.zip";
            String destArchive = destDir.getAbsolutePath() + File.separator + destFileName;

            new ArchiveManipulator().archiveDir(destArchive, new File(codegenOutputDir).getPath());
            FileManipulator.deleteDir(new File(codegenOutputDir));

            DataHandler handler;
            if (destArchive != null) {
                File file = new File(destArchive);
                FileDataSource datasource = new FileDataSource(file);
                handler = new DataHandler(datasource);

                CodegenDownloadData data = new CodegenDownloadData();
                data.setFileName(file.getName());
                data.setCodegenFileData(handler);
                return data;
            } else {
                return null;
            }

        } catch (IOException e) {
            String msg = WSDL2Code.class.getName() + " IOException has occured.";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private CodegenDownloadData getJaxRSCodegenDownloadData(ConfigurationContext configContext, String codegenOutputDir, ArrayList<String> optionsList, HashMap<String, String> projOptionsList) throws AxisFault {
        String uuid;

        String[] args = optionsList.toArray(new String[optionsList.size()]);

        try {
            POMGenerator.generateJaxRSClient(optionsList, codegenOutputDir, projOptionsList);
        } catch (Exception e) {
            String rootMsg = "Code generation failed";
            Throwable throwable = e.getCause();
            if (throwable != null) {
                String msg = throwable.getMessage();
                if (msg != null) {
                    log.error(rootMsg + " " + msg, throwable);
                    throw new AxisFault(throwable.toString());
                }
            }
            log.error(rootMsg, e);
            throw AxisFault.makeFault(e);
        }

        try {
            //achive destination
            uuid = String.valueOf(System.currentTimeMillis() + Math.random());
            File destDir = new File(configContext.getProperty(ServerConstants.WORK_DIR) + File.separator
                    + "tools_codegen"
                    + File.separator
                    + uuid);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
           // String destFileName = uuid.substring(2) + ".zip";
            String destFileName = getJAXWSRSProjectname(optionsList) + "-Client.zip";
            String destArchive = destDir.getAbsolutePath() + File.separator + destFileName;

            new ArchiveManipulator().archiveDir(destArchive, new File(codegenOutputDir).getPath());
            FileManipulator.deleteDir(new File(codegenOutputDir));

            DataHandler handler;
            if (destArchive != null) {
                File file = new File(destArchive);
                FileDataSource datasource = new FileDataSource(file);
                handler = new DataHandler(datasource);

                CodegenDownloadData data = new CodegenDownloadData();
                data.setFileName(file.getName());
                data.setCodegenFileData(handler);
                return data;
            } else {
                return null;
            }

        } catch (IOException e) {
            String msg = WSDL2Code.class.getName() + " IOException has occured.";
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }

    private String getWsdlInformation(String serviceName, AxisConfiguration axisConfig)
            throws AxisFault {
        String ip;
        try {
            ip = NetworkUtils.getLocalHostname();
        } catch (SocketException e) {
            throw new AxisFault("Cannot get local host name", e);
        }
        TransportInDescription http = axisConfig.getTransportIn("http");
        if (http != null) {
            EndpointReference epr =
                    ((HttpTransportListener) http.getReceiver()).
                    getEPRForService(serviceName, ip);
            String wsdlUrlPrefix = epr.getAddress();
            if (wsdlUrlPrefix.endsWith("/")) {
                wsdlUrlPrefix = wsdlUrlPrefix.substring(0, wsdlUrlPrefix.length() - 1);
            }
            return wsdlUrlPrefix + "?wsdl";
        }
        return null;
    }

    /**
     * When the service is a hierarchical service, the service name contains '/'
     * charactors. But if the artifact id of the generated pom.xml file contains
     * '/' charactors, it will fail to build. Therefore, we have to replace '/'
     * with '-'.
     *
     * @param name - original service name
     * @return - formatted name
     */
    private String formatServiceName(String name) {
        String newName = name;
        if (newName.indexOf('/') != -1) {
            newName = newName.replace('/', '-');
        }
        return newName;
    }

    private String generateUUDI() {
        return String.valueOf(System.currentTimeMillis() + Math.random());
    }

    private String getProjectName(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        String fileNameWithoutExtn = fileName.lastIndexOf('?') > -1 ? 
            fileName.substring(0, fileName.lastIndexOf('?')) : fileName;
        return fileNameWithoutExtn;
    }

    private String getJAXWSRSProjectname(ArrayList<String> optionsList){

        String [] arr = new String[optionsList.size()];
        int p =0;
        for(Object b : optionsList.toArray()){
            arr[p]=optionsList.get(p);
            p++;
        }
        String retVal = "";
        for (int x=0; x<arr.length-1 ; x++){
            if (arr[x].toString().equalsIgnoreCase("-service")){
                retVal = getProjectName(arr[x+1].toString());
                break;
            }                                }
        return retVal;
    }
}
