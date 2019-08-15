/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.logging.view.internal;

import org.wso2.carbon.logging.view.data.LogEvent;
import org.wso2.carbon.utils.logging.CircularBuffer;

/**
 * Holder to keep References
 */
public class DataHolder {

    private CircularBuffer<LogEvent> logBuffer = new CircularBuffer<>(2000);

    private static final DataHolder instance = new DataHolder();

    private DataHolder() {

    }

    public CircularBuffer<LogEvent> getLogBuffer() {

        return logBuffer;
    }

    public static DataHolder getInstance() {

        return instance;
    }
}
