/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.usage.agent.util;

import java.io.IOException;
import java.io.Reader;


/**
 * this class is used to wrap the Reader object
 */
public class MonitoredReader extends Reader {
    Reader reader;
    long totalRead;

    public MonitoredReader(Reader reader) {
        this.reader = reader;
        totalRead = 0;
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        int read = reader.read(cbuf, off, len);
        totalRead += read;
        return read;
    }

    public void close() throws IOException {
        reader.close();
    }

    public long getTotalRead() {
        return totalRead;
    }
}
