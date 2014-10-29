<h4><fmt:message key="in.fault.flow"/></h4>
<table id="inFaultFlowChain">
    <thead>
    <tr>
        <td>
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td>
                        <img src="extensions/core/images/handlerChain_leftmost_inflow.gif"/>
                    </td>
                    <%  {
                        int position = 0;
                        PhaseOrderData infaultflowPhaseOrderData = configData.getInfaultflowPhaseOrder();
                        for (PhaseData phaseData : infaultflowPhaseOrderData.getPhases()) {
                            position++;
                            String handlerColor = ((position % 2) == 1) ? "handler_01" : "handler_02";
                            if (position != 1) {%>
                    <td>
                        <img src="extensions/core/images/<%= handlerColor%>_left.gif"
                             border="0px"/>
                    </td>
                    <%
                        } %>
                    <td id="<%= handlerColor%>_BG">
                        <a href="handlers.jsp?retainlastbc=true&flow=inFault&phase=<%=phaseData.getName()%>"><%=phaseData.getName()%>
                        </a>
                    </td>
                    <td>
                        <img src="extensions/core/images/<%= handlerColor%>_right.gif"
                             border="0px"/>
                    </td>
                    <%
                        }
                        }
                    %>
                    <td>
                        <img src="extensions/core/images/handlerChain_rightmost_inflow.gif"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </thead>
</table>