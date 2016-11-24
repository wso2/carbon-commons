
/*
* Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.tools.wsdlvalidator;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.xs.XSModel;
import org.eclipse.wst.wsdl.validation.internal.Constants;
import org.eclipse.wst.wsdl.validation.internal.ControllerValidationInfo;
import org.eclipse.wst.wsdl.validation.internal.IValidationMessage;
import org.eclipse.wst.wsdl.validation.internal.ValidationInfoImpl;
import org.eclipse.wst.wsdl.validation.internal.exception.ValidateWSDLException;
import org.eclipse.wst.wsdl.validation.internal.resolver.URIResolver;
import org.eclipse.wst.wsdl.validation.internal.util.MessageGenerator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.ClassloaderWSDL11ValidatorDelegate;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.IWSDL11ValidationInfo;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.ValidatorRegistry;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.WSDL11BasicValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.WSDL11ValidationInfoImpl;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.WSDL11ValidatorController;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.WSDL11ValidatorDelegate;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.WSDLDocument;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.http.HTTPValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.mime.MIMEValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.soap.SOAPValidator;
import org.w3c.dom.Document;
import org.wso2.carbon.tools.wsdlvalidator.exception.WSDLValidatorException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.activation.DataHandler;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class WsdlValidator {
    public static final String WSDL_VALID = "WSDL DOCUMENT IS VALID";
    public static final String WSDL_VALID_I = "WSDL DOCUMENT IS VALID";
    public static final String WSDL_INVALID = " WSDL DOCUMENT IS INVALID";
    public static final String WSDL_INVALID_I = " WSDL DOCUMENT IS INVALID";

    /*
    *     This method walidate a uploded WSDL file
    *  @param type - not used
    *  @param filedata - DataHandler representing the WSDL
    *  @return - Report bean object
    */
    public Report validateFromFile(String type, DataHandler filedata) throws Exception {
        InputStream inputStream;
        try {
            inputStream = filedata.getDataSource().getInputStream();
        } catch (IOException e) {
            throw new WSDLValidatorException("Exception occurred when validating XML document", e);
        }
        WSDLValidationInfo info1 = validaWSDLFromURI(inputStream);
        return dataPacker(info1);
    }


   /*
    *     This method walidate WSDL file through url
    *  @param type - not used
    *  @param filedata - URL to the WSDL document
    *  @return - Report bean object
    */
    public Report validateFromUrl(String type, String url) throws Exception {
        InputStream inputStream = new URL(url).openStream();
        WSDLValidationInfo info2 = validaWSDLFromURI(inputStream);
        return dataPacker(info2);
    }

   /*
    *     This method do the validation and send errors
    *  @param stream - an InputStream of a wsdl
    *  @param sourceURL - String that contains url to wsdl file
    *  @return - WSDLValidationInfo
    */
   private WSDLValidationInfo validaWSDLFromURI(InputStream stream) throws Exception {
       InputStream inputStream = null;
       URL url;

       try {
           String sanitizedXMLString = sanitizeXMLFileData(stream);
           Document doc = secureParseXML(sanitizedXMLString);
           url = loadXMLToFile(doc);
           inputStream = url.openStream();

           ResourceBundle rb = ResourceBundle.getBundle("validatewsdl");
           MessageGenerator messagegenerator = new MessageGenerator(rb);
           WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
           reader.setFeature("javax.wsdl.importDocuments", true);
           Definition wsdlDefinition = reader.readWSDL(url.toString());
           ControllerValidationInfo validateInfo = new ValidationInfoImpl(url.toString(),
                   messagegenerator);

           URIResolver uriResolver = new URIResolver();
           ((ValidationInfoImpl) validateInfo).setURIResolver(uriResolver);
           /*java.util.Hashtable attributes = new java.util.Hashtable();
            ((ValidationInfoImpl) validateInfo).setAttributes(attributes);*/

           WSDL11ValidationInfoImpl info = new WSDL11ValidationInfoImpl(validateInfo);
           info.setElementLocations(new java.util.Hashtable());
           WSDL11BasicValidator validator = new WSDL11BasicValidator();
//           registerExtensionValidators(validator.getClass().getClassLoader());
           /*validator.setResourceBundle(rb);*/
           IValidationMessage[] messages;

           ExtendedWSDL11ValidatorController wsdl11ValidatorController =
                   new ExtendedWSDL11ValidatorController();
           WSDLDocument[] wsdlDocs = wsdl11ValidatorController.readWSDLDocument(doc,
                   validateInfo.getFileURI(),
                   messagegenerator,
                   info);
           WSDLDocument document = wsdlDocs[0];
           List schema = document.getSchemas();
           for (Object aSchema : schema) {
               info.addSchema((XSModel) aSchema);
           }
           // Set the element locations table.
           info.setElementLocations(document.getElementLocations());
           validator.validate(wsdlDefinition, new ArrayList(), info);
           messages = validateInfo.getValidationMessages();

           WSDLValidationInfo wsdlValidationInfo = new WSDLValidationInfo();
           if (messages.length > 0) {
               wsdlValidationInfo.setStatus(WSDL_INVALID);
           } else {
               wsdlValidationInfo.setStatus(WSDL_VALID);
           }
           for (IValidationMessage message : messages) {
               String messageString =
                       "[" + message.getLine() + "][" + message.getColumn() + "]"
                               + message.getMessage();
               wsdlValidationInfo.addValidationMessage(messageString);
           }
           return wsdlValidationInfo;
       } finally {
           if (inputStream != null) {
               inputStream.close();
           }
       }
   }

    /*
    *     This store the data into report bean
    *  @param info - WSDLValidationInfo
    *  @return - Report
    */
    private Report dataPacker(WSDLValidationInfo info) {

        Report rpt = new Report();
        ArrayList<String> msg = info.getValidationMessages();
        String[] result = msg.toArray(new String[0]);

        rpt.setResult(result);
        rpt.setStatus(info.getStatus());
        return rpt;
    }

    private static void registerExtensionValidators(ClassLoader classLoader) {
        WSDL11ValidatorDelegate delegate1 = new ClassloaderWSDL11ValidatorDelegate(
                WSDL11BasicValidator.class.getName(), classLoader);
        ValidatorRegistry.getInstance().registerValidator(com.ibm.wsdl.Constants.NS_URI_WSDL,
                delegate1);
        delegate1 = new ClassloaderWSDL11ValidatorDelegate(HTTPValidator.class.getName(),
                classLoader);
        ValidatorRegistry.getInstance().registerValidator(Constants.NS_HTTP, delegate1);
        delegate1 = new ClassloaderWSDL11ValidatorDelegate(SOAPValidator.class.getName(),
                classLoader);
        ValidatorRegistry.getInstance().registerValidator(Constants.NS_SOAP11, delegate1);
        delegate1 = new ClassloaderWSDL11ValidatorDelegate(MIMEValidator.class.getName(),
                classLoader);
        ValidatorRegistry.getInstance().registerValidator(Constants.NS_MIME, delegate1);
    }

    private static class ExtendedWSDL11ValidatorController extends WSDL11ValidatorController {

        @Override
        protected WSDLDocument[] readWSDLDocument(Document domModel, String file,
                                                  MessageGenerator messagegenerator,
                                                  IWSDL11ValidationInfo wsdlvalinfo)
                throws ValidateWSDLException {
            return super.readWSDLDocument(domModel, file, messagegenerator, wsdlvalinfo);
        }
    }

    /**
     * Load XML data to a temporary file.
     *
     * @param document XML DOM
     * @return URL of the file
     * @throws IOException          on error writing to file
     * @throws TransformerException on transforming error
     */
    private URL loadXMLToFile(Document document) throws TransformerException, IOException {
        DOMSource source = new DOMSource(document);
        File tempFile = File.createTempFile("temp", ".txt");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);
        StreamResult result = new StreamResult(writer);
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", null);
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
        return tempFile.toURI().toURL();
    }

    /**
     * Securely parse XML document.
     *
     * @param payload String XML
     * @return XML Document
     * @throws WSDLValidatorException on SAX, IO or parsing error
     */
    private Document secureParseXML(String payload) throws WSDLValidatorException {

        Document document;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
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
        } catch (ParserConfigurationException e) {
            throw new WSDLValidatorException("Error parsing XML document", e);
        } catch (SAXException e) {
            throw new WSDLValidatorException("SAX error in processing XML document", e);
        } catch (IOException e) {
            throw new WSDLValidatorException("IO error in processing XML document", e);
        }
        return document;
    }

    /**
     * Remove XML doc type declaration.
     *
     * @param inputStream file data input stream
     * @return Sanitized XML output
     * @throws IOException IOException
     */
    private String sanitizeXMLFileData(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        return writer.toString().replaceAll("\\<(\\!DOCTYPE[^\\>\\[]+(\\[[^\\]]+)?)+[^>]+\\>\n", "")
                .replaceAll("\n", "");
    }
}
