function treeColapse(icon) {
    var parentNode = icon.parentNode;
    var allChildren = parentNode.childNodes;
    var todoOther = "";
    var attributes = "";
    //Do minimizing for the rest of the nodes
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].nodeName == "UL") {

            if (allChildren[i].style.display == "none") {
                attributes = {
                    opacity: { to: 1 }
                };
                var anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.animate();
                allChildren[i].style.display = "";
                if (YAHOO.util.Dom.hasClass(icon, "plus") || YAHOO.util.Dom.hasClass(icon, "minus")) {
                    YAHOO.util.Dom.removeClass(icon, "plus");
                    YAHOO.util.Dom.addClass(icon, "minus");
                }
                todoOther = "show";
                parentNode.style.height = "auto";
            }
            else {
                attributes = {
                    opacity: { to: 0 }
                };
                anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.duration = 0.3;
                anim.onComplete.subscribe(hideTreeItem, allChildren[i]);

                anim.animate();
                if (YAHOO.util.Dom.hasClass(icon, "plus") || YAHOO.util.Dom.hasClass(icon, "minus")) {
                    YAHOO.util.Dom.removeClass(icon, "minus");
                    YAHOO.util.Dom.addClass(icon, "plus");
                }
                todoOther = "hide";
                //parentNode.style.height = "50px";
            }
        }
    }
}
function hideTreeItem(state,opts,item){
   item.style.display = "none"; 
}