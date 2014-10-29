package org.wso2.carbon.wsdl2code.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.wsdl2code.stub.WSDL2CodeServiceStub;
import org.wso2.carbon.wsdl2code.stub.types.carbon.CodegenDownloadData;

import javax.activation.DataHandler;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

public class WSDL2CodeClient {

    private static final Log log = LogFactory.getLog(WSDL2CodeClient.class);
//    private static final String BUNDLE = "org.wso2.carbon.service.mgt.ui.i18n.Resources";
//    private ResourceBundle bundle;
    public WSDL2CodeServiceStub stub;

    public WSDL2CodeClient(ConfigurationContext configContext, String backendServerURL,
                           String cookie) throws AxisFault {

        String backendServiceURL = backendServerURL + "WSDL2CodeService";
        stub = new WSDL2CodeServiceStub(configContext, backendServiceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * invoke the back-end code generation methods and prompt to download the resulting zip file
     * containing generated code
     * @param options - code generation options
     * @param response - http servlet response
     * @throws AxisFault in case of error
     */
    public void codeGen(String[] options, HttpServletResponse response) throws AxisFault {
        try {
            ServletOutputStream out = response.getOutputStream();
            CodegenDownloadData downloadData = stub.codegen(options);
            if (downloadData != null) {
                DataHandler handler = downloadData.getCodegenFileData();
                response.setHeader("Content-Disposition", "fileName=" + downloadData.getFileName());
                response.setContentType(handler.getContentType());
                InputStream in = handler.getDataSource().getInputStream();
                int nextChar;
                while ((nextChar = in.read()) != -1) {
                    out.write((char) nextChar);
                }
                out.flush();
                in.close();
            } else {
                out.write("The requested service archive was not found on the server".getBytes());
            }
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }

    /**
     * invoke the back-end CXF code generation methods and prompt to download the resulting zip file
     * containing generated code
     * @param options - code generation options
     * @param response - http servlet response
     * @throws AxisFault in case of error
     */
    public void codeGenForCXF(String[] options, HttpServletResponse response) throws AxisFault {
        try {
            ServletOutputStream out = response.getOutputStream();
            CodegenDownloadData downloadData = stub.codegenForCXF(options);
            if (downloadData != null) {
                DataHandler handler = downloadData.getCodegenFileData();
                response.setHeader("Content-Disposition", "fileName=" + downloadData.getFileName());
                response.setContentType(handler.getContentType());
                InputStream in = handler.getDataSource().getInputStream();
                int nextChar;
                while ((nextChar = in.read()) != -1) {
                    out.write((char) nextChar);
                }
                out.flush();
                in.close();
            } else {
                out.write("The requested service archive was not found on the server".getBytes());
            }
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new AxisFault(e.getMessage(), e);
        }
    }
}
