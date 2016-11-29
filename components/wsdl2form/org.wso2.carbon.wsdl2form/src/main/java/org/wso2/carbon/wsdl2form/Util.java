/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.wsdl2form;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.processor.TransformerFactoryImpl;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class Util {

    private static Log log = LogFactory.getLog(Util.class);
    public static final String WSDL2SIG_XSL_LOCATION = "xslt/wsdl2sig.xslt";
    public static final String JSSTUB_XSL_LOCATION = "xslt/jsstub.xslt";
    public static final String MOCKIT_XSL_LOCATION = "xslt/mockit.xslt";
    public static final String TRYIT_XSL_LOCATION = "xslt/tryit.xslt";
    public static final String VIEWIT_XSL_LOCATION = "xslt/viewit.xslt";

    public static String TRYIT_SG_NAME = "TryItMockServiceGroup";
    public static String LAST_TOUCH_TIME = "_LAST_TOUCH_TIME_";
    /*Touch time.*/
    public final static long PERIOD = 20 * 60 * 1000;

    public static DOMSource getSigStream(AxisService service, ByteArrayOutputStream wsdlOutStream, Map paramMap)
            throws TransformerFactoryConfigurationError, TransformerException,
                   ParserConfigurationException {
        Source wsdlSource = new StreamSource(new ByteArrayInputStream(wsdlOutStream.toByteArray()));
        InputStream sigStream =
                Util.class.getClassLoader().getResourceAsStream(WSDL2SIG_XSL_LOCATION);
        Source wsdl2sigXSLTSource = new StreamSource(sigStream);
        DocumentBuilder docB = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document docSig = docB.newDocument();
        Result resultSig = new DOMResult(docSig);
        Util.transform(wsdlSource, wsdl2sigXSLTSource, resultSig, paramMap, new SchemaURIResolver(service));
        return new DOMSource(docSig);
    }

    public static DOMSource getSigStream(AxisService axisService, Map paramMap)
            throws TransformerFactoryConfigurationError, TransformerException, AxisFault,
                   ParserConfigurationException {
        ByteArrayOutputStream wsdlOutStream = new ByteArrayOutputStream();
        axisService.printWSDL2(wsdlOutStream);
        return getSigStream(axisService, wsdlOutStream, paramMap);
    }

    /**
     * Transform based on parameters
     *
     * @param xmlIn    XML
     * @param xslIn    XSL
     * @param result   Result
     * @param paramMap Parameter map
     * @throws javax.xml.transform.TransformerException
     *          will be thrown
     */
    public static void transform(Source xmlIn, Source xslIn, Result result, Map paramMap)
            throws TransformerException {
        try {
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            Transformer transformer = transformerFactory.newTransformer(xslIn);
            if (paramMap != null) {
                Set set = paramMap.keySet();
                for (Object aSet : set) {
                    if (aSet != null) {
                        String key = (String) aSet;
                        String value = (String) paramMap.get(key);
                        transformer.setParameter(key, value);
                    }
                }
            }
            transformer.transform(xmlIn, result);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static void transform(Source xmlIn, Source xslIn, Result result, Map paramMap,
                                 URIResolver uriResolver) throws TransformerException {
        try {
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            transformerFactory.setURIResolver(uriResolver);
            Transformer transformer = transformerFactory.newTransformer(xslIn);
            if (paramMap != null) {
                Set set = paramMap.keySet();
                for (Object aSet : set) {
                    String key = (String) aSet;
                    String value = (String) paramMap.get(key);
                    transformer.setParameter(key, value);
                }
            }
            transformer.transform(xmlIn, result);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static void generateTryit(Source xmlIn, Result result, Map paramMap)
            throws TransformerException {
        InputStream tryItXSLTStream =
                Util.class.getClassLoader().getResourceAsStream(Util.TRYIT_XSL_LOCATION);
        Source tryItXSLSource = new StreamSource(tryItXSLTStream);
        Util.transform(xmlIn, tryItXSLSource, result, paramMap, new XSLTURIResolver());
    }

    public static void generateViewit(Source xmlIn, Result result, Map paramMap)
            throws TransformerException {
        InputStream viewItXSLTStream =
                Util.class.getClassLoader().getResourceAsStream(Util.VIEWIT_XSL_LOCATION);
        Source viewItXSLSource = new StreamSource(viewItXSLTStream);
        Util.transform(xmlIn, viewItXSLSource, result, paramMap);
    }

    public static void generateMockit(Source xmlIn, Result result, Map paramMap)
            throws TransformerException {
        InputStream tryItXSLTStream =
                Util.class.getClassLoader().getResourceAsStream(Util.MOCKIT_XSL_LOCATION);
        Source tryItXSLSource = new StreamSource(tryItXSLTStream);
        Util.transform(xmlIn, tryItXSLSource, result, paramMap);
    }

    public static void generateStub(Source xmlIn, Result result, Map paramMap)
            throws TransformerException {
        InputStream stubXSLTStream =
                Util.class.getClassLoader().getResourceAsStream(Util.JSSTUB_XSL_LOCATION);
        Source stubXSLSource = new StreamSource(stubXSLTStream);
        Util.transform(xmlIn, stubXSLSource, result, paramMap, new XSLTURIResolver());
    }

    /**
     * This will return location of the file written to the system filesystem.
     *
     * @param url WSDL location
     * @return String file absolute path
     * @throws AxisFault will be thrown
     */
    public static String writeWSDLToFileSystem(String url) throws AxisFault {
        return writeWSDLToFileSystemHelpler(url).getAbsolutePath();
    }

    /**
     * This will return an InputStream for the written file into the filesystem.
     *
     * @param url WSDL location
     * @return InputStream of the file
     * @throws java.io.IOException will be thrown
     */
    public static InputStream writeWSDLToStream(String url) throws IOException {
        return new FileInputStream(writeWSDLToFileSystemHelpler(url));
    }

    /**
     * Get generic location to write a file with the given suffix.
     *
     * @param suffix should be given with dot; ex: .xml, .wsdl
     * @return
     */
    public static FileInfo getOutputFileLocation(String suffix) throws CarbonException {
        String uuid = String.valueOf(System.currentTimeMillis() + Math.random());
        String extraFileLocation = MessageContext.
                getCurrentMessageContext().
                getConfigurationContext().getProperty("WORK_DIR") + File.separator + "extra" +
                                   File.separator + uuid + File.separator;
        File dirs = new File(extraFileLocation);
        if (!dirs.exists() && !dirs.mkdirs()) {
            throw new CarbonException("Unable to create directory " + dirs.getName());
        }
        File outFile = new File(extraFileLocation, uuid + suffix);
        return new FileInfo(uuid, outFile);
    }

    /**
     * Helper method to write the WSDL file to the filesytem.
     *
     * @param url WSDL location
     * @return File object
     * @throws AxisFault will be thrown
     */
    public static File writeWSDLToFileSystemHelpler(String url) throws AxisFault {
        try {
            InputStream inputStream = new URL(url).openStream();
            String sanitizedXMLString = sanitizeXMLFileData(inputStream);
            Document doc = secureParseXML(sanitizedXMLString);
            return loadXMLToFile(doc);
        } catch (IOException e) {
            throw new AxisFault("IO error in processing XML document", e);
        } catch (ParserConfigurationException e) {
            throw new AxisFault("Error parsing XML document", e);
        } catch (SAXException e) {
            throw new AxisFault("SAX error in processing XML document", e);
        } catch (TransformerException e) {
            throw new AxisFault("Error occurred when transforming the document safely", e);
        }
    }

    /**
     * Load XML data to a temporary file.
     *
     * @param document XML URL
     * @return URL of the file
     * @throws IOException          on error writing to file
     * @throws TransformerException on transforming error
     */
    private static File loadXMLToFile(Document document) throws TransformerException, IOException {
        DOMSource source = new DOMSource(document);
        File tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);
        StreamResult result = new StreamResult(writer);
        TransformerFactory transformerFactory;
        try {
            transformerFactory = TransformerFactory
                    .newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
        } catch (NoSuchMethodError e) {
            log.info("TransformerFactory.newInstance(String, ClassLoader) method not found. " +
                    "Using TransformerFactory.newInstance()");
            transformerFactory = TransformerFactory.newInstance();
        }
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
        return tempFile;
    }

    /**
     * Securely parse XML document.
     *
     * @param payload String XML
     * @return XML Document
     * @throws ParserConfigurationException error parsing xml
     * @throws IOException                  IO error in processing XML document
     * @throws SAXException                 SAX error in processing XML document
     */
    private static Document secureParseXML(String payload)
            throws ParserConfigurationException, IOException, SAXException {

        Document document;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setNamespaceAware(true);

        // Perform namespace processing
        dbf.setFeature("http://xml.org/sax/features/namespaces", true);

        // Validate the document and report validity errors.
        dbf.setFeature("http://xml.org/sax/features/validation", true);

        // Build the grammar but do not use the default attributes and attribute types information it contains.
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);

        // Ignore the external DTD completely.
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource inputSource = new InputSource();
        inputSource.setCharacterStream(new StringReader(payload));
        document = db.parse(inputSource);
        return document;
    }

    /**
     * Remove XML doc type declaration.
     *
     * @param inputStream file data input stream
     * @return Sanitized XML output
     * @throws IOException IOException
     */
    private static String sanitizeXMLFileData(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        return writer.toString().replaceAll("\\<(\\!DOCTYPE[^\\>\\[]+(\\[[^\\]]+)?)+[^>]+\\>\n", "")
                .replaceAll("\n", "");
    }

    /**
     * Class that holds uuid and file info.
     */
    public static class FileInfo {

        private String uuid;
        private File file;

        public FileInfo(String uuid, File file) {
            this.uuid = uuid;
            this.file = file;
        }

        public String getUuid() {
            return uuid;
        }

        public File getFile() {
            return file;
        }
    }


}
