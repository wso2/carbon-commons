/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.reporting.template.core.util.table;


import org.wso2.carbon.reporting.template.core.util.common.FontStyleDTO;

public class ColumnDTO {
    private FontStyleDTO columHeader;
    private FontStyleDTO tableCell;
    private FontStyleDTO columnFooter;
    private String columnHeaderName;
    private String columnFooterName;
    private String columnFamilyName;   //states which cassendra columnFamily the table should be connected
    private String columnName;        //stated which column should be accessed
    private boolean isPrimaryColumn;  //whether this is the column which is supposed to checked to form the relavent corresponding data

    public FontStyleDTO getColumHeader() {
        return columHeader;
    }

    public void setColumHeader(FontStyleDTO columHeader) {
        this.columHeader = columHeader;
    }

    public FontStyleDTO getColumnFooter() {
        return columnFooter;
    }

    public void setColumnFooter(FontStyleDTO columnFooter) {
        this.columnFooter = columnFooter;
    }

    public FontStyleDTO getTableCell() {
        return tableCell;
    }

    public void setTableCell(FontStyleDTO tableCell) {
        this.tableCell = tableCell;
    }

    public String getColumnHeaderName() {
        return columnHeaderName;
    }

    public void setColumnHeaderName(String columnHeaderName) {
        this.columnHeaderName = columnHeaderName;
    }

    public String getColumnFooterName() {
        return columnFooterName;
    }

    public void setColumnFooterName(String columnFooterName) {
        this.columnFooterName = columnFooterName;
    }

    public String getColumnFamilyName() {
        return columnFamilyName;
    }

    public void setColumnFamilyName(String columFamilyName) {
        this.columnFamilyName = columFamilyName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isPrimaryColumn() {
        return isPrimaryColumn;
    }

    public void setPrimaryColumn(boolean primaryColumn) {
        isPrimaryColumn = primaryColumn;
    }
}
