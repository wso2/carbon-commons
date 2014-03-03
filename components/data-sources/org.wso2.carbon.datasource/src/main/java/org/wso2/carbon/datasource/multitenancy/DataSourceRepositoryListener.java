package org.wso2.carbon.datasource.multitenancy;

import org.apache.synapse.commons.datasource.DataSourceInformationRepository;

/**
 * This interface acts as an listener to a specific tenant's data source repository.
 */
public interface DataSourceRepositoryListener {

	public void setDataSourceRepository(DataSourceInformationRepository repo);
	
}
