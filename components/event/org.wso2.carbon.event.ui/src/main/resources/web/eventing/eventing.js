var Dom = YAHOO.util.Dom;
var Event = YAHOO.util.Event;

function show_prompt(form)
{
    CARBON.showInputDialog(
            "Renew Time Period (Hours)",
            function(invalue){   //handle yes
                //alert(invalue);
                form.renewDuration.value=invalue;
	            form.isRenew.value="true";
	            form.submit();
            },
            function(){  //handle no
            }
     );
/*var name=prompt("Renew Time Period (Hours)","12");
if (name!=null && name!="")
  {
	form.renewDuration.value=name;
	form.isRenew.value="true";
	form.submit();
  }*/
}
function doUnsubscribe(subId){
    var theform = document.getElementById(subId);
    theform.submit();
}
function changeEventSinkCode(form){
	// alert(form);
	if(form.eventSinkType.value=="eventSink"){
		hideDiv('scriptInput'); 
		showDiv('eprInput');
         
	}else{
		hideDiv('eprInput'); 
		showDiv('scriptInput');
        YAHOO.util.Event.onAvailable("JScript", function() {
                editAreaLoader.init({
                    id : "JScript"        // textarea id
                    ,syntax: "js"            // syntax to be uses for highgliting
                    ,start_highlight: true        // to display with highlight mode on start-up
                    ,allow_resize: "both"
                    ,min_height:250
                });
            });
	}
}

function hideDiv( whichLayer )
{
  var elem, vis;
  if( document.getElementById ) // this is the way the standards work
    elem = document.getElementById( whichLayer );
  else if( document.all ) // this is the way old msie versions work
      elem = document.all[whichLayer];
  else if( document.layers ) // this is the way nn4 works
    elem = document.layers[whichLayer];
  vis = elem.style;
  vis.display = 'none';
}

function showDiv( whichLayer )
{
  var elem, vis;
  if( document.getElementById ) // this is the way the standards work
    elem = document.getElementById( whichLayer );
  else if( document.all ) // this is the way old msie versions work
      elem = document.all[whichLayer];
  else if( document.layers ) // this is the way nn4 works
    elem = document.layers[whichLayer];
  vis = elem.style;
  vis.display = 'block';
}



function toggleLayer( whichLayer )
{
  var elem, vis;
  if( document.getElementById ) // this is the way the standards work
    elem = document.getElementById( whichLayer );
  else if( document.all ) // this is the way old msie versions work
      elem = document.all[whichLayer];
  else if( document.layers ) // this is the way nn4 works
    elem = document.layers[whichLayer];
  vis = elem.style;
  // if the style.display value is blank we try to figure it out here
  if(vis.display==''&&elem.offsetWidth!=undefined&&elem.offsetHeight!=undefined)
    vis.display = (elem.offsetWidth!=0&&elem.offsetHeight!=0)?'block':'none';
  vis.display = (vis.display==''||vis.display=='block')?'none':'block';
}


function hidediv() { 
	if (document.getElementById) { // DOM3 = IE5, NS6
	document.getElementById('hideshow').style.display="hidden"; 
	} 
	} 

	function showdiv() { 
	if (document.getElementById) { // DOM3 = IE5, NS6
	document.getElementById('hideshow').style="display: block"; 
	} 
	} 
	
function performSubscribe(form){
	form.submit();
}
function performRenew(form){
    form.submit();
}



function moveLeftUser(){
    moveOp('U');
}

function moveLeftRoles(){
    moveOp('R');
}
function moveOp(action){
    var s1 = document.getElementById("s1");
    var s2 = document.getElementById("s2");
    var s3 = document.getElementById("s3");

    if(s2.selectedIndex >= 0){
            var opt1 = s2.options[s2.selectedIndex];
            s1.appendChild(new Option(opt1.text + "("+action+")", "a", false, true));
            s2.remove(opt1.index);
            document.AddTopicForm.actorsToAdd.value = document.AddTopicForm.actorsToAdd.value + "#" + opt1.text + "(R)";
            //alert(document.AddTopicForm.actorsToAdd.value);
    }
    if(s3.selectedIndex >= 0){
            var opt1 = s3.options[s3.selectedIndex];
            s1.appendChild(new Option(opt1.text + "("+action+")", "a", false, true));
            s3.remove(opt1.index);
            document.AddTopicForm.actorsToAdd.value = document.AddTopicForm.actorsToAdd.value + "#" + opt1.text + "(U)"; 
            //alert(document.AddTopicForm.actorsToAdd.value);
    }
}

