// Calendar handlers
var expirationTimeCalendar;
YAHOO.util.Event.onDOMReady(function() {
    expirationTimeCalendar = new YAHOO.widget.Calendar("cal1", "cal1Container", { title:"Choose a date:", close:true });
    expirationTimeCalendar.render();
    expirationTimeCalendar.selectEvent.subscribe(calSelectHandler, expirationTimeCalendar, true)
});

function calSelectHandler(type, date, obj) {
    var selDate = date[0][0][0] + "/" + date[0][0][1] + "/" + date[0][0][2];
    var activeTime = document.getElementById("expirationTime");
    clearTextIn(activeTime);
    activeTime.value = selDate;
    expirationTimeCalendar.hide();
}

// Shows the calender pop up. Used when adding a subscription.
function showCalendar() {
    expirationTimeCalendar.show();
}

//// Watermark for a text box
// Clears text in a text box
var textValue = "";
function clearTextIn(obj) {
    if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
        YAHOO.util.Dom.removeClass(obj, 'initE');
        YAHOO.util.Dom.addClass(obj, 'normalE');
        textValue = obj.value;
        obj.value = "";
    }
}

// Adds texts in a text box
function fillTextIn(obj) {
    if (obj.value == "") {
        obj.value = textValue;
        if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
            YAHOO.util.Dom.removeClass(obj, 'normalE');
            YAHOO.util.Dom.addClass(obj, 'initE');
        }
    }
}

// Collapsing a tree
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

// Deleting a topic
function deleteTopic(topic) {
    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if (o.responseText.indexOf("Error") > -1) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                        location.href = "../topics/topics.jsp"
                    });
                } else {
                    CARBON.showInfoDialog("" + o.responseText, function() {
                        location.href = "../topics/topics.jsp"
                    });
                }

            }
        },
        failure:function(o) {
            topic
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "delete_topic_ajaxprocessor.jsp", callback, "topic=" + topic);

}

// Shows the topic details page
function showManageTopicWindow(topicPath) {
    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                location.href = "../topics/topic_manage.jsp";
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "load_topic_details_from_bEnd_ajaxprocessor.jsp", callback, "topicPath=" + topicPath + "&type=input");
}

// Add topic page
function showAddTopicWindow(topicPath) {
    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                location.href = "../topics/add_subtopic.jsp?topicPath=" + topicPath;
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "access_topic_roles_ajaxprocessor.jsp", callback, "topicPath=" + topicPath + "&type=input");
}

// Hides a tree node item
function hideTreeItem(state, opts, item) {
    item.style.display = "none";
}

// Adding a topic. Backend handler
function addTopicToBackEnd(topic) {
    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {

                if (o.responseText.search("Error:") > 0) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                    });
                }
                else {
                    addPermissions();
//                    updatePermissions();
                }
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }

    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "add_topic_to_backend_ajaxprocessor.jsp", callback, "topic=" + topic + "&type=input");
}

// Adding a topic. Invoked by jsp.
function addTopic() {
    var topic = document.getElementById("topic");

    var error = "";
    topic.value = topic.value.replace(/^\s+|\s+$/g, "");
    if (topic.value == "") {
        error = "Topic name cannot be empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    addTopicToBackEnd(topic.value)

}

// Adds topic from topic detail/manage page
function addTopicFromManage() {
    var existingTopic = document.getElementById("existingTopic");
    var topic = document.getElementById("topic");
    var completeTopic = "";
    if (existingTopic.value == "/") {
        completeTopic = existingTopic.value + topic.value;
    } else {
        completeTopic = existingTopic.value + "/" + topic.value;
    }
    var error = "";

    if (topic.value == "") {
        error = "Topic name cannot be empty.\n";
    }
    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    addTopicToBackEnd(completeTopic)
}

// Shows add subtopic
function showAddSubTopic() {

    var addSubTopicTable = document.getElementById("AddSubTopic");
    if (addSubTopicTable.style.display == "none") {
        addSubTopicTable.style.display = "";
    } else {
        addSubTopicTable.style.display = "none";
    }
}

// Shows add subscription
function showAddSubscription() {
    var addPropertyTable = document.getElementById("userAdd");
    if (addPropertyTable.style.display == "none") {
        addPropertyTable.style.display = "";
    } else {
        addPropertyTable.style.display = "none";
    }
}

// UnSubscribing to a topic
function unsubscribe(subscriptionId, topic) {
    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if (o.responseText.indexOf("Error") != -1) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                       location.href = "../topics/topic_manage.jsp"
                    });
                } else {
                    CARBON.showInfoDialog("" + o.responseText, function() {
                        location.href = "../topics/topic_manage.jsp"
                    });
                }


            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "unsubscribe_from_topic_ajaxprocessor.jsp", callback, "topic=" + topic + "&subscriptionId=" + subscriptionId);

}

