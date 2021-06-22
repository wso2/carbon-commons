/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stratos.common.exception;

/**
 * This class is defined as the general client exception implementation for Stratos. This can be used
 * within Stratos components when handling client exceptions.
 */
public class StratosClientException extends StratosException {

    private static final long serialVersionUID = 7990833632284117835L;
    private String errorCode = null;

    public StratosClientException() {

    }

    public StratosClientException(String message) {

        super(message);
    }

    public StratosClientException(String errorCode, String message) {

        super(message);
        this.errorCode = errorCode;
    }

    public StratosClientException(String message, Throwable cause) {

        super(message, cause);
    }

    public StratosClientException(String errorCode, String message, Throwable cause) {

        super(message, cause);
        this.errorCode = errorCode;
    }

    public StratosClientException(Throwable cause) {

        super(cause);
    }

    public String getErrorCode() {

        return errorCode;
    }

    public void setErrorCode(String errorCode) {

        this.errorCode = errorCode;
    }
}
