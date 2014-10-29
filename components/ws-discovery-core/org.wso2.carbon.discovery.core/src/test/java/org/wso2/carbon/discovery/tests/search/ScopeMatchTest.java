/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery.tests.search;

import junit.framework.TestCase;

import java.net.URI;
import java.util.UUID;

import org.wso2.carbon.discovery.search.ScopeMatchStrategy;
import org.wso2.carbon.discovery.search.RFC3986ScopeMatchStrategy;
import org.wso2.carbon.discovery.search.StrCmpScopeMatchStrategy;
import org.wso2.carbon.discovery.search.UUIDScopeMatchStrategy;

public class ScopeMatchTest extends TestCase {

    URI[] a = new URI[] {URI.create("http://wso2.org/carbon/ws/discovery/")};
    URI[] b = new URI[] {URI.create("http://wso2.org/carbon/ws/discovery")};
    URI[] c = new URI[] {URI.create("http://wso2.org/carbon/ws/")};
    URI[] d = new URI[] {URI.create("http://wso2.org/carbon/ws/security")};
    URI[] e = new URI[] {URI.create("http://wso2.com/carbon/ws/discovery")};
    URI[] f = new URI[] {URI.create("http://wso2.org/")};
    URI[] g = new URI[] {URI.create("http://wso2.org/")};

    public void testRFC3986Strategy() {
        ScopeMatchStrategy strategy = new RFC3986ScopeMatchStrategy();
        assertTrue(strategy.match(a,b));
        assertTrue(strategy.match(a,c));
        assertTrue(strategy.match(b,c));
        assertFalse(strategy.match(a,d));
        assertFalse(strategy.match(a,e));
        assertTrue(strategy.match(a,f));
    }

    public void testStrCmpStrategy() {
        ScopeMatchStrategy strategy = new StrCmpScopeMatchStrategy();
        assertTrue(strategy.match(a,a));
        assertTrue(strategy.match(f,g));
        assertFalse(strategy.match(a,c));
        assertFalse(strategy.match(a,e));
    }

    public void testUUIDStrategy() {
        ScopeMatchStrategy strategy = new UUIDScopeMatchStrategy();
        String uuid = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        assertTrue(strategy.match(new URI[] {URI.create(uuid)}, new URI[] {URI.create(uuid)}));
        assertFalse(strategy.match(new URI[] {URI.create(uuid)}, new URI[] {URI.create(uuid2)}));
    }
}