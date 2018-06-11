/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.ntask.core.impl;

import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManagerId;
import org.wso2.carbon.ntask.core.TaskRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Files based task repository implementation.
 */
public class FileBasedTaskRepository implements TaskRepository {


    private static final String REG_TASK_BASE_PATH = "/repository/components/org.wso2.carbon.tasks";

    private static final String REG_TASK_REPO_BASE_PATH = REG_TASK_BASE_PATH + "/" + "definitions";

    private static String resourcePath = getHome() + File.separator + "registry" + File.separator + "governance" + File.separator;


    private static final char URL_SEPARATOR_CHAR = '/';

    private String taskType;

    private static Marshaller taskMarshaller;

    private static Unmarshaller taskUnmarshaller;

    private int tenantId;

    HashMap<String, Properties> taskMetaPropMap = new HashMap<>();

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

    public FileBasedTaskRepository(int tenantId, String taskType) {
        this.tenantId = tenantId;
        this.taskType = taskType;
    }

    private String getMyTasksPath() {
        return REG_TASK_REPO_BASE_PATH + "/" + this.getTenantId() + "/" + this.getTasksType();
    }

    private static String getHome() {
        String carbonHome = System.getProperty("carbon.home");
        if (carbonHome == null || "".equals(carbonHome) || ".".equals(carbonHome)) {
            carbonHome = getSystemDependentPath(new File(".").getAbsolutePath());
        }
        return carbonHome;
    }

    private static String getSystemDependentPath(String path) {
        return path.replace(URL_SEPARATOR_CHAR, File.separatorChar);
    }

    @Override
    public List<TaskInfo> getAllTasks() throws TaskException {
        Set<TaskInfo> result = new HashSet<TaskInfo>();
        String tasksPath = this.getMyTasksPath();
        try {
            File resource = new File(new URI(getSystemDependentPath(resourcePath + tasksPath)));
            if (resource.exists()) {
                File[] taskPaths = resource.listFiles();
                TaskInfo taskInfo;
                if (taskPaths != null)
                    for (File taskPath : taskPaths) {
                        if (!taskPath.getName().startsWith("_meta_")) {
                            taskInfo = this.getTaskInfoRegistryPath(taskPath.getAbsolutePath());
                            result.add(taskInfo);
                        }
                    }
            }
            return new ArrayList<TaskInfo>(result);
        } catch (Exception e) {
            throw new TaskException("Error in getting all tasks from repository: " + e.getMessage(),
                    TaskException.Code.CONFIG_ERROR, e);
        }
    }

    private static Marshaller getTaskMarshaller() {
        return taskMarshaller;
    }

    private static Unmarshaller getTaskUnmarshaller() {
        return taskUnmarshaller;
    }

