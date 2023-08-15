this.name      = "galNavyTarget-Sortie";
this.author    = "Nemoricus";
this.copyright = "� 2009";
this.description = "Ship script for Galactic Navy reserve mission targets.";
this.version   = "Galactic Navy Build 5.3.0";

this.shipDied = function(whom, why)
{
	if(this.ship.isThargoid) this.ship.commsMessage(expandDescription("[thargoid_curses]"));

	if(system.ID == missionVariables.reserve_planetnum)
	{
		//Decreases the total of enemy ships around.
		--worldScripts.GalNavy.reserve_enemy_ships;

		//Declares the battle over if a certain number of ships have been destroyed.
		switch(missionVariables.reserve_duty)
		{
			case "FLEET_INTERCEPT": //Fall through.
			case "SYSTEM_INVASION":
			if(missionVariables.reserve_offer == "OFFER_COMPLETED" || missionVariables.reserve_offer == "OFFER_ACCEPTED")
			{
				if(worldScripts.GalNavy.reserve_enemy_ships < 8)
				{
					this.noteMissionEnded();
				}
			}
			break;
			case "POLICE_ACTION":
			if(missionVariables.reserve_offer == "OFFER_COMPLETED" || missionVariables.reserve_offer == "OFFER_ACCEPTED")
			{
				if(worldScripts.GalNavy.reserve_enemy_ships < 5 && system.countShipsWithRole("GN_sortie_target") == 0)
				{
					this.noteMissionEnded();
				}
			}
			break;
		}
        
		if(this.checkRole(whom))
		{
			switch(missionVariables.reserve_offer)
			{
				case "OFFER_ACCEPTED": //Fall through.
				case "OBJECTIVES_COMPLETE":

				//Increments kill counter.
				missionVariables.reserve_targets += parseInt(this.ship.scriptInfo.score); // value read from shipData.plist

				//Thanking the player for their help and noting their participation in the sortie as appropriate.
				//Performs mission clean up as well.
				if(missionVariables.reserve_targets >= 3)
				{
					this.noteObjectivesComplete();
				}

				//Fall through.
				
				case "OFFER_COMPLETED": //Fall through.
				case "MISSION_ACCOMPLISHED":
				default:
					player.consoleMessage(expandDescription("[navy-thanks-for-assistance]"));
				break;
			}
		}
	}

	else if(this.checkRole(whom))
	{
		player.consoleMessage(expandDescription("[navy-thanks-for-assistance]"));
	}

	delete this.shipDied; 
}

this.checkRole = function(whom)
{
	if(!whom) return false;
    switch(true)
	{
		case (whom.isPlayer):
		case (whom.owner && whom.owner.isPlayer):  // check if some below are included in this.
		//Fall through the acceptable roles.
		case (whom.hasRole("aquatics_guardianPlayer")):	//Aquatics guardian system.
		case (whom.hasRole("drones_antithargoid_drone")):	//Anti-Thargoid Drone from Drones.
		case (whom.hasRole("drones_combat_drone")):	//Combat Drone from Drones.
		case (whom.hasRole("drones_kamikaze_drone")):	//Kamikaze Drone from Dronaes.
		case (whom.hasRole("CT_thargon")):			//Captured Thargon
		case (whom.hasRole("hiredGuns_escort")):		//Escorts from Hired Guns.
		case (whom.hasRole("OSEhiredGuns_escort")):		//Escorts from OSE.
		return true;
		break;
		
		default:
		return false;
		break;
	}
	
	return false;
}

// Called when the player has made a minimum number of kills.
this.noteObjectivesComplete = function()
{
    player.consoleMessage("Gal Navy: The Navy thanks you for your help, Commander " + player.name + ".", 12);
    player.consoleMessage("Gal Navy: Your participation in this sortie has been noted.", 12);
    missionVariables.player_sortie += 1;
    if(missionVariables.reserve_offer == "OBJECTIVES_COMPLETE")
        missionVariables.reserve_offer = "MISSION_ACCOMPLISHED";
    else missionVariables.reserve_offer = "OFFER_COMPLETED";
}

// Called when the target count drops below a threshold value
this.noteMissionEnded = function()
{
    player.consoleMessage("Gal Navy: The battle at " + system.name + " is over.", 12);
    player.consoleMessage("Gal Navy: Reserve pilots may now leave the system.", 12);
    if(missionVariables.reserve_offer == "OFFER_COMPLETED")
        missionVariables.reserve_offer = "MISSION_ACCOMPLISHED";
    else missionVariables.reserve_offer = "OBJECTIVES_COMPLETE";
    mission.setInstructionsKey(null, "GalNavy");
    mission.unmarkSystem(missionVariables.reserve_planetnum);
}