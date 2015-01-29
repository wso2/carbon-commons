/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.email.verification.services;

import org.wso2.carbon.email.verification.util.Util;
import org.wso2.carbon.email.verification.util.ConfirmationBean;

/**
 * The service that is responsible for the email verification functionality of carbon.
 */
public class EmailVerificationService {

    /**
     * Confirms the link that user clicks is valid.
     * @param secretKey, the secretKey that is sent as a link in the email.
     * @return ConfirmationBean, if the link is valid.
     * @throws Exception, if the link is expired or invalid
     */
    public static ConfirmationBean confirmUser(String secretKey) throws Exception {
        return Util.confirmUser(secretKey);
    }
}
