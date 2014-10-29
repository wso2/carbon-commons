
function deleteRow(name, msg) {
    CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function() {
        document.location.href = "delete-template.jsp?" + "reportName=" + name;
    });
}
function deleteSelected() {
    var checkBoxes = YAHOO.util.Dom.getElementsByClassName('commonDelete');
    for (var i = 0;i< checkBoxes.length; i++) {
        if (checkBoxes[i].checked) {
            deleteRow(checkBoxes[i].value, 'Do you want to delete ');
        }
    }
}
function saveReport(name) {

    var payloadVar = editAreaLoader.getValue("payload");
    if (payloadVar != "") {
        new Ajax.Request('../reporting_custom/update-template_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {name:name, payload: payloadVar},
            onSuccess: function(transport) {

                var message = "Report Template saved";

                CARBON.showInfoDialog(message, function() {
                    window.location = "list-reports.jsp?region=region5&item=reporting_list";
                });
            },
            onFailure: function(transport) {
                CARBON.showErrorDialog(transport.responseText);
            }
        });

    } else {
        var message = "Empty template can not be save";
        CARBON.showWarningDialog(message);
    }
}
function genForm(reportName, obj, resultDivId) {
    jQuery(".reportParms").hide();

    var resultDiv = document.getElementById(resultDivId);
    jQuery("#" + resultDivId).show();
    if (obj.checked) {
        if (reportName != "") {
            new Ajax.Request('../reporting_custom/generateForms_ajaxprocessor.jsp', {
                method: 'post',
                parameters: {reportName:reportName},
                onSuccess: function(transport) {

                    var message = "Report Template saved";
                    resultDiv.innerHTML = transport.responseText;

                },
                onFailure: function(transport) {
                    CARBON.showErrorDialog(transport.responseText);
                }
            });

        } else {
            var message = "Select report template";
            CARBON.showWarningDialog(message);
        }
    } else {
        resultDiv.innerHTML = "";
    }
    selectedForm = resultDivId;

}

var hasReport = false;

// assigning from genForm()
var selectedForm;

function submitReport() {
//    var count = 0;
//    var checkBoxes = YAHOO.util.Dom.getElementsByClassName('commonDelete');
//    for (var i = 0; i< checkBoxes.length; i++) {
//        if (checkBoxes[i].checked) {
//            count = count + 1;
//        }
//    }
//    if (count == 0) {
//        CARBON.showWarningDialog("Select a report template ");
//        uncheckAll(checkBoxes)
//        return;
//    } else if (count != 0 && count > 1) {
//        CARBON.showWarningDialog("Select one  report template at a time");
//        uncheckAll(checkBoxes)
//        return;
//    }
    var genForm = document.getElementById('selectedForm');
    var inputElements = genForm.getElementsByTagName('input');

    for (var i = 0; i < inputElements.length; i++) {

        if (inputElements[i].value == "") {
            CARBON.showWarningDialog("Fill all required parameters ");
            return;

        }

    }


    var param = document.getElementsByName("input_param");
    var wholeString = "";
    for (var i = 0; i < param.length; i++) {
        wholeString += param[i].id + "=";
        wholeString += param[i].value + "|";
    }

    document.getElementById("hidden_param").value = wholeString;
    document.reportConfig.submit();
}

function showUploadForm() {
    var addNewTemplate = document.getElementById("addNewTemplate");
    if (addNewTemplate.style.display == "none") {
        addNewTemplate.style.display = "";
    } else {
        addNewTemplate.style.display = "none";
    }
}
function uncheckAll(field)
{
for (i = 0; i < field.length; i++)
	field[i].checked = false ;
}

