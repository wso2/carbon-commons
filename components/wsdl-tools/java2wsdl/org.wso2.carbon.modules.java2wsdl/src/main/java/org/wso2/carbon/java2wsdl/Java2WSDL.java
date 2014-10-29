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

package org.wso2.carbon.java2wsdl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.Java2WSDLCodegenEngine;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOptionParser;
import org.apache.ws.java2wsdl.utils.Java2WSDLOptionsValidator;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOption;
import org.wso2.carbon.utils.WSO2Constants;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

/**
 * This class will generate a WSDL, either version 1.1 or 2.0 for a given java bytecode. Underline
 * implementation uses org.apache.ws.java2wsdl.Java2WSDLCodegenEngine.
 */
public class Java2WSDL {

    private static Log log = LogFactory.getLog(Java2WSDL.class);

    /**
     * This will generate the WSDL document and output a Id. User has to send this Id to the
     * filedownload hadler to extract the wsdl document.
     *
     * @param options array of options
     * @return String id
     * @throws AxisFault will be thrown
     */
    private String java2wsdl(String[] options) throws AxisFault {
        String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
        String wsdlOutputDir =
                MessageContext.getCurrentMessageContext().getConfigurationContext()
                        .getProperty(WSO2Constants.WORK_DIR) + File.separator + "tools_wsdlview" +
                                                             File.separator + uuid + File.separator;
        ArrayList<String> optionsList = new ArrayList<String>();
        boolean isXCAvailable = false;
        String xcValue = "";
        for (String option : options) {
            if (option.equalsIgnoreCase("-xc")) {
                isXCAvailable = true;
                continue;
            }
            if (isXCAvailable) {
                xcValue = option;
                isXCAvailable = false;
                continue;
            }
            optionsList.add(option);
        }
        optionsList.add("-o");
        optionsList.add(wsdlOutputDir);
        optionsList.add("-of");
        optionsList.add(uuid + ".xml");
        parseXC(xcValue, optionsList);
        String[] args = optionsList.toArray(new String[optionsList.size()]);
        Java2WSDLCommandLineOptionParser commandLineOptionParser =
                new Java2WSDLCommandLineOptionParser(args);
        try {
            Map allOptions = commandLineOptionParser.getAllOptions();
            List list =
                    commandLineOptionParser.getInvalidOptions(new Java2WSDLOptionsValidator());
            if (list.size() > 0) {
                String faultOptions = "";
                for (Object aList : list) {
                    Java2WSDLCommandLineOption commandLineOption =
                            (Java2WSDLCommandLineOption) aList;
                    String optionValue = commandLineOption.getOptionValue();
                    faultOptions += "Invalid input for [ " + commandLineOption.getOptionType() +
                            (optionValue != null ? " : " + optionValue + " ]" : " ]") + "\n";
                }

                log.error(faultOptions);
                throw new AxisFault(faultOptions);
            }

            new Java2WSDLCodegenEngine(allOptions).generate();
        } catch (Exception e) {
            String rootMsg = Java2WSDL.class.getName() + " Exception has occured.";
            Throwable throwable = e.getCause();
            if (throwable != null) {
                String tmpMsg = throwable.toString();
                if (tmpMsg.indexOf("org.apache.axis2.AxisFault") > -1) {
                    tmpMsg = "Please provide the correct inputs to either -p2n or -xc";
                    log.error(tmpMsg, throwable);
                    throw new AxisFault(tmpMsg);
                }
                log.error(rootMsg, throwable);
                throw new AxisFault(throwable.toString());
            }
            String tmpMsg = e.toString();
            if (tmpMsg.indexOf("java.lang.StringIndexOutOfBoundsException") > -1) {
                tmpMsg = "Please provide the correct inputs to either -p2n or -xc";
                log.error(tmpMsg, e);
                throw new AxisFault(tmpMsg);
            }
            log.error(rootMsg, e);
            throw AxisFault.makeFault(e);
        }

        Map fileResourcesMap =
                (Map) MessageContext.getCurrentMessageContext().getConfigurationContext()
                        .getProperty(WSO2Constants.FILE_RESOURCE_MAP);

        if (fileResourcesMap == null) {
            fileResourcesMap = new Hashtable();
            MessageContext.getCurrentMessageContext().getConfigurationContext()
                    .setProperty(WSO2Constants.FILE_RESOURCE_MAP,
                                 fileResourcesMap);
        }

        File[] files = new File(wsdlOutputDir).listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith(".xml");
            }
        });

        if ((files != null) && (files[0] != null) &&
            (files[0].getAbsoluteFile() != null)) {
            fileResourcesMap.put(uuid, files[0].getAbsoluteFile().getAbsolutePath());
        }
        return "../../filedownload?id=" + uuid;

    }

    /**
     * This is the fall though method for wsdlview. This will check for required resources.
     *
     * @param options options array
     * @param uuids   uuid array
     * @return String id
     * @throws AxisFault will be thrown.
     */
    public String java2wsdlWithResources(String[] options, String[] uuids) throws AxisFault {

        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
        try {
            URL[] urls = new URL[uuids.length];
            for (int i = 0; i < uuids.length; i++) {
                urls[i] = new File(uuids[i]).toURL();
            }
            ClassLoader newCl = URLClassLoader.newInstance(urls, prevCl);
            Thread.currentThread().setContextClassLoader(newCl);
            return java2wsdl(options);
        } catch (MalformedURLException e) {
            throw AxisFault.makeFault(e);
        } finally {
            Thread.currentThread().setContextClassLoader(prevCl);
        }

    }

    private void parseXC(String value, ArrayList<String> options) {
        if (value == null || value.length() == 0) {
            return;
        }
        String[] tokens = value.split(",");
        for (String token : tokens) {
            if (token != null && token.length() != 0) {
                options.add("-xc");
                options.add(token);
            }
        }

    }
}