    private TaskInfo getTaskInfoRegistryPath(String path) throws Exception {
        try {

            InputStream in = new FileInputStream(path);
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
    public TaskInfo getTask(String taskName) throws TaskException {
        String tasksPath = this.getMyTasksPath();
        String currentTaskPath = tasksPath + "/" + taskName;
        try {
            File task = new File(getSystemDependentPath(resourcePath + currentTaskPath));
            if (!task.exists()) {
                throw new TaskException("The task '" + taskName + "' does not exist",
                        TaskException.Code.NO_TASK_EXISTS);
            }
            return this.getTaskInfoRegistryPath(resourcePath + currentTaskPath);
        } catch (TaskException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskException("Error in loading task '" + taskName + "' from registry: " + e.getMessage(),
                    TaskException.Code.CONFIG_ERROR, e);
        }
    }

    public static List<TaskManagerId> getAllTenantTaskManagersForType(String taskType)
            throws TaskException {
        List<TaskManagerId> tmList = getAvailableTenantTasksInRepo();
        for (Iterator<TaskManagerId> itr = tmList.iterator(); itr.hasNext(); ) {
            if (!itr.next().getTaskType().equals(taskType)) {
                itr.remove();
            }
        }
        return tmList;
    }

    private static List<TaskManagerId> getAvailableTenantTasksInRepo() throws TaskException {
        List<TaskManagerId> tmList = new ArrayList<TaskManagerId>();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            File file = new File(getSystemDependentPath(resourcePath + REG_TASK_BASE_PATH));
            boolean result = file.exists();
            int tid;
            if (result) {
                if (!(file.isDirectory())) {
                    return tmList;
                }
                if (file.listFiles() != null) {
                    for (File tidPath : file.listFiles()) {
                        if (!(tidPath.isDirectory())) {
                            continue;
                        }
                        if (tidPath.listFiles() != null) {
                            for (File taskTypePath : tidPath.listFiles()) {
                                if (!(taskTypePath.isDirectory())) {
                                    continue;
                                }
                                if (taskTypePath.listFiles() != null && taskTypePath.listFiles().length > 0) {
                                    try {
                                        tid = Integer.parseInt(tidPath.getAbsolutePath().substring(tidPath.getAbsolutePath().lastIndexOf('/') + 1));
                                        tmList.add(new TaskManagerId(tid, taskTypePath.getAbsolutePath()
                                                .substring(taskTypePath.getAbsolutePath().lastIndexOf('/') + 1)));
                                    } catch (NumberFormatException ignore) {
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new TaskException(e.getMessage(), TaskException.Code.UNKNOWN, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return tmList;
    }


    @Override
    public void addTask(TaskInfo taskInfo) throws TaskException {
        String tasksPath = this.getMyTasksPath();
        String currentTaskPath = tasksPath + "/" + taskInfo.getName();
        File file = new File(getSystemDependentPath(resourcePath + tasksPath));
        if (!file.exists()) {
            file.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(getSystemDependentPath(resourcePath + currentTaskPath))) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            getTaskMarshaller().marshal(taskInfo, out);
            fos.write(out.toByteArray());
        } catch (Exception e) {
            throw new TaskException("Error in adding task '" + taskInfo.getName()
                    + "' to the repository: " + e.getMessage(), TaskException.Code.CONFIG_ERROR, e);
        }
    }


    @Override
    public boolean deleteTask(String taskName) throws TaskException {
        String tasksPath = this.getMyTasksPath();
        String currentTaskPath = tasksPath + "/" + taskName;
        try {
            File file = new File(getSystemDependentPath(resourcePath + currentTaskPath));
            return file.exists() && file.delete();
        } catch (Exception e) {
            throw new TaskException("Error in deleting task '" + taskName + "' in the repository",
                    TaskException.Code.CONFIG_ERROR, e);
        }
    }

    @Override
    public String getTasksType() {
        return taskType;
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTaskMetadataProp(String taskName, String key, String value) throws TaskException {
        Properties propertyMap = taskMetaPropMap.get(getSystemDependentPath(resourcePath + taskName));
        if (propertyMap == null) {
            propertyMap = new Properties();
            taskMetaPropMap.put(getSystemDependentPath(resourcePath + taskName), propertyMap);
        }
        try {
            propertyMap.put(key, value);
            writeToMetaFile(propertyMap, taskName);
        } catch (Exception e) {
            throw new TaskException("Error in setting task metadata properties: " + e.getMessage(),
                    TaskException.Code.UNKNOWN, e);
        }
    }

    private void writeToMetaFile(Properties properties, String taskName) throws Exception {
        String tasksPath = this.getMyTasksPath();
        String currentTaskMetaPath = tasksPath + "/_meta_" + taskName;
        try (FileOutputStream fos = new FileOutputStream(getSystemDependentPath(resourcePath + currentTaskMetaPath))) {
            properties.store(fos, null);
        }
    }


    private Properties loadFromMetaFile(String taskName) throws Exception {
        Properties properties = new Properties();
        String tasksPath = this.getMyTasksPath();
        String currentTaskMetaPath = tasksPath + "/_meta_" + taskName;
        try (FileInputStream fis = new FileInputStream(getSystemDependentPath(resourcePath + currentTaskMetaPath))) {
            properties.load(fis);
        }
        return properties;
    }

    @Override
    public String getTaskMetadataProp(String taskName, String key) throws TaskException {
        Properties propertyMap = taskMetaPropMap.get(getSystemDependentPath(resourcePath + taskName));
        try {
            if (propertyMap == null) {
                propertyMap = loadFromMetaFile(taskName);
                taskMetaPropMap.put(getSystemDependentPath(resourcePath + taskName), propertyMap);
            }
            return propertyMap.getProperty(key);
        } catch (Exception e) {
            throw new TaskException("Error in getting task metadata properties: " + e.getMessage(),
                    TaskException.Code.UNKNOWN, e);
        }
    }
}
