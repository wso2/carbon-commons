<h4><fmt:message key="out.fault.flow"/></h4>
<table id="outFaultFlowChain">
    <tbody>
    <tr>
        <td>
            <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                    <td>
                        <img src="extensions/core/images/handlerChain_leftmost_outflow.gif"/>
                    </td>
                    <%
                        {
                        int position = 0;
                        PhaseOrderData outfaultflowPhaseOrderData = configData.getOutfaultPhaseOrder();
                        PhaseData[] phases = outfaultflowPhaseOrderData.getPhases();
                        for (int i = phases.length -1; i >=0; i--) { // reverse the phases and display
                            PhaseData phaseData = phases[i];
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
                        <a href="handlers.jsp?retainlastbc=true&flow=outFault&phase=<%=phaseData.getName()%>"><%=phaseData.getName()%>
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
                        <img src="extensions/core/images/handlerChain_rightmost_outflow.gif"/>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
    </tbody>
</table>