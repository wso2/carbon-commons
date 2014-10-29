<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.transport.jms.stub.types.carbon.TransportParameter" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@page import="org.wso2.carbon.transport.jms.ui.JMSTransportAdminClient"%>
<%@ page import="java.util.Map" %>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>

<script type="text/javascript">
    function enableTx() {
        var txMode = document.getElementById('transport.Transactionality').value;
        var tx1 = document.getElementById('transport.UserTxnJNDIName');
        var tx2 = document.getElementById('transport.CacheUserTxn');
        var tx3 = document.getElementById('transport.jms.SessionTransacted');
        var tx4 = document.getElementById('transport.jms.SessionAcknowledgement');

        if (txMode == 'none') {
            tx1.setAttribute('disabled', 'true');
            tx2.setAttribute('disabled', 'true');
            tx3.setAttribute('disabled', 'true');
            tx4.setAttribute('disabled', 'true');
        } else {
            tx1.removeAttribute('disabled');
            tx2.removeAttribute('disabled');
            tx3.removeAttribute('disabled');
            tx4.removeAttribute('disabled');
        }
    }

    function durableSub(cbox) {
        var subName = document.getElementById("transport.jms.DurableSubscriberName");
        if (cbox.checked) {
            subName.removeAttribute('disabled');
        } else {
            subName.setAttribute('disabled','true');
        }
    }

    function typeSelected(sel) {
        var type = sel.value;
        if (type == 'topic') {
            document.getElementById('transport.jms.ConcurrentConsumers').value = 1;
            document.getElementById('transport.jms.MaxConcurrentConsumers').value = 1;
        }
    }

    function addFactory(confirmText) {
        if (!checkRequiredField(document.getElementById('connFacName').value)) {
            CARBON.showErrorDialog('Connection Factory Name must not be empty');
            return false;
        }
        if (!checkRequiredField(document.getElementById('java.naming.factory.initial').value)) {
            CARBON.showErrorDialog('Initial Context Factory Name must not be empty');
            return false;
        }
        if (!checkRequiredField(document.getElementById('java.naming.provider.url').value)) {
            CARBON.showErrorDialog('JNDI Provider URL must not be empty');
            return false;
        }
        if (!checkRequiredField(document.getElementById('transport.jms.ConnectionFactoryJNDIName').value)) {
            CARBON.showErrorDialog('JNDI Name must not be empty');
            return false;
        }

        if (document.getElementById('transport.jms.SubscriptionDurable').checked) {
            if (!checkRequiredField(document.getElementById('transport.jms.DurableSubscriberName').value)) {
                CARBON.showErrorDialog('A durable subscriber name must be specified when subscription durability is turned on');
                return false;
            }
        }

        var type = document.getElementById('transport.jms.ConnectionFactoryType').value;
        if (type == 'topic') {
            if (document.getElementById('transport.jms.ConcurrentConsumers').value != 1) {
                CARBON.showErrorDialog('The number of concurrent consumers must be 1 for topics');
                return false;
            }
            if (document.getElementById('transport.jms.MaxConcurrentConsumers').value != 1) {
                CARBON.showErrorDialog('The maximum number of concurrent consumers must be 1 for topics');
                return false;
            }
        }

        CARBON.showConfirmationDialog(confirmText,
            function() {
                document.addFactoryForm.submit();
            }, null);
    }

    function checkRequiredField(field) {
        if (field == null || field == undefined || field == '') {
            return false;
        }
        return true;
    }

    function setFieldValue(key, val) {
        var inputField = document.getElementById(key);
        if (inputField != null) {
            inputField.value = val;
        }
    }

    function deleteFactory() {
         CARBON.showConfirmationDialog('Are you sure you want to delete this connection factory?',
            function() {
                var del = document.getElementById('deleteFac');
                del.value = true;
                document.addFactoryForm.submit();
            }, null);
    }


</script>

