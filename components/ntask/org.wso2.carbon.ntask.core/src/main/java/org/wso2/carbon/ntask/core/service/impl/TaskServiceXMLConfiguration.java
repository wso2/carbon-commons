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
package org.wso2.carbon.ntask.core.service.impl;

import org.wso2.carbon.ntask.core.service.TaskService.TaskServerMode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * This represents the task service XML based configuration.
 */
@XmlRootElement(name = "tasks-configuration")
public class TaskServiceXMLConfiguration {

    private TaskServerMode taskServerMode;

    private int taskServerCount;

    private String taskClientDispatchAddress;

    private String remoteServerAddress;

    private String taskRepositoryClass;

    private String remoteServerUsername;

    private String remoteServerPassword;

    private DefaultLocationResolver defaultLocationResolver = new DefaultLocationResolver(DEFAULT_LOCATION_RESOLVER_CLASS);

    private TaskServerAvailabilityCheck taskServerAvailabilityCheck = new TaskServerAvailabilityCheck();

    public static final String DEFAULT_LOCATION_RESOLVER_CLASS = "org.wso2.carbon.ntask.core.impl.RoundRobinTaskLocationResolver";

    public static final int TASKSERVICE_AVAILABILITY_CHECK_RETRY_COUNT = 10;

    public static final int TASKSERVICE_AVAILABILITY_CHECK_RETRY_INTERVAL = 1000;

    public TaskServerMode getTaskServerMode() {
        return taskServerMode;
    }

    public void setTaskServerMode(TaskServerMode taskServerMode) {
        this.taskServerMode = taskServerMode;
    }

    @XmlElement(defaultValue = "-1")
    public int getTaskServerCount() {
        return taskServerCount;
    }

    public void setTaskServerCount(int taskServerCount) {
        this.taskServerCount = taskServerCount;
    }

    @XmlElement(nillable = true)
    public String getTaskClientDispatchAddress() {
        return taskClientDispatchAddress;
    }

    public void setTaskClientDispatchAddress(String taskClientDispatchAddress) {
        this.taskClientDispatchAddress = taskClientDispatchAddress;
    }

    @XmlElement(nillable = true)
    public String getRemoteServerAddress() {
        return remoteServerAddress;
    }

    public void setRemoteServerAddress(String remoteServerAddress) {
        this.remoteServerAddress = remoteServerAddress;
    }

    @XmlElement(nillable = true)
    public String getRemoteServerUsername() {
        return remoteServerUsername;
    }

    public void setRemoteServerUsername(String remoteServerUsername) {
        this.remoteServerUsername = remoteServerUsername;
    }

    @XmlElement(nillable = true)
    public String getRemoteServerPassword() {
        return remoteServerPassword;
    }

    public void setRemoteServerPassword(String remoteServerPassword) {
        this.remoteServerPassword = remoteServerPassword;
    }

    @XmlElement(name = "taskRepositoryClass", defaultValue = "org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository")
    public String getTaskRepositoryClass() {
        return taskRepositoryClass;
    }

    public void setTaskRepositoryClass(String taskRepositoryClass) {
        if (taskRepositoryClass != null) {
            this.taskRepositoryClass = taskRepositoryClass;
        } else {
            this.taskRepositoryClass = "org.wso2.carbon.ntask.core.impl.RegistryBasedTaskRepository";
        }
    }
    
    @XmlElement(name = "defaultLocationResolver", nillable = true, required = false)
    public DefaultLocationResolver getDefaultLocationResolver() {
		return defaultLocationResolver;
	}

	public void setDefaultLocationResolver(
			DefaultLocationResolver defaultLocationResolver) {
		if (defaultLocationResolver != null) {
		    this.defaultLocationResolver = defaultLocationResolver;
		}
	}

    @XmlElement(name = "taskServerAvailabilityCheck", nillable = true, required = false)
    public TaskServerAvailabilityCheck getTaskServerAvailabilityCheck() {
        return taskServerAvailabilityCheck;
    }

    public void setTaskServerAvailabilityCheck(TaskServerAvailabilityCheck taskServerAvailabilityCheck) {
        this.taskServerAvailabilityCheck = taskServerAvailabilityCheck;
    }

	public static class DefaultLocationResolver {
    	
    	private String locationResolverClass;
    	
    	private Property[] properties;
    	
    	public DefaultLocationResolver() {
    	}
    	
    	public DefaultLocationResolver(String locationResolverClass) {
    		this.locationResolverClass = locationResolverClass;
    	}
    	
    	@XmlElement(nillable = true, defaultValue = DEFAULT_LOCATION_RESOLVER_CLASS)
        public String getLocationResolverClass() {
            if (locationResolverClass == null) {
                return DEFAULT_LOCATION_RESOLVER_CLASS;
            }
            return locationResolverClass;
        }
    	
    	public void setLocationResolverClass(String locationResolverClass) {
            this.locationResolverClass = locationResolverClass;
        }
    	
    	@XmlElementWrapper(name = "properties")
    	@XmlElement(name = "property", required = false)
    	public Property[] getProperties() {
			return properties;
		}

		public void setProperties(Property[] properties) {
			this.properties = properties;
		}

		@XmlRootElement (name = "property")
		public static class Property {
    		
			private String name;
			
			private String value;

			@XmlAttribute (name = "name")
			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			@XmlValue
			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}	
			
		}
    	
    }

    public static class TaskServerAvailabilityCheck {

        private int retryCount;
        private long retryInterval;

        @XmlElement(defaultValue = "10")
        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }

        @XmlElement(defaultValue = "1000")
        public long getRetryInterval() {
            return retryInterval;
        }

        public void setRetryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
        }
    }

}
