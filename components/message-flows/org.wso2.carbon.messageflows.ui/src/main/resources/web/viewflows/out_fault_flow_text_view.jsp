<h3><fmt:message key="out.fault.flow"/></h3>
<table class="styledLeft">
    <%
        PhaseOrderData outfaultflowPhaseOrderData = configData.getOutfaultPhaseOrder();
        for (PhaseData phaseData: outfaultflowPhaseOrderData.getPhases()) {
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
            for (int j = handlers.length - 1; j >= 0; j--) {
                HandlerData handlerData = handlers[j];
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