<fmt:bundle basename="org.wso2.carbon.transport.jms.ui.i18n.Resources">
    <carbon:breadcrumb
            label="New Connection Factory"
            resourceBundle="org.wso2.carbon.transport.jms.ui.i18n.Resources"
            topPage="false"
            request="<%=request%>" />

    <%
        boolean update = false;
        boolean sender = false;
        boolean fromConfigPage = false;
        String connFacName = null;
        String backendServerURL;
        ConfigurationContext configContext;
        String cookie;
        JMSTransportAdminClient client;
        String service;
        Map<String,String> map = null;

        if ("true".equals(request.getParameter("isUpdate"))) {
            // Use the isUpdate parameter when updating an existing parameter
            // Also need to use the facName parameter with this
            update = true;
            connFacName = request.getParameter("facName");
        }

        if ("true".equals(request.getParameter("config"))) {
            fromConfigPage = true;
        }

        if ("true".equals(request.getParameter("isSender"))) {
            sender = true;
        }

        // Find out where to head next
        String backLocation = "./listener_enable.jsp?ordinal=1";
        if (!fromConfigPage && sender) {
            backLocation = "./sender_enable.jsp?ordinal=1";
        } else if (fromConfigPage && !sender) {
            backLocation = "./listener_config.jsp?ordinal=1";
        } else if (fromConfigPage && sender) {
            backLocation = "./sender_config.jsp?ordinal=1";
        }

        if (update && connFacName == null) {
            // Cannot update if the connection factory name is not specified
    %>
            <script type="text/javascript">
                CARBON.showErrorDialog('No connection factory is specified for update');
                location.href = '<%=backLocation%>';
            </script>
    <%
        }

        service = request.getParameter("_service");

        if (update && connFacName != null) {
            // Updating an existing parameter
            backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            configContext = (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            client = new JMSTransportAdminClient(cookie, backendServerURL,configContext);

            TransportParameter factory = client.getConnectionFactory(connFacName, service, !sender);
            if (factory == null) {
                // Errorneous or malicious input - Complaign and back off immediately
                %>
                    <script type="text/javascript">
                        CARBON.showErrorDialog('No connection factory exists by the name ' + <%=connFacName%>);
                        location.href = '<%=backLocation%>';
                    </script>
                <%    
            }
            map = client.getFactoryParameters(factory);
        }
    %>

    <div id="middle">
    <h2 id="listTransport"><fmt:message key="transport.mgmt"/></h2>
    <div id="workArea">
        <form name="addFactoryForm" action="handle_connection_factory.jsp">
            <input type="hidden" name="_trpSender" value="<%=sender%>"/>
            <input type="hidden" name="_update" value="<%=update%>"/>
            <input type="hidden" name="_backLocation" value="<%=backLocation%>"/>
            <input type="hidden" name="_deleteFac" id="deleteFac" value="false"/>
            <input type="hidden" name="_oriFacName" value="<%=connFacName%>"/>
            <table class="styledLeft" id="jmsTransport" width="100%">
                <tr>
                    <td style="border-left: 0px !important; border-right: 0px !important; border-top: 0px !important; padding-left: 0px !important;">
                        <%
                            if (update) {
                        %>
                                <h4><strong><fmt:message key="transport.jms.factory.edit"/></strong></h4>
                        <%
                            } else {
                        %>
                                <h4><strong><fmt:message key="transport.jms.factory.add"/></strong></h4>
                        <%
                            }
                        %>
                    </td>
                    <td style="border-left: 0px !important; border-right: 0px !important; border-top: 0px !important; padding-left: 0px !important;">
                        <%
                            if (update) {
                                String bLocation = backLocation.substring(2);
                        %>
                                <a href="#" onclick="javascript:deleteFactory('<%=connFacName%>', '<%=bLocation%>');">Delete This Connection Factory</a>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <tr>
                    <td class="sub-header"><fmt:message key="transport.parameter.name"/></td>
                    <td class="sub-header"><fmt:message key="transport.parameter.value"/></td>
                </tr>
                <tr>
                    <td>Connection Factory Name *</td>
                    <td><input type="text" name="conn.fac.name" id="connFacName" size="70"/></td>
                </tr>
                <tr>
                    <td colspan="2"><strong><fmt:message key="transport.jms.jndi.settings"/></strong></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.factory.initial"/> *</td>
                    <td><input type="text" name="java.naming.factory.initial" id="java.naming.factory.initial" size="70" value="org.apache.activemq.jndi.ActiveMQInitialContextFactory"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.conn.url"/> *</td>
                    <td><input type="text" name="java.naming.provider.url" id="java.naming.provider.url" size="70" value="tcp://localhost:61616"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.jndi.name"/> *</td>
                    <td><input type="text" name="transport.jms.ConnectionFactoryJNDIName" id="transport.jms.ConnectionFactoryJNDIName" size="70"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.jndi.username"/></td>
                    <td><input type="text" name="java.naming.security.principal" id="java.naming.security.principal"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.jndi.password"/></td>
                    <td><input type="password" name="java.naming.security.credentials" id="java.naming.security.credentials"/></td>
                </tr>
                <tr>
                    <td colspan="2"><strong><fmt:message key="transport.jms.tx.settings"/></strong></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.tx"/></td>
                    <td>
                        <select name="transport.Transactionality" id="transport.Transactionality" onchange="javascript:enableTx()">
                            <option value="none">None</option>
                            <option value="local">Local</option>
                            <option value="jta">JTA</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.tx.username"/></td>
                    <td><input type="text" name="transport.UserTxnJNDIName" size="70" id="transport.UserTxnJNDIName" disabled="true"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.cache.user.tx"/></td>
                    <td><input type="checkbox" name="transport.CacheUserTxn" checked="true" id="transport.CacheUserTxn" value="true" disabled="true"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.session.tx"/></td>
                    <td><input type="checkbox" name="transport.jms.SessionTransacted" id="transport.jms.SessionTransacted" value="true" disabled="true"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.session.ack"/></td>
                    <td>
                        <select name="transport.jms.SessionAcknowledgement" id="transport.jms.SessionAcknowledgement" disabled="true">
                            <option value="AUTO_ACKNOWLEDGE">AUTO_ACKNOWLEDGE</option>
                            <option value="CLIENT_ACKNOWLEDGE">CLIENT_ACKNOWLEDGE</option>
                            <option value="DUPS_OK_ACKNOWLEDGE">DUPS_OK_ACKNOWLEDGE</option>
                            <option value="SESSION_TRANSACTED">SESSION_TRANSACTED</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><strong><fmt:message key="transport.jms.conn.settings"/></strong></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.api.version"/></td>
                    <td>
                        <select name="transport.jms.JMSSpecVersion" id="transport.jms.JMSSpecVersion">
                            <option value="1.1">1.1</option>
                            <option value="1.0.2b">1.0.2b</option>                            
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.factory.type"/></td>
                    <td>
                        <select name="transport.jms.ConnectionFactoryType" id="transport.jms.ConnectionFactoryType" onchange="javascript:typeSelected(this)">
                            <option value="queue">Queue</option>
                            <option value="topic">Topic</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.conn.user"/></td>
                    <td><input type="text" name="transport.jms.UserName" id="transport.jms.UserName"/></td>
                </tr>
                <tr>
                    <td><fmt:message key="transport.jms.conn.pass"/></td>
                    <td><input type="password" name="transport.jms.Password" id="transport.jms.Password"/></td>
                </tr>
                <tr>
                    <td colspan="2"><strong>Destination Settings</strong></td>
                </tr>
                <tr>
                    <td>Destination JNDI Name</td>
                    <td><input type="text" name="transport.jms.Destination" id="transport.jms.Destination" size="70" /></td>
                </tr>
                <tr>
                    <td>Destination Type</td>
                    <td>
                        <select name="transport.jms.DestinationType" id="transport.jms.DestinationType">
                            <option value="queue">Queue</option>
                            <option value="topic">Topic</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>Default Reply Destination</td>
                    <td><input type="text" name="transport.jms.DefaultReplyDestination" id="transport.jms.DefaultReplyDestination" size="70" /></td>
                </tr>
                <tr>
                    <td>Reply Destination Type</td>
                    <td>
                        <select name="transport.jms.DefaultReplyDestinationType" id="transport.jms.DefaultReplyDestinationType">
                            <option value="queue">Queue</option>
                            <option value="topic">Topic</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2"><strong>Reconnection Settings</strong></td>
                </tr>
                <tr>
                    <td>Initial Reconnection Duration</td>
                    <td><input type="text" name="transport.jms.InitialReconnectDuration" id="transport.jms.InitialReconnectDuration" value="10000"/> ms</td>
                </tr>
                <tr>
                    <td>Reconnection Progress Factor</td>
                    <td><input type="text" name="transport.jms.ReconnectProgressFactor" id="transport.jms.ReconnectProgressFactor" value="2"/></td>
                </tr>
                <tr>
                    <td>Maximum Reconnection Duration</td>
                    <td><input type="text" name="transport.jms.MaxReconnectDuration" id="transport.jms.MaxReconnectDuration" value="3600000"/> ms</td>
                </tr>
                <tr>
                    <td colspan="2"><strong>Advanced Settings</strong></td>
                </tr>
                <tr>
                    <td>Message Selector</td>
                    <td><input type="text" name="transport.jms.MessageSelector" id="transport.jms.MessageSelector" size="70" /></td>
                </tr>
                <tr>
                    <td>Subscription Durable</td>
                    <td><input type="checkbox" name="transport.jms.SubscriptionDurable" id="transport.jms.SubscriptionDurable" value="true" onclick="durableSub(this)"/></td>
                </tr>
                <tr>
                    <td>Durable Subscriber Name</td>
                    <td><input type="text" name="transport.jms.DurableSubscriberName" size="70" id="transport.jms.DurableSubscriberName" disabled="true"/></td>
                </tr>
                <tr>
                    <td>Publish By the Same Connection</td>
                    <td><input type="checkbox" name="transport.jms.PubSubNoLocal" id="transport.jms.PubSubNoLocal" value="true" onclick="durableSub(this)"/></td>
                </tr>
                <tr>
                    <td>Cache Level</td>
                    <td>
                        <select name="transport.jms.CacheLevel" id="transport.jms.CacheLevel">
                            <option value="auto">auto</option>
                            <option value="connection">connection</option>
                            <option value="session">session</option>
                            <option value="consumer">consumer</option>
                            <option value="producer">producer</option>
                            <option value="none">none</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td>Receive Timeout</td>
                    <td><input type="text" name="transport.jms.ReceiveTimeout" id="transport.jms.ReceiveTimeout" value="10000"/> ms</td>
                </tr>
                <tr>
                    <td>Concurrent Consumers</td>
                    <td><input type="text" name="transport.jms.ConcurrentConsumers" id="transport.jms.ConcurrentConsumers" value="1" size="5"/></td>
                </tr>
                <tr>
                    <td>Maximum Concurrent Consumers</td>
                    <td><input type="text" name="transport.jms.MaxConcurrentConsumers" id="transport.jms.MaxConcurrentConsumers" value="1" size="5"/></td>
                </tr>
                <tr>
                    <td>Idle Task Limit</td>
                    <td><input type="text" name="transport.jms.IdleTaskLimit" id="transport.jms.IdleTaskLimit" value="10" size="5"/></td>
                </tr>
                <tr>
                    <td>Maximum Messages Per Task</td>
                    <td><input type="text" name="transport.jms.MaxMessagesPerTask" id="transport.jms.MaxMessagesPerTask" value="-1" size="5"/></td>
                </tr>
                <tr>
                    <td colspan="2" class="buttonRow">
                        <%
                            String btnText = "Add";
                            String confirmText = "Are you sure you want to add this new connection factory?";
                            if (update) {
                                btnText = "Update";
                                confirmText = "Are you sure you want to update this connection factory?";
                            }
                        %>
                        <input name="addBtn" type="button" value="<%=btnText%>" class="button" onclick="javascript:addFactory('<%=confirmText%>'); return false;"/>
                        <input name="backBtn" class="button" type="reset" value="Back"  onclick="javascript:window.location.href='<%=backLocation%>'; return false;" />
                    </td>
                </tr>
            </table>
        </form>

    <%
        if (update) {
            // We need to update the form with the values already available
            if (map != null) {
                for (String key : map.keySet()) {
    %>
                        <script type="text/javascript">
                            setFieldValue('<%=key%>', '<%=map.get(key)%>');
                        </script>
    <%
                }
            }
    %>
                <script type="text/javascript">
                    var nameBox = document.getElementById('connFacName');
                    nameBox.value = '<%=connFacName%>'
                </script>
    <%
        }
    %>

    </div>
    </div>
</fmt:bundle>