function moveRight(){
    var s1 = document.getElementById("s1");
    var s2 = document.getElementById("s2");
    var s3 = document.getElementById("s3");

    if(s1.selectedIndex >= 0){
            var opt1 = s1.options[s1.selectedIndex];
            s1.remove(opt1.index);

            var optName = opt1.text;
            var optNameOriginal = opt1.text;
            
            if(optName.indexOf("(R)",0) >= 0){
                    optName = optName.replace(/\(.*\)/,"");
                    s2.appendChild(new Option(optName, "a", false, true));
            }else{
                    optName = optName.replace(/\(.*\)/,"");
                    s3.appendChild(new Option(optName, "a", false, true));
            }
            document.AddTopicForm.actorsToRemove.value = document.AddTopicForm.actorsToRemove.value + "#" + optNameOriginal;
    }       
}

function onTopicDataSubmit() {
	var actorsToAdd = document.getElementById("actorsToAdd");
	var actorsToRemove = document.getElementById("actorsToRemove");
	var topicName = document.getElementById("topicName");
	
	var isSecureTopic = document.getElementById("isSecureTopic");
	var description = document.getElementById("description");
	var schemadescription = document.getElementById("schemadescription");
	
	//alert(actorsToAdd.value);
	jQuery.post("securetopic_ajaxprocessor.jsp", {"action":"Edit",
			"topicName": topicName.value, "actorsToAdd":actorsToAdd.value, "actorsToRemove":actorsToRemove.value,
			"isSecureTopic":isSecureTopic.value, "description":description.value, "schemadescription":schemadescription.value},
			function(data) {
        		jQuery('#topicData').html(data);
                CARBON.showInfoDialog("Topic data updated  successfully!!");
    		});
}



function cleanSelected(){
   var allNodes = Dom.get("topicTree").getElementsByTagName("a");
    for(var i=0;i<allNodes.length; i++){
        if(YAHOO.util.Dom.hasClass(allNodes[i],"selected")){
            YAHOO.util.Dom.removeClass(allNodes[i],"selected");
        }
    }
}
function selectNode(env){
    var node = YAHOO.util.Event.getTarget(env, true);
    if(node == null){
        return;
    }
    var path = node.title;
    if(typeof(node)!="object"){
        node = Dom.get(node);
    }
    //unselect all
    cleanSelected();
    YAHOO.util.Dom.addClass(node,"selected");
    
    //Do the ajax call
    loadTopicData(node.innerHTML,path);
}


function showMessage(msgboxId, msgId){
	jQuery.post("getmessage_ajaxprocessor.jsp", {"messageboxId": msgboxId, "messageId":msgId},
            function(data) {
				data = jQuery.trim(data);
				alert(data);
				editAreaLoader.setValue("messageArea", data);	
                //Dom.get('messageArea').value = data;
            });
}

                                                   

function perCheckBoxHandler(myID, peerID) {
 //Only one is checked always, so it is safe to make the peer false
 if (myID != peerID) {
 var peer = document.getElementById(peerID);
 peer.checked = false;
 }
}
function addRolePermission(){
    var AddTopicForm = document.getElementById("AddTopicForm");
    var roleToAuthorize = AddTopicForm.roleToAuthorize;
    var actionToAuthorize = AddTopicForm.actionToAuthorize;
    var permissionType = AddTopicForm.permissionType;
    var selectedPermissionType = "allow";
    if(permissionType[1].checked){
        selectedPermissionType = "deny";
    }

    if(actionToAuthorize[actionToAuthorize.selectedIndex].value == "select"){
        CARBON.showErrorDialog("Please select an Action for Permission");
        return;
    }
    // Setting the role permission string
    var permissionStr = roleToAuthorize[roleToAuthorize.selectedIndex].value + "(" + actionToAuthorize[actionToAuthorize.selectedIndex].value + ")";

    jQuery.post("rolepermission_ajaxprocessor.jsp", {"permissionStr": permissionStr
        ,"permissionType":selectedPermissionType},
            function(data) {

            });
}
