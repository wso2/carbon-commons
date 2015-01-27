/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.ntask.core;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.*;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * This class contains utitilty functions related to tasks.
 */
public class TaskUtils {

    public static final String SECURE_VAULT_NS = "http://org.wso2.securevault/configuration";

    public static final String SECRET_ALIAS_ATTR_NAME = "secretAlias";

    public static final String TASK_STATE_PROPERTY = "TASK_STATE_PROPERTY";

    private static SecretResolver secretResolver;

    public static Registry getGovRegistryForTenant(int tid) throws TaskException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tid);
            return TasksDSComponent.getRegistryService().getGovernanceSystemRegistry(tid);
        } catch (RegistryException e) {
            throw new TaskException("Error in retrieving registry instance", Code.UNKNOWN, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static Document convertToDocument(File file) throws TaskException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        try {
            return fac.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            throw new TaskException("Error in creating an XML document from file: "
                    + e.getMessage(), Code.CONFIG_ERROR, e);
        }
    }

    private static void secureLoadElement(Element element) throws CryptoException {
        Attr secureAttr = element.getAttributeNodeNS(SECURE_VAULT_NS, SECRET_ALIAS_ATTR_NAME);
        if (secureAttr != null) {
            element.setTextContent(loadFromSecureVault(secureAttr.getValue()));
            element.removeAttributeNode(secureAttr);
        }
        NodeList childNodes = element.getChildNodes();
        int count = childNodes.getLength();
        Node tmpNode;
        for (int i = 0; i < count; i++) {
            tmpNode = childNodes.item(i);
            if (tmpNode instanceof Element) {
                secureLoadElement((Element) tmpNode);
            }
        }
    }

    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(TasksDSComponent.getSecretCallbackHandlerService()
                    .getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

    public static void secureResolveDocument(Document doc) throws TaskException {
        Element element = doc.getDocumentElement();
        if (element != null) {
            try {
                secureLoadElement(element);
            } catch (CryptoException e) {
                throw new TaskException("Error in secure load of document: " + e.getMessage(),
                        Code.UNKNOWN, e);
            }
        }
    }

    public static void setTaskState(TaskRepository taskRepo, String taskName,
            TaskManager.TaskState taskState) throws TaskException {
        taskRepo.setTaskMetadataProp(taskName, TASK_STATE_PROPERTY, taskState.toString());
    }

    public static TaskManager.TaskState getTaskState(TaskRepository taskRepo, String taskName)
            throws TaskException {
        String currentTaskState = taskRepo.getTaskMetadataProp(taskName, TASK_STATE_PROPERTY);
        if (currentTaskState != null) {
            for (TaskManager.TaskState taskState : TaskManager.TaskState.values()) {
                if (currentTaskState.equalsIgnoreCase(taskState.toString())) {
                    return taskState;
                }
            }
        }
        return null;
    }

    public static void setTaskPaused(TaskRepository taskRepo, String taskName, boolean paused)
            throws TaskException {
        if (paused) {
            setTaskState(taskRepo, taskName, TaskManager.TaskState.PAUSED);
        } else {
            setTaskState(taskRepo, taskName, TaskManager.TaskState.NORMAL);
        }
    }

    public static boolean isTaskPaused(TaskRepository taskRepo, String taskName)
            throws TaskException {
        TaskManager.TaskState currentState = getTaskState(taskRepo, taskName);
        if (currentState == null || !currentState.equals(TaskManager.TaskState.PAUSED)) {
            return false;
        } else
            return true;
    }

    public static void setTaskFinished(TaskRepository taskRepo, String taskName, boolean finished)
            throws TaskException {
        if (finished) {
            setTaskState(taskRepo, taskName, TaskManager.TaskState.FINISHED);
        } else {
            setTaskState(taskRepo, taskName, TaskManager.TaskState.NORMAL);
        }
    }

    public static boolean isTaskFinished(TaskRepository taskRepo, String taskName)
            throws TaskException {
        TaskManager.TaskState currentState = getTaskState(taskRepo, taskName);
        if (currentState == null || !currentState.equals(TaskManager.TaskState.FINISHED)) {
            return false;
        } else
            return true;
    }

}
