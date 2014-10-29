var cal1;
YAHOO.util.Event.onDOMReady(function() {
    cal1 = new YAHOO.widget.Calendar("cal1", "cal1Container", { title:"Choose a date:", close:true });
    cal1.render();
    cal1.selectEvent.subscribe(calSelectHandler, cal1, true)
});
function showCalendar() {
    cal1.show();
}
function calSelectHandler(type, args, obj) {
    var selected = args[0];
    //var selDate = this.toDate(selected[0]);
    var selDate = args[0][0][0] + "/" + args[0][0][1] + "/" + args[0][0][2];
    var activeTime = document.getElementById("expirationTime");
    clearTextIn(activeTime);
    activeTime.value = selDate;
    cal1.hide();
}