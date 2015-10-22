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

package org.wso2.carbon.deployment.synchronizer.subversion.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * This class resolves the checksum issues causing svn 1.6 in the .svn/entries file.
 * The error message from the SVNNotifyListener will trigger this.
 *
 */
public class SVNFileChecksumResolverUtil {

    private static final Log log = LogFactory.getLog(SVNFileChecksumResolverUtil.class);
    
    private static final String CHECKSUM_FILENAME = "entries";
    private static final String CHECKSUM_TEMP_FILENAME = "tmp_entries";
    private static final String SVN_FOLDER = ".svn";
    
	/**
	 * Resolves the checksum inconsistency in svn client when commit and update.
	 * 
	 * @param errorMessage Error message which is captured by the SVNNotifyListener logError
	 * method.
	 */
	public static void resolveChecksum(String errorMessage) {
		
        if(errorMessage.contains("Base checksum mismatch")) {
            int exIndex = errorMessage.indexOf("expected:");
            int acIndex = errorMessage.indexOf("actual:");
            int beginFile = errorMessage.indexOf("'", 0);
            int endFile = errorMessage.indexOf("'", beginFile +1);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String axis2RepoPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
            
//            Append file separator at the end if not present. Usually tenant mode its not present.
            if((axis2RepoPath.length() -1) != axis2RepoPath.lastIndexOf(File.separator)) {
            	axis2RepoPath = axis2RepoPath.concat(File.separator);
            }
            
            /*
            Expected and actual checksum values are interchanged in the exception trace 
            in svn client side.
             */
            String expectedChecksum = errorMessage.substring(acIndex+7, errorMessage.length()-1);
            String actualChecksum =  errorMessage.substring(exIndex+9, acIndex);
            String filePath = errorMessage.substring(beginFile+1, endFile);
            String svnFile = SVN_FOLDER + File.separator + CHECKSUM_FILENAME;
            String newSvnFile = SVN_FOLDER + File.separator + CHECKSUM_TEMP_FILENAME;
            
            int fileNameStartIndex = filePath.lastIndexOf(File.separatorChar) + 1;
            String svnPath = filePath.substring(0, fileNameStartIndex);
            
            String fullSvnFilePath = axis2RepoPath + svnPath + svnFile;
            String fulltmpFilePath = axis2RepoPath + svnPath + newSvnFile;
            File errorFile = new File(fullSvnFilePath);
            File tmpFile = new File(fulltmpFilePath);
            
            log.debug("Trying to correct the checksum mismatch for SVN file:" +fullSvnFilePath+ "." +
            		"Expected:" +expectedChecksum.trim()+ " but it is:" +actualChecksum.trim());

            try (Reader fis = new FileReader(errorFile);
                 BufferedReader bis = new BufferedReader(new InputStreamReader(new FileInputStream(errorFile)));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile))) {
                
                String line;
                while((line = bis.readLine()) != null){   
                	String plainLine = line.trim();
                	if(plainLine.contains(actualChecksum.trim())) {                		
                		log.debug("Found checksum:"+actualChecksum.trim()+". Will be replaced " +
                				"with:"+expectedChecksum.trim());
                		bw.write(expectedChecksum.trim());
                	}else {
                		bw.write(line);
                	}
                	bw.newLine();                	
                }
            } catch(Exception e){
                log.error(e.getMessage());
            }

//            Rename the tmp file and remove old file.
            if(errorFile.delete()){
            	log.debug("SVN file:" +fullSvnFilePath+ " modifying.");
                if(tmpFile.renameTo(errorFile)){
                	log.debug("SVN file:" +fullSvnFilePath+ " update successful.");
                }else {
                	log.error("SVN file:" +fulltmpFilePath+ " update failed. Please check file permissions and allow for modification.");
                }
            }else {
            	log.error("SVN file:" +fullSvnFilePath+ " unable to modify. Please check file permissions and allow for modification.");
            	log.error("Unable to resolve checksum mismatch");
            }

        }
	}
}
