/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.tracer.service;
/*
 * 
 */

public class TracerServiceInfo {
    private String flag;
    private MessageInfo[] messageInfo;
    private MessagePayload lastMessage;
    private boolean empty;
    private boolean filter;
    private String filterString;
    private String tracePersister;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public MessageInfo[] getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(MessageInfo[] messageInfo) {
        this.messageInfo = messageInfo;
    }

    public MessagePayload getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessagePayload lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public String getFilterString() {
        return filterString;
    }

    public void setFilterString(String filterString) {
        this.filterString = filterString;
    }


    public String getTracePersister() {
        return tracePersister;
    }

    public void setTracePersister(String tracePersister) {
        this.tracePersister = tracePersister;
    }
}
