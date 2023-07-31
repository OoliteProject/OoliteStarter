this.name      = "hofd-nellie-pod"; 
this.author    = "eric walch"; 
this.copyright = "";
this.description = "ship script for Galactic Navy";
this.version   = "1.0";
this.licence   = "CC-by-NC-SA";

this.shipWasScooped = function(scooper)
{
     if (scooper.isPlayer)
     {
        player.consoleMessage("You have captured Admiral Kurtz!");
        missionVariables.hofd = "KURTZ_CAPTURED";
        player.bounty = 0;
     }
     mission.unmarkSystem(214);
     delete this.shipWasScooped;
     delete this.shipDied;
}

this.shipDied = function()
{
    player.consoleMessage("Admiral Kurtz has been killed!");
    missionVariables.hofd = "KURTZ_KILLED";
    mission.unmarkSystem(214);
}