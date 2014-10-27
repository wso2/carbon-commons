package org.wso2.carbon.logging.sort;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.logging.service.data.LogEvent;
import org.wso2.carbon.logging.util.LoggingConstants;
public class LogEventSorter implements Callable<LogEvent[]>{

    private LogEvent[] events;
    
    private LogEvent[] sortedEvents;

    private List<LogEvent> preprocessedEvents;

    private static LogTaskThreadPoolExecuter executer;
	private static Log log = LogFactory.getLog(LogEventSorter.class);
     
    public LogEventSorter(LogEvent[] events, String priority) {
        this.events = events;
        executer = new LogTaskThreadPoolExecuter();
        this.preprocessedEvents = new ArrayList<LogEvent>();
       
//        if (!(priority == null || priority.equals("") || priority.equals("ALL"))) {
//        	   this.preProcessLogEvents(priority);
//        }
    }

    private LogEvent[] getLogEvents() {
        return this.events;
    }


    private void preProcessLogEvents(String priority) {
        priority = priority.toUpperCase();
        for (LogEvent event : this.getLogEvents()) {
            if (priority.equals(event.getPriority())) {
                this.getPreprocessedEvents().add(event);
            }
        }
    }

    private LogTaskThreadPoolExecuter getExecuter() {
        return executer;
    }

    public List<LogEvent> getPreprocessedEvents() {
        return preprocessedEvents;
    }

    public LogEvent[] getSortedEvents() {
		return sortedEvents;
	}
    
//	public void sort() {
//        this.getExecuter().runTask(new Runnable() {
//            public void run() {
////                LogEvent[] events = getPreprocessedEvents().toArray(
////                        new LogEvent[getPreprocessedEvents().size()]);
//                if (events != null) {
//                	sortedEvents = sortLogs(events, 0, events.length - 1);
//                    
//                }
//            }
//        });
//    }

    private static LogEvent[] sortLogs(LogEvent[] events, int left, int right) {
        int pivot = 0;

        if (left < right) {
            int initialPivot = left + ((right - left) / 2);
            pivot = partition(events, left, right, initialPivot);
        }

        if (pivot > 0) {
            sortLogs(events, left, pivot - 1);
        }
        if (pivot < events.length - 1 && pivot > 0) {
            sortLogs(events, pivot + 1, right);
        }
        return events;
    }

    private synchronized static int partition(LogEvent[] events1, int left, int right, int pivot) {
        int storeIndex;
        long t = createDateObject(events1[pivot].getLogTime()).getTime();
        LogEvent e1 = events1[right];
        events1[right] = events1[pivot];
        events1[pivot] = e1;

        storeIndex = left;
        for (int i = left; i < right; i++) {
            long t1 = createDateObject(events1[i].getLogTime()).getTime();
            if (t1 >t) {
                LogEvent e = events1[storeIndex];
                events1[storeIndex] = events1[i];
                events1[i] = e;
                storeIndex = storeIndex + 1;
            }
        }
        LogEvent e = events1[storeIndex];
        events1[storeIndex] = events1[right];
        events1[right] = e;

        return storeIndex;
    }

    private static Date createDateObject(String date) {
        Date d = null;
        DateFormat formatter;
        try {
        	formatter = new SimpleDateFormat(LoggingConstants.DATE_TIME_FORMATTER);
            d = formatter.parse(date);
        } catch (ParseException e) {
    		log.error("Illegal Date Format", e);
        }
        return d;
    }

	@Override
	public LogEvent[] call() throws Exception {
		LogEvent[] arr = new LogEvent[0];
		if (events != null) {
        	arr = sortLogs(events, 0, events.length - 1);
            
        }
		return arr;
	}

}