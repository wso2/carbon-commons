var textValue = "";
function clearTextIn(obj){
		  if(YAHOO.util.Dom.hasClass(obj,'initE')){
		      YAHOO.util.Dom.removeClass(obj,'initE');
		      YAHOO.util.Dom.addClass(obj,'normalE');
              textValue = obj.value;
		      obj.value = "";
		  }
	  }
function fillTextIn(obj){
    if(obj.value ==""){
        obj.value = textValue;
        if(YAHOO.util.Dom.hasClass(obj,'normalE')){
		      YAHOO.util.Dom.removeClass(obj,'normalE');
		      YAHOO.util.Dom.addClass(obj,'initE');
		  }
    }
}