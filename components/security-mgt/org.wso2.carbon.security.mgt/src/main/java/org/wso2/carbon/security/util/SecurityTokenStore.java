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

package org.wso2.carbon.security.util;

import org.apache.rahas.SimpleTokenStore;
import org.apache.rahas.Token;
import org.apache.rahas.TrustException;


/**
 *
 */
public class SecurityTokenStore extends SimpleTokenStore {
 /*   private PersistenceManager pm = new PersistenceManager();

    public SecurityTokenStore() throws TrustException {

        // Load all the tokens from the DB
        SecurityTokenDO[] tokens = pm.getAllSecurityTokens();
        for (int i = 0; i < tokens.length; i++) {
            SecurityTokenDO tokenDO = tokens[i];
            Token token = tokenDO.toToken();
            super.tokens.put(token.getId(), token);
        }
    }

    public void add(Token token) throws TrustException {
        String tokenId;
        if (token != null &&
            token.getId() != null &&
            (tokenId = token.getId().trim()).length() != 0 &&
            getToken(tokenId) == null) {

            SecurityTokenDO tokenDO = new SecurityTokenDO(token.getId(),
                                                          token.getToken(),
                                                          token.getCreated(),
                                                          token.getExpires());
            tokenDO.update(token);
            try {
                pm.addToken(tokenDO);
            } catch (TokenAlreadyExistsException e) {
                throw new TrustException("Token already exists", e);
            }
            tokens.put(tokenId, token);
        }
    }

    public void update(Token token) throws TrustException {
        if (token != null && token.getId() != null && token.getId().trim().length() != 0) {

            if (!this.tokens.keySet().contains(token.getId())) {
                throw new TrustException("noTokenToUpdate", new String[]{token.getId()});
            }
            this.tokens.put(token.getId(), token);
            SecurityTokenDO tokenDO = pm.getSecurityToken(token.getId());
            tokenDO.update(token);
            pm.updateSecurityToken(tokenDO);
        }
    }*/
}
