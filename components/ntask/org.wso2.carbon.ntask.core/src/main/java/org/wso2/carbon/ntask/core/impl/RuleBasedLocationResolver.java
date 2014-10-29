/**
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.common.TaskException.Code;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskLocationResolver;
import org.wso2.carbon.ntask.core.TaskServiceContext;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;

import com.hazelcast.core.HazelcastInstance;

/**
 * This class represents a task location resolver, which assigns the locations
 * according to a filtering rules given as parameters. 
 * If task-type-pattern matches and task-name-pattern matches, check existing addresses of
 * address-pattern, and if addresses exist, select address in round-robin
 * fashion, if not move onto next rule in sequence, if none matches, the task is
 * not scheduled. 
 * <property name="rule-[order]">[task-type-pattern],[task-name-pattern],[address-pattern]</property>
 */
public class RuleBasedLocationResolver implements TaskLocationResolver {

	private static final String RULE_BASED_TASK_RESOLVER_ID = "__RULE_BASED_TASK_RESOLVER_ID__";
	
	private static final Log log = LogFactory.getLog(RuleBasedLocationResolver.class);
	
	private List<Rule> rules = new ArrayList<RuleBasedLocationResolver.Rule>();
	
	@Override
	public void init(Map<String, String> properties) throws TaskException {
		int seq;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			if (entry.getKey().startsWith("rule-")) {
				try {
				    seq = Integer.parseInt(entry.getKey().substring(5));
				} catch (NumberFormatException e) {
					throw new TaskException("The RuleBasedLocationResolver must have the property name in the format of "
							+ "rule-[sequence_number]", Code.CONFIG_ERROR);
				}
				this.rules.add(new Rule(seq, entry.getValue()));
			}
		}
		Collections.sort(this.rules);
	}

	@Override
	public int getLocation(TaskServiceContext ctx, TaskInfo taskInfo)
			throws TaskException {
		List<Integer> locations;
		/* if matched by no one, lets just assign it to the first server */
		int result = 0;
		for (Rule rule : this.rules) {
			try {
			    locations = rule.evaluate(ctx, taskInfo);
			} catch (Exception e) {
				throw new TaskException("Error in rule evaluation in RuleBasedLocationResolver: " + 
			            e.getMessage(), Code.UNKNOWN);
			}
			if (locations.size() > 0) {
				if (log.isDebugEnabled()) {
					log.debug("Task rule hit: " + rule + 
							" for task: [" + ctx.getTaskType() + "][" + taskInfo.getName() + "]");
				}
				result = this.getRoundRobinLocation(rule, locations);
				break;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Task location resolved to: " + result + 
					" for task: [" + ctx.getTaskType() + "][" + taskInfo.getName() + "]");
		}
		return result;
	}
	
	private int getRoundRobinLocation(Rule rule, List<Integer> locations) {
		HazelcastInstance hz = TasksDSComponent.getHazelcastInstance();
        if (hz == null) {
            return 0;
        }
        int result = (int) Math.abs(hz.getAtomicLong(RULE_BASED_TASK_RESOLVER_ID + rule.hashCode()).incrementAndGet());
        result = locations.get(result % locations.size());
        return result;
	}
	
	private class Rule implements Comparable<Rule> {
		
		private int sequence;
		
		private String taskTypePattern;
		
		private String taskNamePattern;
		
		private String addressPattern;
		
		public Rule(int sequence, String entry) throws TaskException {
			this.sequence = sequence;
			String[] tokens = entry.split(",");
			if (tokens.length != 3) {
				throw new TaskException("The RuleBasedLocationResolver must have the properties in the format of "
						+ "[task-type-pattern],[task-name-pattern],[address-pattern]", Code.CONFIG_ERROR);
			}
			this.taskTypePattern = tokens[0];
			this.taskNamePattern = tokens[1];
			this.addressPattern = tokens[2];
		}
		
		public int getSequence() {
			return sequence;
		}

		public String getTaskTypePattern() {
			return taskTypePattern;
		}

		public String getTaskNamePattern() {
			return taskNamePattern;
		}

		public String getAddressPattern() {
			return addressPattern;
		}
		
		@Override
		public int hashCode() {
			return (this.getSequence() + ":" + this.getTaskTypePattern() + ":" + 
		            this.getTaskNamePattern() + this.getAddressPattern()).hashCode();
		}

		@Override
		public int compareTo(Rule rhs) {
			return this.getSequence() - rhs.getSequence();
		}
		
		@Override
		public String toString() {
			return "Rule [" + this.getSequence() + "] - " + this.getTaskTypePattern() + 
					"," + this.getTaskNamePattern() + "," + this.getAddressPattern();
		}
		
		public List<Integer> evaluate(TaskServiceContext ctx, TaskInfo taskInfo) {
			List<Integer> result = new ArrayList<Integer>();
			if (ctx.getTaskType().matches(this.getTaskTypePattern())) {
				if (taskInfo.getName().matches(this.getTaskNamePattern())) {
					int count = ctx.getServerCount();
					InetSocketAddress sockAddr;
					InetAddress inetAddr;
					String ip = null, host1, host2 = null;
					for (int i = 0; i < count; i++) {
						sockAddr = ctx.getServerAddress(i);
						if (sockAddr != null) {
						    host1 = sockAddr.getHostName();
						    inetAddr = sockAddr.getAddress();
						    if (inetAddr != null) {
							    ip = inetAddr.getHostAddress();
							    host2 = inetAddr.getCanonicalHostName();
						    }
						    if (host1.matches(this.getAddressPattern())) {
							    result.add(i);
						    } else if (ip != null && ip.matches(this.getAddressPattern())) {
							    result.add(i);
						    } else if (!host1.equals(host2) && host2 != null && host2.matches(this.getAddressPattern())) {
							    result.add(i);
						    }
						} else {
							log.warn("RuleBasedLocationResolver: cannot find the host address for node: " + i);
						}					
					} 
				}
			}
			return result;
		}
		
	}

}
