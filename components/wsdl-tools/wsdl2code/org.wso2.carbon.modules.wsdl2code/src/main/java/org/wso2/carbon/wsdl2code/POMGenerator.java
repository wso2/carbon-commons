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

import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.transport.local.LocalTransportSender;
import org.apache.axis2.util.CommandLineOption;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class POMGenerator {


    public void generateAxis2Client(Map optionsMap, String codegenOutputDirectory, HashMap<String, String> projOptionsList) throws Exception {
        Map<String, String> configurationMap = new HashMap<String, String>();
        Map<String, String> predifinedValues = new HashMap<String, String>();

        configurationMap.put("uri", "wsdlFile");
        configurationMap.put("o", "outputDirectory");
        configurationMap.put("a", "syncMode");
        configurationMap.put("s", "syncMode");
        configurationMap.put("u", "unpackClasses");
        configurationMap.put("sn", "serviceName");
        configurationMap.put("uw", "unwrap");
        configurationMap.put("ap", "allPorts");
        configurationMap.put("pn", "portName");
        configurationMap.put("p", "packageName");
        configurationMap.put("ns2p", "namespaceToPackages");
        configurationMap.put("t", "generateTestcase");
        configurationMap.put("p", "packageName");
        configurationMap.put("l", "language");
        configurationMap.put("d", "databindingName");

        predifinedValues.put("a", "async");
        predifinedValues.put("s", "sync");

        checkPreconditions(optionsMap);

        String s = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/wso2/carbon/wsdl2code/axis-pom.xml"), "UTF-8");
        
        s = s.replace("gid", projOptionsList.get("-gid").toString()).
                replace("aid", projOptionsList.get("-aid").toString()).
                replace("vn", projOptionsList.get("-vn").toString());

        Iterator iterator = optionsMap.keySet().iterator();
        String configurations = "";
        while (iterator.hasNext()) {
            Object o = iterator.next();
            configurations += getConfiguratonElement((String) o, (CommandLineOption) optionsMap.get(o), configurationMap, predifinedValues);
        }

        ArrayList<String> artifactVersions = new ArrayList<String>();

        //axis2 dependency
        URL url = getContainingArtifact(org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator.class);
        String version = getVersion(url.getFile());

        artifactVersions.add(version);

        //axis2-adb dependency
        url = getContainingArtifact(RPCMessageReceiver.class);
        version = getVersion(url.getFile());

        artifactVersions.add(version);

        //axis2 http transport dependency
        url = getContainingArtifact(org.apache.axis2.transport.http.HTTPWorker.class);
        version = getVersion(url.getFile());

        artifactVersions.add(version);

        //axis2 local transport dependency
        url = getContainingArtifact(LocalTransportSender.class);
        version = getVersion(url.getFile());

        artifactVersions.add(version);

//        url = getContainingArtifact(TestCase.class);
//        version = getVersion(url.getFile());
//
//        artifactVersions.add(version);

//        for (int i = 0; i < artifactVersions.size(); i++) {
//            s = s.replace("{dependancy-v-" + (i + 1) + "}", artifactVersions.get(i).replace(".wso2", "-wso2"));
//           
//        }
        s = s.replaceAll("axs_ver", artifactVersions.get(0).replace(".wso2", "-wso2"));

        String toWrite = String.format(s, configurations);
        createFile(codegenOutputDirectory, toWrite);

    }

    private static void checkPreconditions(Map optionsMap) {
        //if s and a are both present s takes precedence over -a
        if (optionsMap.containsKey("s")) {
            optionsMap.remove("a");
        }
    }

    private static void createFile(String codegenOutputDirectory, String toWrite) throws IOException {
        FileUtils.writeStringToFile(new File(codegenOutputDirectory + File.separator + "pom.xml"), toWrite);
    }


    public void generateJaxWSClient(List optionsList, String codegenOutputDirectory, HashMap<String, String> projOptionsList) throws Exception {
        String s = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/wso2/carbon/wsdl2code/jaxws-pom.xml"), "UTF-8");
        s = s.replace("gid", projOptionsList.get("-gid").toString()).
                replace("aid", projOptionsList.get("-aid").toString()).
                replace("vn", projOptionsList.get("-vn").toString());
        String wsdlURL = getArgumentValue("-Service", optionsList);
        String wsdlOptions = getExtraArgsJaxWS(optionsList);
        wsdlOptions += getBindingFiles(optionsList);
        wsdlOptions += getServiceName(optionsList);
        s = String.format(s, wsdlURL, wsdlOptions);

        createFile(codegenOutputDirectory, s);

    }

    /**
     * For CXF Services
     *
     * @param optionsList
     * @return
     */
    private static String getServiceName(List optionsList) {
        for (int i = 0; i < optionsList.size(); i++) {
            if ("-sn".equals((String) optionsList.get(i)) && i + 1 < optionsList.size()) {
                return "<serviceName>" + (String) optionsList.get(i + 1) + "</serviceName>";
            }
        }
        return "";
    }

    /**
     * For CXF services
     *
     * @param optionsList
     * @return
     */
    private static String getBindingFiles(List optionsList) {
        for (int i = 0; i < optionsList.size(); i++) {
            if ("-b".equals((String) optionsList.get(i)) && i + 1 < optionsList.size()) {
                return "<bindingFiles>\n" + "<bindingFile>" + (String) optionsList.get(i + 1) + "</bindingFile>\n" +
                        "      </bindingFiles>";
            }
        }
        return "";
    }


    /**
     * For Axis2
     *
     * @param key
     * @param value
     * @param configurationMap
     * @return
     */
    public static String getConfiguratonElement(String key, CommandLineOption value, Map configurationMap, Map defaultValueMap) {
        String s = (String) configurationMap.get(key);
        if (s == null) {
            return "";
        }
        String stringValue = null;
        if (value.getOptionValue() == null) {
            if (defaultValueMap.containsKey(key)) {
                stringValue = (String) defaultValueMap.get(key);
            } else {
                stringValue = "true";
            }
        } else {
            stringValue = value.getOptionValue();
        }
        return "<" + s + ">" + stringValue + "</" + s + ">";
    }

    /**
     * For CXF JaxWS
     *
     * @param optionsList
     * @return
     */
    public static String getExtraArgsJaxWS(List optionsList) {
        String s = "<extraargs>\n";
        String[] extraArgsWithoutValue = new String[]{"-client", "-server", "-compile", "-impl", "-all", "-ant", "-autoNameResolution", "-v", "-verbose", "-quiet", "-interface", "-noTypes", "-generateEnums", "-supportMultipleXmlReps", "-inheritResourceParams", "-noVoidForEmptyResponses", "-validate", "-noAddressBinding", "-aer=true"};
        String[] extraArgsWithValue = new String[]{"-fe", "-db", "-exsh", "-dns", "-dex", "-wv", "-exceptionSuper"};
        //Special cases are used to avoid unpermitted values in html. So a mock value is used in HTML form and mapped it to the correct argument
        Map<String, String> specialCases = new HashMap<String, String>();
        specialCases.put("-mg", "-mark-generated");
        specialCases.put("-defaultValues", null);
        specialCases.put("-asyncMethods", null);
        specialCases.put("-bareMethods", null);
        specialCases.put("-mimeMethods", null);

        for (int i = 0; i < optionsList.size(); i++) {
            String argument = (String) optionsList.get(i);
            if (argument.startsWith("-")) {
                for (String temp : extraArgsWithoutValue) {
                    if (temp.equals(argument)) {
                        s += "<extraarg>" + argument + "</extraarg>";
                        break;
                    }
                }
                for (String temp : extraArgsWithValue) {
                    if (temp.equals(argument) && i + 1 < optionsList.size()) {
                        s += "<extraarg>" + argument + "</extraarg>";
                        s += "<extraarg>" + (String) optionsList.get(i + 1) + "</extraarg>";
                        break;
                    }
                }
                Iterator<String> iterator = specialCases.keySet().iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equalsIgnoreCase(argument)) {
                        String value = specialCases.get(argument);
                        if (value == null && i + 1 < optionsList.size()) {
                            s += "<extraarg>" + argument + "=" + (String) optionsList.get(i + 1) + "</extraarg>";
                            break;
                        } else {
                            s += "<extraarg>" + value + "</extraarg>";
                            break;
                        }
                    }

                }
            }
        }
        s += "</extraargs>\n";
        return s;
    }


    /**
     * For CXF JaxRS
     *
     * @param optionsList
     * @return
     */
    public static String getExtraArgsJaxRS(List optionsList) {
        String s = "<extraargs>\n";
        String[] extraArgsWithoutValue = new String[]{"-client", "-server", "-compile", "-impl", "-all", "-ant", "-autoNameResolution", "-v", "-verbose", "-quiet", "-exceptionSuper", "-interface", "-noTypes", "-generateEnums", "-supportMultipleXmlReps", "-inheritResourceParams", "-noVoidForEmptyResponses"};
        String[] extraArgsWithValue = new String[]{"-p", "-sp", "-tMap", "-repMap", "-catalog"};
        for (int i = 0; i < optionsList.size(); i++) {
            String argument = (String) optionsList.get(i);
            if (argument.startsWith("-")) {
                for (String temp : extraArgsWithoutValue) {
                    if (temp.equals(argument)) {
                        s += "<extraarg>" + argument + "</extraarg>";
                    }
                }
                for (String temp : extraArgsWithValue) {
                    if (temp.equals(argument) && i + 1 < optionsList.size()) {
                        s += "<extraarg>" + argument + "</extraarg>";
                        s += "<extraarg>" + (String) optionsList.get(i + 1) + "</extraarg>";
                    }
                }
            }
        }
        s += "</extraargs>\n";
        return s;
    }


    public static String getArgumentValue(String argument, List optionsList) {
        for (int i = 0; i < optionsList.size(); i++) {
            if (argument.equals((String) optionsList.get(i)) && i + 1 < optionsList.size()) {
                return (String) optionsList.get(i + 1);
            }
        }
        return null;
    }

    public static void generateJaxRSClient(List optionsList, String codegenOutputDirectory, HashMap<String, String> projOptionsList) throws Exception {
        String s = IOUtils.toString(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("org/wso2/carbon/wsdl2code/jaxrs-pom.xml"), "UTF-8");
        s = s.replace("gid", projOptionsList.get("-gid").toString()).
                replace("aid", projOptionsList.get("-aid").toString()).
                replace("vn", projOptionsList.get("-vn").toString());

//        URL url = getContainingArtifact(javax.ws.rs.core.Application.class);
//        String version = getVersion(url.getFile());

      //  s = s.replace("jsr311-version", version);

        String wadlUrl = getArgumentValue("-Service", optionsList);
        String wsdlOptions = getExtraArgsJaxRS(optionsList);
        s = String.format(s, wadlUrl, wsdlOptions);

        createFile(codegenOutputDirectory, s);
    }

    public static URL getContainingArtifact(Class clazz) {
        if (clazz == null ||
                clazz.getProtectionDomain() == null ||
                clazz.getProtectionDomain().getCodeSource() == null ||
                clazz.getProtectionDomain().getCodeSource().getLocation() == null)

            // This typically happens for system classloader
            // (java.lang.* etc. classes)
            return null;

        return clazz.getProtectionDomain()
                .getCodeSource().getLocation();
    }

    public static String getVersion(String artifact) throws Exception {


        java.io.File file = new java.io.File(artifact);
        java.util.jar.JarFile jar = new java.util.jar.JarFile(file);
        java.util.jar.Manifest manifest = jar.getManifest();

        String versionNumber = "";
        java.util.jar.Attributes attributes = manifest.getMainAttributes();
        if (attributes != null) {
            java.util.Iterator it = attributes.keySet().iterator();
            while (it.hasNext()) {
                java.util.jar.Attributes.Name key = (java.util.jar.Attributes.Name) it.next();
                String keyword = key.toString();
                if (keyword.equals("Implementation-Version") || keyword.equals("Bundle-Version")) {
                    versionNumber = (String) attributes.get(key);
                    break;
                }
            }
        }
        jar.close();

        if (versionNumber != null && !versionNumber.equals("")) {
            return versionNumber;
        }

        //if manifest does not contain version number it had to be extracted from the file name
        String fileName = file.getName().substring(0, file.getName().lastIndexOf("."));
        if (fileName.contains(".")) {
            String majorVersion = fileName.substring(0, fileName.indexOf("."));
            String minorVersion = fileName.substring(fileName.indexOf("."));
            int delimiter = majorVersion.lastIndexOf("-");
            if (majorVersion.indexOf("_") > delimiter) delimiter = majorVersion.indexOf("_");
            majorVersion = majorVersion.substring(delimiter + 1, fileName.indexOf("."));
            versionNumber = majorVersion + minorVersion;
        }

        return versionNumber;

    }

}
