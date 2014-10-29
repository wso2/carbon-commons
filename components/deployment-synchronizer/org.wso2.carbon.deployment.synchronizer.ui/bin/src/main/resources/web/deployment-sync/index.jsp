<!--
 ~ Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->

<%@page import="org.apache.axis2.util.JavaUtils"%>
<%@page import="org.wso2.carbon.deployment.synchronizer.stub.types.util.RepositoryConfigParameter"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.deployment.synchronizer.ui.client.DeploymentSyncAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.deployment.synchronizer.stub.types.util.DeploymentSynchronizerConfiguration" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.deployment.synchronizer.ui.i18n.Resources">
    <carbon:breadcrumb
            label="deployment.sync.menu.text"
            resourceBundle="org.wso2.carbon.deployment.synchronizer.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>

    <script type="text/javascript">
        function disable() {
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=disable';
            form.submit();
            return true;
        }

        function update() {
            var period = document.getElementById('syncPeriod').value;
            if (period == null || period == '') {
                CARBON.showErrorDialog('Synchronization period has not been specified');
                return false;
            }
            if (isNaN(period) || period <= 0) {
                CARBON.showErrorDialog('Invalid value for synchronization period');
                return false;
            }
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=update';
            form.submit();
            return true;
        }

        function enable() {
            var period = document.getElementById('syncPeriod').value;
            if (period == null || period == '') {
                CARBON.showErrorDialog('Synchronization period has not been specified');
                return false;
            }
            if (isNaN(period) || period <= 0) {
                CARBON.showErrorDialog('Invalid value for synchronization period');
                return false;
            }
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=enable';
            form.submit();
            return true;
        }

        function enableEventingCheckbox() {
            var checkbox = document.getElementById('auto.checkout.chkbox');
            if (checkbox.checked) {
                document.getElementById('use.eventing.chkbox').removeAttribute('disabled');
            } else {
                document.getElementById('use.eventing.chkbox').setAttribute('disabled', 'true');
            }
            return true;
        }

        function getLastCommitTime() {
            jQuery.get('statusCheck-ajaxprocessor.jsp', { 'mode' : 'commit' },
                    function(data, status) {
                        var text;
                        if (data == 'error') {
                            text = 'Unable to get the last commit time';
                        } else {
                            text = data;
                        }
                        document.getElementById('lastCommitTimeCell').innerHTML = text;
                    });
            var timer1 = setTimeout("getLastCommitTime()",10000);
        }

        function getLastCheckoutTime() {
            jQuery.get('statusCheck-ajaxprocessor.jsp', { 'mode' : 'checkout' },
                    function(data, status) {
                        var text;
                        if (data == 'error') {
                            text = 'Unable to get the last checkout time';
                        } else {
                            text = data;
                        }
                        document.getElementById('lastCheckoutTimeCell').innerHTML = text;
                    });
            var timer2 = setTimeout("getLastCheckoutTime()",10000);
        }

        function commit() {
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=commit';
            form.submit();
            return true;
        }

        function checkout() {
            var form = document.getElementById('sync_form');
            form.action = 'index.jsp?action=checkout';
            form.submit();
            return true;
        }
        
        function changeRepoType(repoTypeElem){
        	var newValue = repoTypeElem.value;
        	var oldvalue = document.getElementById('currentRepo').value;
        	
        	var newTableId = newValue + ".ConfigTable";
        	var oldTableId = oldvalue + ".ConfigTable";
        	
        	var newTable = document.getElementById(newTableId);
        	var oldTable = document.getElementById(oldTableId);
        	
        	//if a table exists for the new repository 
        	if(newTable != null){
        		//Show the new table 
        		newTable.style.display = "";
        	}
        	//if a table exists for the old repository 
        	if(oldTable != null){
        		//Hide the old table 
        		oldTable.style.display = "none";
        	}
        	
        	document.getElementById('currentRepo').value = newValue;
        }
    </script>

    <%
        DeploymentSynchronizerConfiguration synchronizerConfiguration = null;
        boolean syncPerformed = false;
        String repoType = null;
        String[] repositoryTypes = null;
        boolean disableFields = false;
        DeploymentSyncAdminClient client = null;
        
        try {
        	boolean valid = true;
            String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new DeploymentSyncAdminClient(
                    configContext, backendServerURL, cookie, request.getLocale());

            boolean submitted = Boolean.parseBoolean(request.getParameter("submitted"));
            

            if (submitted) {
                String action = request.getParameter("action");
                
                if ("disable".equals(action)) {
                    client.disableSynchronizer();
                } else if ("enable".equals(action)) {
                	
                    DeploymentSynchronizerConfiguration newConfig = new DeploymentSynchronizerConfiguration();
                    newConfig.setEnabled(true);
                    newConfig.setAutoCommit(request.getParameter("autoCommit") != null);
                    newConfig.setAutoCheckout(request.getParameter("autoCheckout") != null);
                    newConfig.setPeriod(Long.parseLong(request.getParameter("syncPeriod")));
                    newConfig.setUseEventing(request.getParameter("useEventing") != null);
                    newConfig.setRepositoryType(request.getParameter("repo.type"));
                    
                    RepositoryConfigParameter[] parameters = client.getParamsByRepositoryType(newConfig.getRepositoryType());
                    if(parameters != null){
                    	for(int i=0; i<parameters.length; i++){
                    		String requestParam = request.getParameter(parameters[i].getName());
                    		
                    		//If a required parameter has not been specified.
                    		if(parameters[i].getRequired() && (requestParam == null || "".equals(requestParam))){
                    			valid = false;
                    			%>
                                <script type="text/javascript">
                                	CARBON.showInfoDialog("Please enter value for " + "<fmt:message key="<%=parameters[i].getName()%>"/>");
            			        </script>
                                <%
                                break;
                    		}
                    		parameters[i].setValue(requestParam);
                    	}
                    	newConfig.setRepositoryConfigParameters(parameters);
                    }
                    
                    if(valid){
                    	client.enableSynchronizer(newConfig);	                    		
                    }
                    else{
                    	synchronizerConfiguration = newConfig;
                    }
                } else if ("update".equals(action)) {
                	
                    DeploymentSynchronizerConfiguration newConfig = new DeploymentSynchronizerConfiguration();
                    newConfig.setEnabled(true);
                    newConfig.setAutoCommit(request.getParameter("autoCommit") != null);
                    newConfig.setAutoCheckout(request.getParameter("autoCheckout") != null);
                    newConfig.setPeriod(Long.parseLong(request.getParameter("syncPeriod")));
                    newConfig.setUseEventing(request.getParameter("useEventing") != null);
		    		newConfig.setRepositoryType(request.getParameter("repo.type"));
		    		
		    		RepositoryConfigParameter[] parameters = client.getParamsByRepositoryType(newConfig.getRepositoryType());
                    if(parameters != null && parameters.length != 0){
                    	for(int i=0; i<parameters.length; i++){
                    		String requestParam = request.getParameter(parameters[i].getName());
                    		
                    		//If a required parameter has not been specified.
                    		if(parameters[i].getRequired() && (requestParam == null || "".equals(requestParam))){
                    			valid = false;
                    			%>
                                <script type="text/javascript">
                                CARBON.showInfoDialog("Please enter value for " + "<fmt:message key="<%=parameters[i].getName()%>"/>");
            			        </script>
                                <%
                                break;
                    		}
                    		parameters[i].setValue(requestParam);
                    	}
                    	newConfig.setRepositoryConfigParameters(parameters);
                    }
		    		
                    if(valid){
                    	client.updateSynchronizer(newConfig);
                        %>
                        <script type="text/javascript">
                        	CARBON.showInfoDialog("<fmt:message key="deployment.sync.configupdate.success"/>");
    			        </script>
                        <%
                    }
                    else{
                    	synchronizerConfiguration = newConfig;
                    }
                } else if ("commit".equals(action)) {
                    if (client.getConfiguration().getEnabled()) {
                        client.commit();
                        syncPerformed = true;
                    }
                } else if ("checkout".equals(action)) {
                    if (client.getConfiguration().getEnabled()) {
                        client.checkout();
                        syncPerformed = true;
                    }
                }
            }

            if(valid){
            	synchronizerConfiguration = client.getConfiguration();        	
	        }
            repoType = synchronizerConfiguration.getRepositoryType();
            repositoryTypes = client.getRepositoryTypes();
            
            //If configuration has been specified in the carbon.xml, disable input fields.
            disableFields = synchronizerConfiguration.getServerBasedConfiguration();
            
        } catch (Exception e) {
        	session.setAttribute("depSyncErrorMessage", e.getMessage());
            //CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
        	<script type="text/javascript">
	        	jQuery.ajax({
	                type: "GET",
	                url: "displayerror-ajaxprocessor.jsp",
	                data: {},
	                success: function(data) {
	                	CARBON.showErrorDialog(data);
	                }
	            });
            </script>
    <%
            synchronizerConfiguration = new DeploymentSynchronizerConfiguration();
        }
    %>

    <div id="middle">
        <h2><fmt:message key="deployment.sync.menu.text"/></h2>
        <div id="workArea">
        	<%if(disableFields){ %>
	            <p>
	            	<font color="blue"><fmt:message key="deployment.sync.config.disabled"/></font>	
	            </p>
	            </br>
            <%} %>
            <p>
                <%
                    if (synchronizerConfiguration.getEnabled()) {
                %>
                <font color="green"><fmt:message key="deployment.sync.enabled"/></font>
                <%
                    } else {
                %>
                <font color="red"><fmt:message key="deployment.sync.disabled"/></font>
                <%
                    }
                %>
            </p>
            <p>&nbsp;</p>
            <form id="sync_form" action="" method="POST">
                <input name="submitted" type="hidden" value="true"/>
                <table id="syncTable" class="styledLeft">
                    <thead>
                        <tr>
                            <th colspan="2"><fmt:message key="deployment.sync.config"/></th>
                        </tr>
                    </thead>
                    <tbody>
                    	<tr>
                        	<td><fmt:message key="deployment.sync.repo.type"/></td>
                        	<td>
                        		<select id="repo.type" name="repo.type" onchange="changeRepoType(this)" 
                        			<%if(disableFields){%>disabled="disabled"<%}%>>
									<option value="registry" <%if (repoType == null || repoType.equalsIgnoreCase("registry")) {%>
											selected="selected"<%}%>>
										<fmt:message key="deployment.sync.repo.type.registry"/>
									</option>
									<%if(repositoryTypes != null){ 
										for(String repositoryType : repositoryTypes){
											if(repositoryType.equalsIgnoreCase("registry")){
												//Ignore registry type since it is the default repository type
												continue;	
											}%>
											<option value="<%=repositoryType%>" <%if (repoType.equalsIgnoreCase(repositoryType)) {%>
												selected="selected"<%}%>>
												<fmt:message key="<%="deployment.sync.repo.type." + repositoryType%>"/>
											</option>
										<%}
									} %>
								</select>
                        	</td>
                        </tr>
                        
                        <tr>
                            <td width="30%"><fmt:message key="deployment.sync.auto.commit"/></td>
                            <td>
                                <input id="auto.commit.chkbox" type="checkbox" name="autoCommit"
                                <%if(disableFields){%>disabled="disabled"<%}%>/>
                                <%
                                    if (synchronizerConfiguration.getAutoCommit()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('auto.commit.chkbox').setAttribute('checked', 'true');
                                </script>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="deployment.sync.auto.checkout"/></td>
                            <td>
                                <input id="auto.checkout.chkbox" type="checkbox" name="autoCheckout" onclick="enableEventingCheckbox();"
                                <%if(disableFields){%>disabled="disabled"<%}%>/>
                                <%
                                    if (synchronizerConfiguration.getAutoCheckout()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('auto.checkout.chkbox').setAttribute('checked', 'true');
                                </script>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="deployment.sync.use.eventing"/></td>
                            <td>
                                <input id="use.eventing.chkbox" type="checkbox" name="useEventing"
                                <%if(disableFields){%>disabled="disabled"<%}%>/>
                                <%
                                    if (synchronizerConfiguration.getUseEventing()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('use.eventing.chkbox').setAttribute('checked', 'true');
                                </script>
                                <%
                                    }

                                    if (!synchronizerConfiguration.getAutoCheckout()) {
                                %>
                                <script type="text/javascript">
                                    document.getElementById('use.eventing.chkbox').setAttribute('disabled', 'true');
                                </script>
                                <%
                                    }
                                %>
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="deployment.sync.period"/></td>
                            <td><input type="text" value="<%=synchronizerConfiguration.getPeriod()%>" name="syncPeriod" id="syncPeriod"
                            	<%if(disableFields){%>disabled="disabled"<%}%>/>s
                            </td>
                        </tr>
                    </tbody>
                </table>
                
                <%if(repositoryTypes != null){
	                for(String repositoryType : repositoryTypes){ 
	                	
	                	RepositoryConfigParameter[] configParams = client.getParamsByRepositoryType(repositoryType);
	                	
	                	if(configParams != null){%>
	                	
		                <table id="<%=repositoryType%>.ConfigTable" class="styledLeft" style="margin-top: 20px; display: <%=(repoType.equals(repositoryType)?"":"none")%>"> 
		                    <thead>
		                        <tr>
		                            <th colspan="2"><fmt:message key="<%="deployment.sync." + repositoryType + ".config"%>"/></th>
		                        </tr>
		                    </thead>
		                    <tbody>
		                    	<%for(int i=0; i<configParams.length; i++){ %>
		                    		<tr id="<%=repositoryType + ".row."+i%>">
		                    			<td width="30%">
		                    				<fmt:message key="<%=configParams[i].getName()%>"/>
		                    				<%if(configParams[i].getRequired()){%>
		                    					<span class="required">*</span>
		                    				<%} %>
		                    			</td>
		                    			<td>
			                    			<!-- If type of parameter is string. Display text/password field. If not, display check-box -->
			                        		<input id="<%=configParams[i].getName()%>" name="<%=configParams[i].getName()%>"
			                        			<% if(disableFields){%>
			                        				disabled="disabled"
			                        			<% }%>
			                        			<%if(configParams[i].getType().equalsIgnoreCase("string")){ %>
			                        				value="<%=configParams[i].getValue() == null ? "" : 
			                        						configParams[i].getValue()%>"
			                       					size=<%=configParams[i].getMaxlength()%>
			                     					<%if(configParams[i].getMasked()){ %>
			                     						type="password"
			                     					<%}
			                        				  else{%>
			                        				  	type="text"
			                        				<%} %>
			                        			<%}
			                        			else{%>
			                        				type="checkbox" 
			                        			<%} %> 
			                        		/>
			                        		<%if (configParams[i].getValue() != null && configParams[i].getValue().length() != 0) {%>
				                                <script type="text/javascript">
				                                    document.getElementById("<%=configParams[i].getName()%>").setAttribute('checked', 'true');
				                                </script>
			                                <%}%>
		                        		</td>
		                    		</tr>
		                    	<%} %>
		                    </tbody>
		                </table>
		                
		                <script type="text/javascript">
					        alternateTableRows('<%=repositoryType%>.ConfigTable', 'tableEvenRow', 'tableOddRow');
					    </script>
	                	<%}
	                } 
                
                }%>
                
                <input type="hidden" id="currentRepo" name="currentRepo"/>
                
                 <table id="buttonTable" class="styledLeft" style="margin-top: 10px;">
               		<tr>
                          <td class="buttonRow">
                              <%
                                  if (synchronizerConfiguration.getEnabled()) {
                              %>
                              <input type="button" class="button" onclick="update()" 
                              		<%if(disableFields){%>disabled="disabled" <%}%> 
                              		value="<fmt:message key="deployment.sync.update"/>"/>
                              <input type="button" class="button" onclick="disable(); return false;" 
                              		<%if(disableFields){%>disabled="disabled"<%}%>
                              		value="<fmt:message key="deployment.sync.disable"/>"/>
                              <%
                              } else {
                              %>
                              <input type="button" class="button" onclick="enable(); return false;" 
                              		<%if(disableFields){%>disabled="disabled"<%}%> 
                              		value="<fmt:message key="deployment.sync.enable"/>"/>
                              <%
                                  }
                              %>
                          </td>
                      </tr>
                 </table>
                
                <p>&nbsp;</p>
                <%
                    if (synchronizerConfiguration.getEnabled()) {
                %>
                    <table id="commitStatusTable" class="styledLeft">
                        <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="deployment.sync.commit.status"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td width="30%"><fmt:message key="deployment.sync.auto.commit"/></td>
                                <td>
                                    <%
                                        if (synchronizerConfiguration.getAutoCommit()) {
                                    %>
                                    <font color="green"><fmt:message key="deployment.sync.on"/></font>
                                    <%
                                        } else {
                                    %>
                                    <font color="red"><fmt:message key="deployment.sync.off"/></font>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="deployment.sync.last.commit"/></td>
                                <td id="lastCommitTimeCell"></td>
                            </tr>
                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <button class="button" onclick="commit()"><fmt:message key="deployment.sync.commit.now"/></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <p>&nbsp;</p>
                    
                    <table id="checkoutStatusTable" class="styledLeft">
                        <thead>
                            <tr>
                                <th colspan="2"><fmt:message key="deployment.sync.checkout.status"/></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td width="30%"><fmt:message key="deployment.sync.auto.checkout"/></td>
                                <td>
                                    <%
                                        if (synchronizerConfiguration.getAutoCheckout()) {
                                    %>
                                    <font color="green"><fmt:message key="deployment.sync.on"/></font>
                                    <%
                                        } else {
                                    %>
                                    <font color="red"><fmt:message key="deployment.sync.off"/></font>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr>
                                <td><fmt:message key="deployment.sync.last.checkout"/></td>
                                <td id="lastCheckoutTimeCell"></td>
                            </tr>
                            <tr>
                                <td class="buttonRow" colspan="2">
                                    <button class="button" onclick="checkout()"><fmt:message key="deployment.sync.checkout.now"/></button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                <%
                    }
                %>
            </form>
        </div>
    </div>

    <script type="text/javascript">
        alternateTableRows('syncTable', 'tableEvenRow', 'tableOddRow');
    </script>
    
    <script>
    	var selectedValue = document.getElementById('repo.type').value;
    	document.getElementById('currentRepo').value = selectedValue;
    </script>

    <%
        if (synchronizerConfiguration.getEnabled()) {
    %>
        <script type="text/javascript">
            alternateTableRows('commitStatusTable', 'tableEvenRow', 'tableOddRow');
            alternateTableRows('checkoutStatusTable', 'tableEvenRow', 'tableOddRow');
            getLastCommitTime();
            getLastCheckoutTime();
        </script>
    <%
        }

        if (syncPerformed) {
    %>
        <script type="text/javascript">
            CARBON.showInfoDialog('Synchronization operation performed successfully');
        </script>
    <%
        }
    %>

</fmt:bundle>
