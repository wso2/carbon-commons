/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.stratos.common.exception;

public class TenantMgtException extends Exception {

    private String errorCode = null;

    public TenantMgtException(String msg, Exception e) {

        super(msg, e);
    }

    public TenantMgtException(String msg) {

        super(msg);
    }

    public TenantMgtException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    public TenantMgtException(String errorCode, String message, Exception e) {

        super(message, e);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }
}
