this.name      = "navyBattlegroupEscort"; 
this.author    = "eric walch"; 
this.copyright = "� 2009";
this.description = "ship script for Galactic Navy";
this.version   = "1.00";
this.licence   = "CC-by-NC-SA";

/*
Escorts added with scanClass CLASS_POLICE always start with a wingman role. However, with scanning for a 
formationleader they only accept a ship with role police as a mother. When not available, a wingman 
becomes role police. All next wingmen will now see a ship with role police and will follow him.  
By this mechanisme the Behemoth itself looses all its escorts (wingmen). 
Therefor explicit set the role of these behemoth escorts as escort on spawning. spawnedAsEscort() trigers before shipSpawned().
"TARGET_LOST" is removed from its custom AI.
*/
this.spawnedAsEscort = function()
{
    this.ship.switchAI("navyBattlegroupEscortAI.plist");
    this.ship.primaryRole = "escort";
    this.ship.AIState ="FLYING_ESCORT";
}