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

package org.wso2.carbon.viewflows;

/**
 * this class keeps the data about a pirticular phase
 */

public class PhaseData {

    private String name;
    private HandlerData[] handlers;
    // whether this is a globally engaged phase or a phase limit only to
    // engaged operations

    private boolean isGlobalPhase;

    public PhaseData(String name) {
        this.name = name;
        this.handlers = new HandlerData[0];
    }

    public boolean getIsGlobalPhase() {
        return isGlobalPhase;
    }

    public void setIsGlobalPhase(boolean isGlobalPhase) {
        this.isGlobalPhase = isGlobalPhase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HandlerData[] getHandlers() {
        return handlers;
    }

    public void setHandlers(HandlerData[] handlers) {
        this.handlers = handlers;
    }
}
