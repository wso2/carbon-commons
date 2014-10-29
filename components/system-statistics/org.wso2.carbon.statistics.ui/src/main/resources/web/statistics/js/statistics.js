/*
 statistics.js contains scripts pertaining to handle @server_short_name@ statistics data
 */

// Memory
var graphUsedMemory;
var graphTotalMemory;

// Response Times
var graphAvgResponse;

function initStats(responseTimeXScale, memoryXScale) {
    if (memoryXScale != null) {
        initMemoryGraphs(memoryXScale);
    } else {
    }
    if (responseTimeXScale != null) {
        initResponseTimeGraph(responseTimeXScale);
    }
}

function isNumeric(sText){
    var validChars = "0123456789.";
    var isNumber = true;
    var character;
    for (var i = 0; i < sText.length && isNumber == true; i++) {
        character = sText.charAt(i);
        if (validChars.indexOf(character) == -1) {
            isNumber = false;
        }
    }
    return isNumber;
}

function initMemoryGraphs(memoryXScale) {
    if (memoryXScale < 1 || !isNumeric(memoryXScale)) {
        return;
    }
    graphUsedMemory = new carbonGraph(memoryXScale);
    graphTotalMemory = new carbonGraph(memoryXScale);
}

function initResponseTimeGraph(responseTimeXScale) {
    if (responseTimeXScale < 1 || !isNumeric(responseTimeXScale)) {
        return;
    }
    graphAvgResponse = new carbonGraph(responseTimeXScale);
}




