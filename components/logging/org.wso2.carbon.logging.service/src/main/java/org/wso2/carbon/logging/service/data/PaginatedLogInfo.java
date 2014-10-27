/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.logging.service.data;

import java.util.List;

import org.wso2.carbon.utils.Pageable;

/**
 * Bean for paginated Login Information
 */
public class PaginatedLogInfo implements Pageable {

	private LogInfo[] logInfo;
	private int numberOfPages;

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public LogInfo[] getLogInfo() {
		return logInfo;
	}

	public void setLogInfo(LogInfo[] logInfo) {
		this.logInfo = logInfo;
	}


	public <T> void set(List<T> items) {
		this.logInfo = items.toArray(new LogInfo[items.size()]);
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

}
