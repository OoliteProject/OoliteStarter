// this is a world event handler
// see https://wiki.alioth.net/index.php/Oolite_JavaScript_Reference:_World_script_event_handlers

"use strict";

this.name		= "oolite-starter-oxp";
this.author		= "Hiran";
this.license		= "GPL3";
this.description	= "Partner of the Oolite Starter";
this.version		= "0.1";

this.pushdata = false;

// brought in via https://github.com/maikschulz/oolite-mqtt-bridge/blob/master/oolite-mqtt-bridge.oxp/Config/script.js
this.startUp = function()
{
    var callbackCounter = 0.0;
    var prevMsg = {
        'speed': player.ship.speed,
        'maxSpeed': player.ship.maxSpeed,
    };

    this.$fcb = addFrameCallback(function (delta)
    {
        if (pushdata!=true)
            return;
        if (!debugConsole)
            return;

        // do not update more than 10 times per second
        callbackCounter += delta;
        if (callbackCounter < 0.1)
            return;
        callbackCounter = 0.0;

        var msg = {
            'msgType': "controls",
            'speed': player.ship.speed,
            'maxSpeed': player.ship.maxSpeed
        }
        if (msg.speed !== prevMsg.speed || msg.maxSpeed !== prevMsg.maxSpeed) {
            debugConsole.consoleMessage(JSON.stringify(msg));
            prevMsg = msg;
        }

        //debugConsole.consoleMessage(JSON.stringify(msg));
    });
    commsMessageReceived("", {'displayName': this.name });
}

this.alertConditionChanged = function(newCondition, oldCondition)
{
    if (this.pushdata!=true)
        return;

    var msg = {
        'msgType': "alert",
        'alertCondition': newCondition
    }
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.commsMessageReceived = function(message, sender)
{
    if (this.pushdata!=true)
        return;

    var msg = {
        'msgType': "comms",
        'message': message,
        //'sender': sender.displayName
        'sender': sender
    }
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.playerWillSaveGame = function(reason)
{
    log(this.name, "playerWillSaveGame(" + reason + ") -> storing resourcePaths");

    missionVariables["ooliteStarter_oxpList"] = oolite.resourcePaths;
}
