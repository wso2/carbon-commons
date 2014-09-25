/**
 *  Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.ndatasource.ui.config;

import org.wso2.carbon.ndatasource.ui.stub.NDataSourceAdminDataSourceException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.util.List;


@XmlRootElement (name = "configuration")
@XmlType (propOrder = {"driverClassName", "url", "username", "password", "defaultAutoCommit",
		"defaultReadOnly", "defaultTransactionIsolation", "defaultCatalog", "maxActive", "maxIdle", "minIdle",
		"initialSize", "maxWait", "testOnBorrow", "testOnReturn", "testWhileIdle", "validationQuery",
		"validatorClassName", "timeBetweenEvictionRunsMillis", "numTestsPerEvictionRun", "minEvictableIdleTimeMillis",
		"accessToUnderlyingConnectionAllowed", "removeAbandoned", "removeAbandonedTimeout", "logAbandoned", 
		"connectionProperties", "initSQL", 	"jdbcInterceptors", "validationInterval", "jmxEnabled", "fairQueue", 
		"abandonWhenPercentageFull", "maxAge", "useEquals", "suspectTimeout", "validationQueryTimeout" , "alternateUsernameAllowed",
		"dataSourceClassName", "dataSourceProps"})
public class RDBMSDSXMLConfiguration extends DSXMLConfiguration {
	
	private String url;
	
	private String driverClassName;
	
	private String username;
	
	private Password password;
	
	private Boolean defaultAutoCommit;
	
	private Boolean defaultReadOnly;
	
	private String defaultTransactionIsolation;
	
	private String defaultCatalog;
	
	private Integer maxActive;
	
	private Integer maxIdle;
	
	private Integer minIdle;
	
	private Integer initialSize;
	
	private Integer maxWait;
	
	private Boolean testOnBorrow;
	
	private Boolean testOnReturn;
	
	private Boolean testWhileIdle;
	
	private String validationQuery;
	
	private String validatorClassName;
	
	private Integer timeBetweenEvictionRunsMillis;
	
	private Integer numTestsPerEvictionRun;
	
	private Integer minEvictableIdleTimeMillis;
	
	private Boolean accessToUnderlyingConnectionAllowed;
	
	private Boolean removeAbandoned;
	
	private Integer removeAbandonedTimeout;
	
	private Boolean logAbandoned;
	
	private String connectionProperties;
	
	private String initSQL;

	private String jdbcInterceptors;
	
	private Long validationInterval;
	
	private Boolean jmxEnabled;
	
	private Boolean fairQueue;
	
	private Integer abandonWhenPercentageFull;
	
	private Long maxAge;
	
	private Boolean useEquals;
	
	private Integer suspectTimeout;

        private Integer validationQueryTimeout;
	
	private Boolean alternateUsernameAllowed;
	
	private String dataSourceClassName;
	
	private List<DataSourceProperty> dataSourceProps;
		
	private static Marshaller rdbmsMarshaller;
	
	public RDBMSDSXMLConfiguration() throws NDataSourceAdminDataSourceException {
		JAXBContext ctx;
		try {
			ctx = JAXBContext.newInstance(RDBMSDSXMLConfiguration.class);
			rdbmsMarshaller = ctx.createMarshaller();
		} catch (JAXBException e) {
			throw new NDataSourceAdminDataSourceException ("Error creating rdbms data source configuration info marshaller: "+ e.getMessage(), e);
		}
	}

	@XmlElement (name = "url", nillable = false)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement (name = "driverClassName", nillable = false)
	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
	
	@XmlElement (name = "username", nillable = false)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	@XmlElement (name = "password")
	public Password getPassword() {
		return password;
	}

	public void setPassword(Password password) {
		this.password = password;
	}
	
	public Boolean getDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	public void setDefaultAutoCommit(Boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	public Boolean getDefaultReadOnly() {
		return defaultReadOnly;
	}

	public void setDefaultReadOnly(Boolean defaultReadOnly) {
		this.defaultReadOnly = defaultReadOnly;
	}

	public String getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	public void setDefaultTransactionIsolation(String defaultTransactionIsolation) {
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}

	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public void setDefaultCatalog(String defaultCatalog) {
		this.defaultCatalog = defaultCatalog;
	}

	public Integer getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(Integer maxActive) {
		this.maxActive = maxActive;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

	public Integer getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(Integer initialSize) {
		this.initialSize = initialSize;
	}

	public Integer getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(Integer maxWait) {
		this.maxWait = maxWait;
	}

	public Boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(Boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public Boolean getTestOnReturn() {
		return testOnReturn;
	}

	public void setTestOnReturn(Boolean testOnReturn) {
		this.testOnReturn = testOnReturn;
	}

	public Boolean getTestWhileIdle() {
		return testWhileIdle;
	}

	public void setTestWhileIdle(Boolean testWhileIdle) {
		this.testWhileIdle = testWhileIdle;
	}

	public String getValidationQuery() {
		return validationQuery;
	}

	public void setValidationQuery(String validationQuery) {
		this.validationQuery = validationQuery;
	}

	public String getValidatorClassName() {
		return validatorClassName;
	}

	public void setValidatorClassName(String validatorClassName) {
		this.validatorClassName = validatorClassName;
	}

	public Integer getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}

	public Integer getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(Integer numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public Integer getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public Boolean getAccessToUnderlyingConnectionAllowed() {
		return accessToUnderlyingConnectionAllowed;
	}

	public void setAccessToUnderlyingConnectionAllowed(Boolean accessToUnderlyingConnectionAllowed) {
		this.accessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed;
	}

	public Boolean getRemoveAbandoned() {
		return removeAbandoned;
	}

	public void setRemoveAbandoned(Boolean removeAbandoned) {
		this.removeAbandoned = removeAbandoned;
	}

	public Integer getRemoveAbandonedTimeout() {
		return removeAbandonedTimeout;
	}

	public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
		this.removeAbandonedTimeout = removeAbandonedTimeout;
	}

	public Boolean getLogAbandoned() {
		return logAbandoned;
	}

	public void setLogAbandoned(Boolean logAbandoned) {
		this.logAbandoned = logAbandoned;
	}

	public String getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(String connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	public String getInitSQL() {
		return initSQL;
	}

	public void setInitSQL(String initSQL) {
		this.initSQL = initSQL;
	}

	public String getJdbcInterceptors() {
		return jdbcInterceptors;
	}

	public void setJdbcInterceptors(String jdbcInterceptors) {
		this.jdbcInterceptors = jdbcInterceptors;
	}

	public Long getValidationInterval() {
		return validationInterval;
	}

	public void setValidationInterval(Long validationInterval) {
		this.validationInterval = validationInterval;
	}

	public Boolean getJmxEnabled() {
		return jmxEnabled;
	}

	public void setJmxEnabled(Boolean jmxEnabled) {
		this.jmxEnabled = jmxEnabled;
	}

	public Boolean getFairQueue() {
		return fairQueue;
	}

	public void setFairQueue(Boolean fairQueue) {
		this.fairQueue = fairQueue;
	}

	public Integer getAbandonWhenPercentageFull() {
		return abandonWhenPercentageFull;
	}

	public void setAbandonWhenPercentageFull(Integer abandonWhenPercentageFull) {
		this.abandonWhenPercentageFull = abandonWhenPercentageFull;
	}

	public Long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Long maxAge) {
		this.maxAge = maxAge;
	}

	public Boolean getUseEquals() {
		return useEquals;
	}

	public void setUseEquals(Boolean useEquals) {
		this.useEquals = useEquals;
	}

	public Integer getSuspectTimeout() {
		return suspectTimeout;
	}

	public void setSuspectTimeout(Integer suspectTimeout) {
		this.suspectTimeout = suspectTimeout;
	}

	public Boolean getAlternateUsernameAllowed() {
		return alternateUsernameAllowed;
	}

	public void setAlternateUsernameAllowed(Boolean alternateUsernameAllowed) {
		this.alternateUsernameAllowed = alternateUsernameAllowed;
	}

	@XmlElement (name = "dataSourceClassName", nillable = false)
	public String getDataSourceClassName() {
		return dataSourceClassName;
	}

	public void setDataSourceClassName(String dataSourceClassName) {
		this.dataSourceClassName = dataSourceClassName;
	}

	@XmlElementWrapper (name = "dataSourceProps", nillable = false)
	@XmlElement (name = "property", nillable = false)
	public List<DataSourceProperty> getDataSourceProps() {
		return dataSourceProps;
	}

	public void setDataSourceProps(List<DataSourceProperty> dataSourceProps) {
		this.dataSourceProps = dataSourceProps;
	}

        public Integer getValidationQueryTimeout() {
                return validationQueryTimeout;
        }

        public void setValidationQueryTimeout(Integer validationQueryTimeout) {
                this.validationQueryTimeout = validationQueryTimeout;
        }

        @XmlRootElement (name = "password")
	public static class Password {
		
		private boolean encrypted = true;
		
		private String value;

		@XmlAttribute (name = "encrypted")
		public boolean isEncrypted() {
			return encrypted;
		}

		public void setEncrypted(boolean encrypted) {
			this.encrypted = encrypted;
		}

		@XmlValue
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
		
	@XmlRootElement (name = "property")
	public static class DataSourceProperty {
		
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

	@Override
	@XmlTransient
	public Marshaller getDSMarshaller() {
		return rdbmsMarshaller;
	}

}