// Adds permissions to a topic when a topic is created
function addPermissions() {
    var permissionTable = document.getElementById("permissionsTable");
    var rowCount = permissionTable.rows.length;
    var parameters = "";
    for (var i = 1; i < rowCount; i++) {
        var roleName = permissionTable.rows[i].cells[0].innerHTML.replace(/^\s+|\s+$/g, "");
        var subscribeAllowed = permissionTable.rows[i].cells[1].getElementsByTagName("input")[0].checked;
        var publishAllowed = permissionTable.rows[i].cells[2].getElementsByTagName("input")[0].checked;
        if (i == 1) {
            parameters = roleName + "," + subscribeAllowed + "," + publishAllowed + ",";
        } else {
            parameters = parameters + roleName + "," + subscribeAllowed + "," + publishAllowed + ",";
        }
    }

    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if (o.responseText.indexOf("Error") > -1) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                        location.href = "../topics/topics.jsp"
                    });
                } else {
                    var message = "Topic added successfully";
                    CARBON.showInfoDialog("" + message, function() {
                        location.href = "../topics/topics.jsp"
                    });
                }
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "update_role_permissions_ajaxprocessor.jsp", callback, "permissions=" + parameters + "&type=input");
}

// Updates permissions of a topic
function updatePermissions() {
    var permissionTable = document.getElementById("permissionsTable");
    var rowCount = permissionTable.rows.length;
    var parameters = "";
    for (var i = 1; i < rowCount; i++) {
        var roleName = permissionTable.rows[i].cells[0].innerHTML.replace(/^\s+|\s+$/g, "");
        var subscribeAllowed = permissionTable.rows[i].cells[1].getElementsByTagName("input")[0].checked;
        var publishAllowed = permissionTable.rows[i].cells[2].getElementsByTagName("input")[0].checked;
        if (i == 1) {
            parameters = roleName + "," + subscribeAllowed + "," + publishAllowed + ",";
        } else {
            parameters = parameters + roleName + "," + subscribeAllowed + "," + publishAllowed + ",";
        }
    }

    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if (o.responseText.indexOf("Error") > -1) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                        location.href = "../topics/topic_manage.jsp"
                    });
                } else {
                    var message = "Updated permissions successfully";
                    CARBON.showInfoDialog("" + message, function() {
                        location.href = "../topics/topic_manage.jsp"
                    });
                }

            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "update_role_permissions_ajaxprocessor.jsp", callback, "permissions=" + parameters + "&type=input");
}

// Validates topic message publishing
function validateInvoking() {
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
    /* $.post('try_it_out_invoke_ajaxprocessor.jsp', {topic : topic,xmlMessage:message}, function (data) {
     CARBON.showConfirmationDialog(data);
     });*/
    return true;
}

// Publishes message of a topic
function invokeService() {
    if (validateInvoking()) {
        var topic = document.getElementById('topic').value;
        var message = document.getElementById('xmlMessage').value;

        var callback =
        {
            success:function(o) {
                if (o.responseText.search("Error:") > 0) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                    });
                }

                else {
                    CARBON.showInfoDialog("" + o.responseText, function() {
                        location.href = "../topics/topic_manage.jsp";
                    });
                }
            },
            failure:function(o) {
                if (o.responseText !== undefined) {
                    alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
                }
            }
        };
        var request = YAHOO.util.Connect.asyncRequest('POST', "try_it_out_invoke_ajaxprocessor.jsp", callback, "topic=" + topic + "&xmlMessage=" + message);
    }
}

function performSubscribe() {
    var topic = document.getElementById('topicId').value;
    var subURL = document.getElementById('subURL').value;
    var expiryDate = document.getElementById('expirationTime').value;
    var hours = document.getElementById('hhid').value;
    var minutes = document.getElementById('mmid').value;
    var seconds = document.getElementById('ssid').value;
    var subscriptionMode = document.getElementById("subscriptionModes")[document.getElementById("subscriptionModes").selectedIndex];


    var parameters = "topic=" + topic + "&subURL=" + subURL + "&expirationTime=" + expiryDate + "&hours=" + hours
                             + "&minutes=" + minutes + "&seconds=" + seconds + "&subMode=" + subscriptionMode.value;

    var callback =
    {
        success:function(o) {
            if (o.responseText !== undefined) {
                if (o.responseText.search("Error:") > 0) {
                    CARBON.showErrorDialog("" + o.responseText, function() {
                    });
                } else {
                    CARBON.showInfoDialog("" + o.responseText, function() {

                        location.href = "../topics/topics.jsp"
                    });
                }
            }
        },
        failure:function(o) {
            if (o.responseText !== undefined) {
                alert("Error " + o.status + "\n Following is the message from the server.\n" + o.responseText);
            }
        }
    };
    var request = YAHOO.util.Connect.asyncRequest('POST', "add_subscription_ajaxprocessor.jsp", callback, parameters);

}
