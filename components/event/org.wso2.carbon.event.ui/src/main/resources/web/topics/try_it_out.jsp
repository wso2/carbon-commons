<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<fmt:bundle basename="org.wso2.carbon.event.ui.i18n.Resources">
 <%
     String topic = request.getParameter("t");
     String erorTopic = (String)session.getAttribute("errorTopic");
     String errorXML = "";
     if(session.getAttribute("xmlMessage") != null){
         errorXML = (String)session.getAttribute("xmlMessage");
     }
     session.removeAttribute("xmlMessage");
     session.removeAttribute("errorTopic");

%>

<carbon:breadcrumb
		label="Try It Out"
		resourceBundle="org.wso2.carbon.event.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />


    <script type="text/javascript">
            function validateStep1() {
                var topic = document.getElementById('topic').value;
                if (topic == '') {
                    CARBON.showWarningDialog("Topic is empty");
                    return false;
                }
                var message = document.getElementById('xmlMessage').value;
                if (message == '') {
                    CARBON.showWarningDialog("XML Message is empty");
                    return false;
                }
               /* $.post('try_it_out_invoke.jsp', {topic : topic,xmlMessage:message}, function (data) {
                    CARBON.showConfirmationDialog(data);
                });*/
                return true;
            }
        </script>

        <div id="middle">
                <h2><fmt:message key="try.it.out"/></h2>

            <div id="workArea">
                <form method="post"  name="dataForm" action="try_it_out_invoke_ajaxprocessor.jsp"
                      onsubmit="return validateStep1();">
                    <table class="styledLeft">
                        <tr>
                            <td class="formRaw">
                                <table class="normal">
                                    <tr>
                                        <td><fmt:message key="topic"/>
                                        </td>
                                        <td>
                                            <input class="longInput" type="text" readonly="true" name="topic"
                                                   id="topic" value="<%=topic%>"/>
                                        </td>

                                    </tr>
                                    <tr>
                                        <td><fmt:message key="xml.message"/></td>
                                        <td><textarea cols="50" rows="10" name="xmlMessage" id="xmlMessage"></textarea>=errorXML</td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input class="button" type="submit"
                                       value="<fmt:message key="service.invoke"/> >"/>
                                <input class="button" type="button" value="<fmt:message key="cancel"/>"
                                       onclick="location.href = 'index.jsp'"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div>
        </div>
    </fmt:bundle>
