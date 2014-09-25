package org.wso2.carbon.logging.service.data;

import java.util.List;

import org.wso2.carbon.utils.Pageable;

public class PaginatedLogEvent implements Pageable {
	private LogEvent[] logEvt;
	private int numberOfPages;

	public int getNumberOfPages() {
		return numberOfPages;
	}

	public LogEvent[] getLogInfo() {
		return logEvt;
	}

	public void setLogInfo(LogEvent[] logEvt) {
		this.logEvt = logEvt;
	}

	public <T> void set(List<T> items) {
		this.logEvt = items.toArray(new LogEvent[items.size()]);
	}

	public void setNumberOfPages(int numberOfPages) {
		this.numberOfPages = numberOfPages;
	}

}
