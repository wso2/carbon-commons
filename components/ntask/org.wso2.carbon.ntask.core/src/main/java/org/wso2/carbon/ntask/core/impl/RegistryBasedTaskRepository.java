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
package org.wso2.carbon.ntask.core.impl;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManagerId;
import org.wso2.carbon.ntask.core.TaskRepository;
import org.wso2.carbon.ntask.core.TaskUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Registry based task repository implementation.
 */
public class RegistryBasedTaskRepository implements TaskRepository {

    public static final String REG_TASK_BASE_PATH = "/repository/components/org.wso2.carbon.tasks";

    public static final String REG_TASK_REPO_BASE_PATH = REG_TASK_BASE_PATH + "/" + "definitions";

    private static Registry registry;

    private String taskType;

    private static Marshaller taskMarshaller;

    private static Unmarshaller taskUnmarshaller;

    private int tenantId;

    static {
        try {
            JAXBContext ctx = JAXBContext.newInstance(TaskInfo.class);
            taskMarshaller = ctx.createMarshaller();
            taskUnmarshaller = ctx.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Error creating task marshaller/unmarshaller: "
                    + e.getMessage());
        }
    }

    public RegistryBasedTaskRepository(int tenantId, String taskType) throws TaskException {
        this.tenantId = tenantId;
        this.taskType = taskType;
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    private static Marshaller getTaskMarshaller() {
        return taskMarshaller;
    }

    private static Unmarshaller getTaskUnmarshaller() {
        return taskUnmarshaller;
    }

    public static Registry getRegistry() throws TaskException {
        if (registry == null) {
            synchronized (RegistryBasedTaskRepository.class) {
                if (registry == null) {
                    registry = TaskUtils
                            .getGovRegistryForTenant(MultitenantConstants.SUPER_TENANT_ID);
                }
            }
        }
        return registry;
    }

    public String getTaskType() {
        return taskType;
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
    	/* a set is used here to exclude any possible duplicates */
        Set<TaskInfo> result = new HashSet<TaskInfo>();
        String tasksPath = this.getMyTasksPath();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                    MultitenantConstants.SUPER_TENANT_ID);
            if (getRegistry().resourceExists(tasksPath)) {
                Collection tasksCollection = (Collection) getRegistry().get(tasksPath);
                String[] taskPaths = tasksCollection.getChildren();
                TaskInfo taskInfo;
                for (String taskPath : taskPaths) {
                    taskInfo = this.getTaskInfoRegistryPath(taskPath);
                    result.add(taskInfo);
                }
            }
            return new ArrayList<TaskInfo>(result);
        } catch (Exception e) {
            throw new TaskException("Error in getting all tasks from repository: " + e.getMessage(),
                    Code.CONFIG_ERROR, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public TaskInfo getTask(String taskName) throws TaskException {
        String tasksPath = this.getMyTasksPath();
        String currentTaskPath = tasksPath + "/" + taskName;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            if (!getRegistry().resourceExists(currentTaskPath)) {
                throw new TaskException("The task '" + taskName + "' does not exist",
                        Code.NO_TASK_EXISTS);
            }
            TaskInfo taskInfo = this.getTaskInfoRegistryPath(currentTaskPath);
            return taskInfo;
        } catch (TaskException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskException("Error in loading task '" + taskName + "' from registry: " + e.getMessage(),
                    Code.CONFIG_ERROR, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public synchronized void addTask(TaskInfo taskInfo) throws TaskException {
        String tasksPath = this.getMyTasksPath();
        String currentTaskPath = tasksPath + "/" + taskInfo.getName();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            getTaskMarshaller().marshal(taskInfo, out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            Resource resource = getRegistry().newResource();
            resource.setContentStream(in);
            getRegistry().put(currentTaskPath, resource);
        } catch (Exception e) {
            throw new TaskException("Error in adding task '" + taskInfo.getName()
                    + "' to the repository: " + e.getMessage(), Code.CONFIG_ERROR, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public synchronized boolean deleteTask(String taskName) throws TaskException {
        String tasksPath = this.getMyTasksPath();
        String currentTaskPath = tasksPath + "/" + taskName;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            if (!getRegistry().resourceExists(currentTaskPath)) {
                return false;
            }
            getRegistry().delete(currentTaskPath);
            return true;
        } catch (RegistryException e) {
            throw new TaskException("Error in deleting task '" + taskName + "' in the repository",
                    Code.CONFIG_ERROR, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String getMyTasksPath() {
        return REG_TASK_REPO_BASE_PATH + "/" + this.getTenantId() + "/" + this.getTasksType();
    }

    private TaskInfo getTaskInfoRegistryPath(String path) throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            Resource resource = getRegistry().get(path);
            InputStream in = resource.getContentStream();
            TaskInfo taskInfo;
            /*
             * the following synchronized block is to avoid
             * "org.xml.sax.SAXException: FWK005" error where the XML parser is
             * not thread safe
             */
            synchronized (getTaskUnmarshaller()) {
                taskInfo = (TaskInfo) getTaskUnmarshaller().unmarshal(in);
            }
            in.close();
            taskInfo.getProperties().put(TaskInfo.TENANT_ID_PROP,
                    String.valueOf(this.getTenantId()));
            return taskInfo;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public String getTasksType() {
        return taskType;
    }

    public static List<TaskManagerId> getAvailableTenantTasksInRepo() throws TaskException {
        List<TaskManagerId> tmList = new ArrayList<TaskManagerId>();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            boolean result = getRegistry().resourceExists(
                    RegistryBasedTaskRepository.REG_TASK_REPO_BASE_PATH);
            Resource tmpRes;
            int tid;
            if (result) {
                tmpRes = getRegistry().get(RegistryBasedTaskRepository.REG_TASK_REPO_BASE_PATH);
                if (!(tmpRes instanceof Collection)) {
                    return tmList;
                }
                Collection tenantsCollection = (Collection) tmpRes;
                Collection tidPathCollection, taskTypePathCollection;
                for (String tidPath : tenantsCollection.getChildren()) {
                    tmpRes = getRegistry().get(tidPath);
                    if (!(tmpRes instanceof Collection)) {
                        continue;
                    }
                    tidPathCollection = (Collection) tmpRes;
                    for (String taskTypePath : tidPathCollection.getChildren()) {
                        tmpRes = getRegistry().get(taskTypePath);
                        if (!(tmpRes instanceof Collection)) {
                            continue;
                        }
                        taskTypePathCollection = (Collection) tmpRes;
                        if (taskTypePathCollection.getChildren().length > 0) {
                            try {
                                tid = Integer.parseInt(tidPath.substring(tidPath.lastIndexOf('/') + 1));
                                tmList.add(new TaskManagerId(tid, taskTypePath
                                        .substring(taskTypePath.lastIndexOf('/') + 1)));
                            } catch (NumberFormatException ignore) {
                                continue;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), Code.UNKNOWN, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return tmList;
    }

    public static List<TaskManagerId> getAllTenantTaskManagersForType(String taskType)
            throws TaskException {
        List<TaskManagerId> tmList = getAvailableTenantTasksInRepo();
        for (Iterator<TaskManagerId> itr = tmList.iterator(); itr.hasNext();) {
            if (!itr.next().getTaskType().equals(taskType)) {
                itr.remove();
            }
        }
        return tmList;
    }

    private Resource getTaskMetadataPropResource(String taskName) throws TaskException,
            RegistryException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            return getRegistry().get(
                    RegistryBasedTaskRepository.REG_TASK_REPO_BASE_PATH + "/"
                            + this.getTenantId() + "/" + this.getTasksType() + "/" + taskName);
        } catch (ResourceNotFoundException e) {
            throw new TaskException("The task '" + taskName + "' does not exist",
                    Code.NO_TASK_EXISTS, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void setTaskMetadataProp(String taskName, String key, String value) throws TaskException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            Resource res = this.getTaskMetadataPropResource(taskName);
            res.setProperty(key, value);
            getRegistry().put(res.getPath(), res);
        } catch (RegistryException e) {
            throw new TaskException("Error in setting task metadata properties: " + e.getMessage(),
                    Code.UNKNOWN, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public String getTaskMetadataProp(String taskName, String key) throws TaskException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            return this.getTaskMetadataPropResource(taskName).getProperty(key);
        } catch (TaskException e) {
            if (Code.NO_TASK_EXISTS.equals(e.getCode())) {
                /* if the task itself does not exist, we must return null */
                return null;
            } else {
                throw e;
            }
        } catch (RegistryException e) {
            throw new TaskException("Error in getting task metadata properties: " + e.getMessage(),
                    Code.UNKNOWN, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
