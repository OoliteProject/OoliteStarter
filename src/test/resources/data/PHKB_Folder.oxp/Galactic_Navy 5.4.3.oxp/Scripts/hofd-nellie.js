this.name      = "hofd-nellie"; 
this.author    = "eric walch"; 
this.copyright = "";
this.description = "ship script for Galactic Navy";
this.version   = "1.0";
this.licence   = "CC-by-NC-SA";

this.shipSpawned = function ()
{
    if(missionVariables.hofd == "HOFD_IN_ROUTE") missionVariables.hofd = "HOFD_ARRIVED";
    
    // build group to handle groupAttackTarget commands
    var groupMembers = system.shipsWithRole("nelly_crew", this.ship, 10E3);
    var group = this.ship.group;
    if (!group)
    {
        // make sure the ship has a group.
        group = new ShipGroup();
        group.addShip(this.ship);
        this.ship.group = group;
    }
    group.name= "Nelly Group";
    this.ship.group = group;
    for (var i=0; i < groupMembers.length; i++)
    {
        group.addShip(groupMembers[i]);
        groupMembers[i].group = group;
    }
};

this.shipEnergyIsLow = function ()
{
    this.ship.launchShipWithRole("hofd-shuttle", true);
    delete this.shipEnergyIsLow;
}

this.stationLaunchedShip = function(ship)
{
    if (ship.primaryRole == "hofd-shuttle") 
    {
        missionVariables.hofd = "KURTZ_ESCAPED";
        delete this.shipDied;
    }
    if (ship.isPlayer && missionVariables.hofd == "HOFD_SERVICE") 
    {
        this.ship.commsMessage("Kill the deserter!");
    }
}

this.shipDied = function ()
{
    // Nelly was killed before Cmd Kurtz launched.
    player.consoleMessage("Admiral Kurtz has been killed!");
    missionVariables.hofd = "KURTZ_KILLED";
    mission.unmarkSystem(214);
}