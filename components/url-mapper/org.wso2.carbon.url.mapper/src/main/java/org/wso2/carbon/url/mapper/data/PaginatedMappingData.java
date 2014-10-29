package org.wso2.carbon.url.mapper.data;

import java.util.List;

import org.wso2.carbon.utils.Pageable;


public class PaginatedMappingData  implements Pageable {
	private MappingData[] mappingData;
	private int numberOfPages;

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public MappingData[] getMappingData() {
		return mappingData;
	}

	public void setMappingData(MappingData[] mappingData) {
		this.mappingData = mappingData;
	}


	public <T> void set(List<T> items) {
		this.mappingData = items.toArray(new MappingData[items.size()]);
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}
}
