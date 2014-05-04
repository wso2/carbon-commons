package org.wso2.carbon.databridge.core.definitionstore;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionNotFoundException;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.Collection;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public interface StreamDefinitionStore {

    public StreamDefinition getStreamDefinition(String streamName,
                                                String streamVersion, int tenantId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException;

    public StreamDefinition getStreamDefinition(String streamId, int tenantId)
            throws StreamDefinitionNotFoundException, StreamDefinitionStoreException;


    public Collection<StreamDefinition> getAllStreamDefinitions(int tenantId);

    public void saveStreamDefinition(StreamDefinition streamDefinition, int tenantId)
            throws DifferentStreamDefinitionAlreadyDefinedException, StreamDefinitionStoreException;

    public boolean deleteStreamDefinition(String streamName, String streamVersion, int tenantId);

    public void subscribe(StreamAddRemoveListener streamAddRemoveListener);

    public void unsubscribe(StreamAddRemoveListener streamAddRemoveListener);


}
