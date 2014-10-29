var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;
var oMenu = null;
function addRightClicks(){
    Event.onAvailable("domChecker", function() {
        var topicTree =  Dom.get("topicTree");
        var aNodes = topicTree.getElementsByTagName("a");
        for (var i = 0; i < aNodes.length; i++) {
            if (Dom.hasClass(aNodes[i], "treeNode")) {
                Event.addListener(aNodes[i], 'contextmenu', handleRightClick, false);
                Event.addListener(aNodes[i], 'click', selectNode, true);
            }
        }
    });
}
var leftClickEvent;
function handleRightClick(env) { 
    var targetObj = Event.getTarget(env, true);
    if (oMenu != null) {
        oMenu.destroy();
    }
    Event.removeListener(targetObj, 'click'); 
    oMenu = new YAHOO.widget.Menu("basicmenu", { context:[targetObj, "tl", "br"]  });
    oMenu.clearContent();

    if (Dom.hasClass(targetObj,"treeNode")) {    //main conf node
        var oMenuItems = [
            //{ text: "Add Topic", onclick:{fn:deleteItem,obj:targetObj} },
            { text: "Subscribe to Topic", onclick:{fn:subscribeItem,obj:targetObj} }

        ];
    }
    oMenu.addItems(oMenuItems);
    oMenu.render(targetObj);
    oMenu.show();
    var listeners = YAHOO.util.Event.getListeners(targetObj);
    for (var i=0; i<listeners.length; ++i) {

    }
    leftClickEvent = env;
    YAHOO.util.Event.stopPropagation(env);   //stop the event propagation (to stop the left click)
    YAHOO.util.Event.stopEvent(env);   //stop the event propagation (to stop the left click)
}


function subscribeItem(x, y, obj) {
    var path = obj.getAttribute("title");
    jQuery.ajax({
        type:"GET",
        url:'subscribe_ajaxprocessor.jsp',
        data: "topicName="+path,
        dataType: "html",
        success:
                function(data, status)
                {
                    jQuery('#topicData').html(data);
                    Event.addListener(obj, 'click', selectNode, true);

                }
    });
}
