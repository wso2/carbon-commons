/*
 * Copyright 2005,2012 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.logging.appender;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StreamDefinitionCache {

    private static volatile LoadingCache<String, StreamData> streamIdLoadingCache;


    private static void init() {
        if (streamIdLoadingCache != null) {
            return;
        }
        synchronized (StreamDefinitionCache.class) {
            if (streamIdLoadingCache != null) {
                return;
            }
            streamIdLoadingCache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterAccess(120, TimeUnit.MINUTES)
                    .build(new CacheLoader<String, StreamData>() {
                        @Override
                        public StreamData load(String tenantId) throws Exception {
                            return new StreamData(tenantId, "");
                        }
                    }
                    );
        }

    }

    public static StreamData getStream(String tenantId)
            throws ExecutionException {
        init();
        return streamIdLoadingCache.get(tenantId);
    }

    public static void putStream(String tenantId, String streamId, String date) {
        init();
        streamIdLoadingCache.put(tenantId, new StreamData(streamId, date));
    }
}
