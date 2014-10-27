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

package org.wso2.carbon.user.mgt.bulkimport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import java.io.InputStream;

public class ExcelUserBulkImport {

    private static Log log = LogFactory.getLog(ExcelUserBulkImport.class);
    
    private BulkImportConfig config;

    public ExcelUserBulkImport(BulkImportConfig config) {
        super();
        this.config = config;
    }

    public void addUserList(UserStoreManager userStore) throws UserAdminException {
        try {
            Workbook wb = this.createWorkbook();
            Sheet sheet = wb.getSheet(wb.getSheetName(0));
            String password = config.getDefaultPassword();
           
            if (sheet == null || sheet.getLastRowNum() == -1) {
                throw new UserAdminException("The first sheet is empty");
            }
            int limit = sheet.getLastRowNum();
            boolean isDuplicate = false;
            boolean fail = false;
            boolean success = false;
            String lastError = "UNKNOWN";
            for (int i = 1; i < limit+1; i++) {
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(0);
                String userName = cell.getStringCellValue();
                if(userName != null && userName.trim().length() > 0){
                    try{
                        if (!userStore.isExistingUser(userName)) {
                            userStore.addUser(userName, password, null, null, null, true);
                            success = true;
                        } else {
                            isDuplicate = true;
                        }
                    } catch (Exception e){
                        if(log.isDebugEnabled()) {
                            log.debug(e);
                        }
                        lastError = e.getMessage();
                        fail = true;
                    }
                }
            }
            
            if (fail && success) {
                throw new UserAdminException("Error occurs while importing user names. " +
                        "Some user names were successfully imported. Some were not. Last error was : " + lastError);
            }

            if(fail && !success){
                throw new UserAdminException("Error occurs while importing user names. " +
                        "All user names were not imported. Last error was : " + lastError);
            }
            if (isDuplicate) {
                throw new UserAdminException("Detected duplicate user names. " +
                        "Failed to import duplicate users. Non-duplicate user names were successfully imported.");
            }            
        } catch (UserAdminException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public Workbook createWorkbook() throws Throwable {
        String filename = config.getFileName();
        InputStream ins = config.getInStream();
        Workbook wb = null;
        try {
            if (filename.endsWith(".xlsx")) {
                wb = new XSSFWorkbook(ins);
            } else {
                POIFSFileSystem fs = new POIFSFileSystem(ins);
                wb = new HSSFWorkbook(fs);
            }
        } catch (Exception e) {
            log.error("Bulk import failed" + e.getMessage(), e);
            throw new UserAdminException("Bulk import failed" + e.getMessage(), e);
        }
        return wb;
    }

}
