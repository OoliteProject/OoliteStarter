this.name      = "navyMineSweeper"; 
this.author    = "eric walch"; 
this.copyright = "";
this.description = "ship script for Galactic Navy";
this.version   = "1.0";
this.licence   = "CC-by-NC-SA";


/*
Escorts added with scanClass CLASS_POLICE always start with a wingman role. However, with scanning for a formationleader they only accept a ship with role police as a mother. When not available, a wingman becomes role police. All next wingmen will now see a ship with role police and will follow him.  By this mechanisme the Behemoth itself looses all its escorts (wingmen). 
Therefor explicit set the role of these behemoth escorts as escort on spawning. spawnedAsEscort() trigers before shipSpawned().
"TARGET_LOST" is removed from its custom AI.
*/
this.spawnedAsEscort = function()
{
    this.ship.switchAI("navyBattlegroupMineSweeperAI.plist");
    this.ship.primaryRole = "escort"; // no need to change role from Oolite 1.75 onwards
    this.ship.AIState ="FLYING_ESCORT";
}

this.findCascadeWeapons = function()
{
	function isCascadeWeapon(entity) 
	{ return entity.isShip && entity.isWeapon && (entity.primaryRole == "EQ_QC_MINE" || entity.primaryRole == "EQ_CASCADE_MISSILE")};
	var weapon = system.filteredEntities(this, isCascadeWeapon, this.ship, this.ship.scannerRange)[0];
	if(weapon)
	{
		this.ship.target = weapon;
		this.ship.reactToAIMessage("CASCADE_WEAPON_FOUND");
	}
}
