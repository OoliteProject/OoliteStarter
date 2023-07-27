this.name      = "navySeccomStation"; 
this.author    = "eric walch"; 
this.copyright = "� 2008 eric walch."; 
this.version   = "1.00";
this.licence		= "CC-by-NC-SA";

this.shipSpawned = function()
{
    var targetVector = system.mainPlanet.position.subtract(this.ship.position).direction();
    var angle = this.ship.heading.angleTo(targetVector);
    var cross = this.ship.heading.cross(targetVector);
    // align the heading to the targetVector 
	this.ship.orientation = this.ship.orientation.rotate(cross, -angle);
    system.addShips("navySeccomBuoy", 1, this.ship.position.add(this.ship.heading.multiply(10E3)), 1);
    delete this.shipSpawned;
}

this.stationLaunchedShip = function(ship)
{
    if (ship.isPlayer) 
    {
         //This changes the player's status with regard to the SecCom station to say that they're revisiting it.
         //missionVariables.seccom_station has uses in the docking code.
         missionVariables.seccom_station = "SECCOM_REVISIT";
         return;
    }
    if (ship.hasRole("galNavy-shuttle")) ship.primaryRole = "shuttle"; // only a shuttle can always land without crashing.
}
