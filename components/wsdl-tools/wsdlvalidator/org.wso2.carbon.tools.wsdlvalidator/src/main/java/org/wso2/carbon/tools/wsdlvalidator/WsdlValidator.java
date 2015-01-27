
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

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.xerces.xs.XSModel;

import org.eclipse.wst.wsdl.validation.internal.Constants;
import org.eclipse.wst.wsdl.validation.internal.ControllerValidationInfo;
import org.eclipse.wst.wsdl.validation.internal.IValidationMessage;
import org.eclipse.wst.wsdl.validation.internal.exception.ValidateWSDLException;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.*;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.http.HTTPValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.mime.MIMEValidator;
import org.eclipse.wst.wsdl.validation.internal.wsdl11.soap.SOAPValidator;
import org.eclipse.wst.wsdl.validation.internal.resolver.URIResolver;
import org.eclipse.wst.wsdl.validation.internal.ValidationInfoImpl;
import org.eclipse.wst.wsdl.validation.internal.util.MessageGenerator;

import org.w3c.dom.Document;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.Definition;
import java.io.*;
import java.util.*;
import java.net.URL;

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
            throw new Exception(e);
        }
        WSDLValidationInfo info1 = validaWSDLFromURI(inputStream, null);
        return dataPacker(info1);
    }


   /*
    *     This method walidate WSDL file through url
    *  @param type - not used
    *  @param filedata - URL to the WSDL document
    *  @return - Report bean object
    */
    public Report validateFromUrl(String type, String url) throws Exception {
        HttpMethod httpMethod = new GetMethod(url);
        InputStream inputStream = httpMethod.getResponseBodyAsStream();

        WSDLValidationInfo info2 = validaWSDLFromURI(inputStream, url);
        return dataPacker(info2);
    }

   /*
    *     This method do the validation and send errors
    *  @param stream - an InputStream of a wsdl
    *  @param sourceURL - String that contains url to wsdl file
    *  @return - WSDLValidationInfo
    */
   private WSDLValidationInfo validaWSDLFromURI(InputStream stream,
                                                String sourceURL) throws Exception {
       InputStream inputStream = null;
       URL url;

       try {
           if (sourceURL == null) {
               File tempFile = File.createTempFile("temp", ".txt");
               tempFile.deleteOnExit();

               BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
               byte[] contentChunk = new byte[1024];
               int byteCount;
               while ((byteCount = stream.read(contentChunk)) != -1) {
                   out.write(contentChunk, 0, byteCount);
               }
               out.flush();
               url = tempFile.toURI().toURL();
           } else {
               url =  new URL(sourceURL);
           }
           inputStream = url.openStream();

           ResourceBundle rb = ResourceBundle.getBundle("validatewsdl");
           MessageGenerator messagegenerator = new MessageGenerator(rb);
           DocumentBuilder db;
           DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
           dbf.setNamespaceAware(true);

           try {
               db = dbf.newDocumentBuilder();
           } catch (Exception e) {
               dbf = DocumentBuilderFactory.newInstance();
               db = dbf.newDocumentBuilder();
           }

           Document doc = db.parse(inputStream);
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
}
