<h3><fmt:message key="in.fault.flow"/></h3>
<table class="styledLeft">
    <%
        PhaseOrderData infaultflowPhaseOrderData = configData.getInfaultflowPhaseOrder();
        for (PhaseData phaseData : infaultflowPhaseOrderData.getPhases()) {
    %>
    <thead>
    <tr>
        <th colspan="2">&#160;&#160;<%= phaseData.getName()%>
        </th>
    </tr>
    </thead>
    <%
        HandlerData[] handlers = phaseData.getHandlers();
        if (handlers != null && handlers.length != 0) {
            int position = 0;
            for (HandlerData handlerData : handlers) {
                position++;
                String bgColor = ((position % 2) == 1) ? "#EEEFFB" : "white"; %>
    <tr bgcolor="<%= bgColor%>">
        <%
            String text = handlerData.getName() +
                          (handlerData.getPhaseLast() ? " (phaseLast)" : "");
        %>
        <td width="30%"><%= text %></td>
        <td><%= handlerData.getClassName()%></td>
    </tr>

    <%
        }
    } else { %>
    <tr>
        <td colspan="2"><fmt:message key="no.handlers.present"/>.
        </td>
    </tr>
    <% }%>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <% }%>
</table>