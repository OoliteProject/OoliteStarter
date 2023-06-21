// this is a world event handler
// see https://wiki.alioth.net/index.php/Oolite_JavaScript_Reference:_World_script_event_handlers

"use strict";

this.name		= "oolite-starter-oxp";
this.author		= "Hiran Chaudhuri";
this.license		= "GPL3";
this.description	= "Partner of the Oolite Starter";
this.version		= "0.1";

this.playerWillSaveGame = function()
{
    log(this.name, "playerWillSaveGame() -> storing resourcePaths");

    missionVariables["ooliteStarter_oxpList"] = oolite.resourcePaths;
}

this.playerWillSaveGame = function(reason)
{
    log(this.name, "playerWillSaveGame(" + reason + ") -> storing resourcePaths");

    missionVariables["ooliteStarter_oxpList"] = oolite.resourcePaths;
}
