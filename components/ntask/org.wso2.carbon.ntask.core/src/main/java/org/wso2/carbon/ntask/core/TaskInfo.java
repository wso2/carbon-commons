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

import org.wso2.carbon.ntask.common.TaskConstants.TaskMisfirePolicy;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a task job definition.
 */
@XmlRootElement(name = "taskInfo")
public class TaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String TENANT_ID_PROP = "__TENANT_ID_PROP__";

    private String name;

    private String taskClass;

    private Map<String, String> properties;
    
    private Map<String, String> locationResolverProperties;

    private String locationResolverClass;

    private TriggerInfo triggerInfo;

    @Deprecated
    public TaskInfo() {
    	this.setProperties(null);
    	this.setLocationResolverClass(TasksDSComponent.getTaskService()
                .getServerConfiguration().getLocationResolverClass());
        this.setLocationResolverProperties(TasksDSComponent.getTaskService()
                .getServerConfiguration().getLocationResolverProperties());
    }

    /**
     * TaskInfo constructor.
     * 
     * @param name The name of the task
     * @param taskClass The task implementation class
     * @param properties The properties that will be passed into the task implementation at runtime
     * @param triggerInfo Task trigger information
     */
    public TaskInfo(String name, String taskClass, Map<String, String> properties,
            TriggerInfo triggerInfo) {
    	this.name = name;
        this.taskClass = taskClass;
        this.setProperties(properties);
        this.triggerInfo = triggerInfo;
        if (this.getTriggerInfo() == null) {
            throw new IllegalArgumentException("Trigger information cannot be null");
        }
        this.setLocationResolverClass(TasksDSComponent.getTaskService()
                .getServerConfiguration().getLocationResolverClass());
        this.setLocationResolverProperties(TasksDSComponent.getTaskService()
                .getServerConfiguration().getLocationResolverProperties());
    }

    /**
     * TaskInfo constructor with custom TaskLocationResolver.
     * @param name The name of the task
     * @param taskClass The task implementation class
     * @param properties The properties that will be passed into the task implementation at runtime
     * @param locationResolverClass The TaskLocationResolver implementation, which is used to
     *            resolve the server location of the task at schedule time.
     * @param triggerInfo Task trigger information
     * @deprecated use setters to set location resolver related properties, if set explicitly,
     * users must have a way of changing this, i.e. using the UI, or else, the global tasks configuration
     * based settings must be used
     */
    @Deprecated
    public TaskInfo(String name, String taskClass, Map<String, String> properties,
            String locationResolverClass, TriggerInfo triggerInfo) {
        this.name = name;
        this.taskClass = taskClass;
        this.setProperties(properties);
        this.setLocationResolverClass(TasksDSComponent.getTaskService()
                .getServerConfiguration().getLocationResolverClass());
        this.triggerInfo = triggerInfo;
        if (this.getTriggerInfo() == null) {
            throw new IllegalArgumentException("Trigger information cannot be null");
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTaskClass(String taskClass) {
        this.taskClass = taskClass;
    }

    public void setProperties(Map<String, String> properties) {
    	this.properties = new HashMap<String, String>();
    	if (properties != null) {
            this.properties.putAll(properties);
    	}
    }

    public void setTriggerInfo(TriggerInfo triggerInfo) {
        this.triggerInfo = triggerInfo;
    }

    @XmlElement(name = "triggerInfo")
    public TriggerInfo getTriggerInfo() {
        return triggerInfo;
    }

    @XmlElement(name = "name")
    public String getName() {
        return name;
    }

    @XmlElement(name = "taskClass")
    public String getTaskClass() {
        return taskClass;
    }

    @XmlElement(name = "locationResolverClass")
    public String getLocationResolverClass() {
        return locationResolverClass;
    }

    public void setLocationResolverClass(String locationResolverClass) {
        this.locationResolverClass = locationResolverClass;
    }
    
    @XmlElementWrapper(name = "locationResolverProperties", required = false, nillable = true)
    public Map<String, String> getLocationResolverProperties() {
        return locationResolverProperties;
    }

    public void setLocationResolverProperties(Map<String, String> locationResolverProperties) {
    	this.locationResolverProperties = new HashMap<String, String>();
    	if (locationResolverProperties != null) {
            this.locationResolverProperties.putAll(locationResolverProperties);
    	}
    }

    @XmlElementWrapper(name = "properties")
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    @Override
    public boolean equals(Object rhs) {
        if (!(rhs instanceof TaskInfo)) {
        	return false;
        }
        return ((TaskInfo) rhs).getName().equals(this.getName());
    }

    /**
     * This class represents task trigger information.
     */
    @XmlRootElement(name = "triggerInfo")
    public static class TriggerInfo implements Serializable {

        private static final long serialVersionUID = 1L;

        private Date startTime;

        private Date endTime;

        private long intervalMillis;

        private int repeatCount;

        private String cronExpression;

        private TaskMisfirePolicy misfirePolicy = TaskMisfirePolicy.DEFAULT;

        private boolean disallowConcurrentExecution;

        public TriggerInfo() {
        }

        public TriggerInfo(Date startTime, Date endTime, long intervalMillis, int repeatCount) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.intervalMillis = intervalMillis;
            this.repeatCount = repeatCount;
        }

        public TriggerInfo(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        public void setDisallowConcurrentExecution(boolean disallowConcurrentExecution) {
            this.disallowConcurrentExecution = disallowConcurrentExecution;
        }

        public void setMisfirePolicy(TaskMisfirePolicy misfirePolicy) {
            this.misfirePolicy = misfirePolicy;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public void setIntervalMillis(long intervalMillis) {
            this.intervalMillis = intervalMillis;
        }

        public void setRepeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
        }

        public void setCronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        @XmlElement(name = "disallowConcurrentExecution")
        public boolean isDisallowConcurrentExecution() {
            return disallowConcurrentExecution;
        }

        @XmlElement(name = "misfirePolicy")
        public TaskMisfirePolicy getMisfirePolicy() {
            return misfirePolicy;
        }

        @XmlElement(name = "cronExpression")
        public String getCronExpression() {
            return cronExpression;
        }

        @XmlElement(name = "startTime")
        public Date getStartTime() {
            return startTime;
        }

        @XmlElement(name = "endTime")
        public Date getEndTime() {
            return endTime;
        }

        @XmlElement(name = "intervalMillis")
        public long getIntervalMillis() {
            return intervalMillis;
        }

        @XmlElement(name = "repeatCount")
        public int getRepeatCount() {
            return repeatCount;
        }

    }

}
