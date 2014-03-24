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
import java.io.Writer;


/**
 * this class is used to wrap Writer object
 */
public class MonitoredWriter extends Writer {
    Writer writer;
    long totalWritten;

    public MonitoredWriter(Writer writer) {
        this.writer = writer;
        totalWritten = 0;
    }

    public void write(char cbuf[], int off, int len) throws IOException {
        totalWritten += (len - off);
        writer.write(cbuf, off, len);
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }

    public long getTotalWritten() {
        return totalWritten;
    }
}
