this.name      = "galNavyPicketFrigate"; 
this.author    = "eric walch"; 
this.copyright = "� 2010";
this.description = "ship script for Galactic Navy";
this.version   = "1.00";
this.licence		= "CC-by-NC-SA";

this.shipSpawned = function()
{
    // build group to handle groupAttackTarget commands
    var groupMembers = system.shipsWithPrimaryRole("picket-viper", this.ship, 25E3);
    var group = new ShipGroup("Picket Group");
    group.addShip(this.ship);
    this.ship.group = group;
    for (var i=0; i < groupMembers.length; i++)
    {
        group.addShip(groupMembers[i]);
        groupMembers[i].group = group;
    }
};