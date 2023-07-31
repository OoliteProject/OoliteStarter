this.name = "GalNavy"; // don't change this name.
this.author = "matt634, Nemoricus";
this.copyright = "(c) matt634";
this.description = "This is the main script for Galactic Navy.";
this.version = "5.4.3";

//Things to do on start up, like initializing variables.
this.startUp = function()
{
     //This code initializes any undefined Galactic Navy variables.
     //This can probably be moved to a different section.

     //This should set the victory bond count to zero if it is
     //not yet defined when the player launches from a station.
     if(missionVariables.vicbond_count == undefined)
	missionVariables.vicbond_count = 0;

     //This sets the status of the reserve enlistment offer.     
     if(missionVariables.reserve_offer == undefined)
      missionVariables.reserve_offer = "OFFER_OPEN";

     //This sets the reserve status of the player.
     if(missionVariables.reserve_status == undefined)
	missionVariables.reserve_status = "STATUS_CIVILIAN";

     //This sets the sorties of the player and top reservists. Variable is incremented in ship scripts.
     if(missionVariables.player_sortie == undefined)
     {
      missionVariables.player_sortie = 0;
      missionVariables.powers_sortie = 31;
      missionVariables.zalack_sortie = 25;
      missionVariables.warrick_sortie = 27;
      missionVariables.xaquetl_sortie = 19;
     }

     //This sets the intial status of scooped navy officers to zero.
     if(missionVariables.navy_officer_count == undefined && 0 < oolite.compareVersion("1.75"))
        missionVariables.navy_officer_count = 0;

	//Initializing kill counter for Galactic Navy reserve duty.
	if(missionVariables.reserve_targets == undefined)
		missionVariables.reserve_targets = 0;

	if(this.reserve_enemy_ships == undefined)
		this.reserve_enemy_ships = 0;
	
	if(missionVariables.seccom_station == undefined)
		missionVariables.seccom_station = "SECCOM_REVISIT";

     //End Galactic Navy Initialisation code.
}

//Stuff to do after launching from a station.
this.shipWillLaunchFromStation = function(station)
{
    //If the player is in the mission system, this adds in the missions ships should they be absent.
	if(missionVariables.reserve_offer == "OFFER_ACCEPTED" && system.ID == missionVariables.reserve_planetnum)
	{
		if(system.countShipsWithPrimaryRole("reserve-pirate") == 0 && system.countShipsWithPrimaryRole("galactic-navy-thargoid") <= 3)
			this.ReserveDutySystemSetUp();
	}
	
	// Add SecCom System ships if this is a SecCom system and that a SecCom station hasn't been spawned yet.
	if(this.SecComSystemCheck() && 
	   !(system.countShipsWithRole("navystat") == 1 || system.countShipsWithRole("navystat25") == 1 || 
	   system.countShipsWithRole("navystat50") == 1 || system.countShipsWithRole("navystat75") == 1))
		this.SecComSystemSetUp();
}

//Some things to do as the player is leaving the mission system.
this.shipWillEnterWitchspace = function()
{
	var reserve_offer = missionVariables.reserve_offer;
    if(reserve_offer == "OFFER_COMPLETED" || reserve_offer == "OBJECTIVES_COMPLETE" || reserve_offer == "MISSION_ACCOMPLISHED")
	{
		this.ReserveDutyCleanup();
	}
}

//This section of code deals with things that need to done before the player arrives in a system.
this.shipWillExitWitchspace = function()
{
     //Checking if the player has landed in interstellar space.
     if(system.isInterstellarSpace)
      {

        //Randomly determining what ships to add to make for some interesting action.
        if(Math.random() < .26)
        {
          system.addShips("navy-frigate", 1);
          system.addShips("galnav-thargoid", 3);
        }

        if(Math.random() < .26)
        {
          system.addShips("navy-frigate", 1);
          system.addShips("galnav-thargoid", 2);
          system.addShips("galnav-thargbatship", 1);
        }

        if(Math.random() < .26)
        {
          system.addShips("navy-frigate", 1);
          system.addShips("galnav-thargbatship", 1);
          system.addShips("galnav-thargcruiser", 2);
        }

        if(Math.random() < .26)
        {
          system.addShips("navy-frigate", 1);
          system.addShips("galnav-thargoid", 1);
          system.addShips("galnav-thargcruiser", 1);
          system.addShips("galnav-thargbatship", 1);
        }

        if(Math.random() < .26)
        {
          system.addShips("navy-frigate", 1);
          system.addShips("galnav-thargbatship", 3);
        }

      }

	//Otherwise, the player is in a normal system.
	//
	//Possible to do: add code to make patrols vary based on Sector.
      else
	{
		// Add SecCom System ships if this is a SecCom system.
		if(this.SecComSystemCheck())
			this.SecComSystemSetUp();

	  //Randomly adding in patrols.
	  if(Math.random() > .90)
	  {
	    system.addShipsToRoute("patrol-frigate", 1, Math.random(), 'wp');
	  }

	  if(Math.random() > .90)
	  {
	    system.addShipsToRoute("navy-medship", 1, Math.random(), 'wp');
	  }

        //Adding a convoy randomly.
	  if(Math.random() > .85) 
	  {
	    system.addGroup("navy-convoy", 5, Vector3D(0, 0, 0.5).fromCoordinateSystem("wpu"), 5000);
	  }

	  //This section adds a carrier battle group. Eric Walch's code made this much better. Full credit to him.
	  if(Math.random() > .95)
	  {
	    system.addShipsToRoute("navy-behemoth-battlegroup", 1, Math.random(), 'wp');
	  }

	  //Hey, those reserve pilots that are also on the leaderboard do missions too, you know.
	  var chance = Math.random();

	  if(chance < .06)
		missionVariables.xaquetl_sortie += 1;

	  else if(chance < .11)
		missionVariables.powers_sortie += 1;

	  else if(chance < .16)
		missionVariables.warrick_sortie += 1;

	  else if(chance < .21)
		missionVariables.zalack_sortie += 1;

	  //Doing set-up and clean-up after entry into the reserve duty system.
	  if(missionVariables.reserve_offer == "OFFER_ACCEPTED")
	  {
		this.reserve_counter_part_two = clock.seconds - missionVariables.reserve_counter;	

		//If the player takes too long to get there.
		if(this.reserve_counter_part_two >= 1209600)
		{
			player.consoleMessage("Gal Navy: The battle at " + missionVariables.reserve_planetname + " is over.");
			player.consoleMessage("Gal Navy: Report to the nearest SecCom station for another assignment.");
			this.ReserveDutyCleanup();
		}
		//If the player is in the target system and the battle is still on.
		else if(system.ID == missionVariables.reserve_planetnum)
		{
			this.ReserveDutySystemSetUp();
		}
	  }
	}
}

this.playerEnteredNewGalaxy = function(galaxyNumber)
{
	if(missionVariables.reserve_status == "STATUS_ENLISTED")
		player.consoleMessage("Gal Navy: Transferring you to Galaxy " + (galaxyNumber+1) + "'s Naval jurisdiction.");

	var offer = missionVariables.reserve_offer; // working with missionVariables is much slower as with JS variables
	
        if(offer == "OFFER_ACCEPTED") // This is really the only case that matters.
	{
		player.consoleMessage("Gal Navy: You have left the galaxy your mission was assigned in.");
		player.consoleMessage("Gal Navy: Please report to the nearest SecCom station for another mission.");
		this.ReserveDutyCleanup();
	}
	
}

//Stuff to do while docked at a station.
this.missionScreenOpportunity = function()
{
	if(player.ship.docked && player.ship.dockedStation.name == "Navy SecCom Station")
	{
        this.SecComBusiness();
	}
}
this.shipWillDockWithStation = function(station)
{
    var offer = missionVariables.reserve_offer;
    if(offer == "OFFER_COMPLETED" || offer == "MISSION_ACCOMPLISHED" || offer == "OBJECTIVES_COMPLETE") this.ReserveDutyCleanup();
}

this.shipDockedWithStation = function(station)
{
	if (0 >= oolite.compareVersion("1.75"))
    {
        // for 1.75 onwards we do pilot awarding with a pilot script.
        delete missionVariables.navy_officer_count; // not needed for 1.75
        delete this.shipDockedWithStation;
        return;
    }
    // cycle throug all rescued officer pilots and add the messages to the report screen.
    // Not needed for Oolite 1.75+
    if (missionVariables.navy_officer_count > 0) this.OfficerRescueReward();
}

//Rewards player for rescue of navy officers.
this.OfficerRescueReward = function()
{
    while (missionVariables.navy_officer_count > 0)
    {
        missionVariables.navy_officer_count--;
        this.unloadOfficer();
    }
}

this.unloadOfficer = function()
{
    var count = Math.ceil(Math.random()*10);
    var payout, message;
    switch(count)
    {
        case 1:
            message = "2lt_insurance_payment";
            payout = 100.0;
            break;
        case 2:
            message = "1lt_insurance_payment";
            payout = 250.0;
            break;
        case 3:
        case 4:
            message = "cpt_insurance_payment";
            payout = 500.0;
            break;
        case 5:
        case 6:
            message = "radm_insurance_payment";
            payout = 750.0;
            break;
        case 7:
        case 8:
            message = "vadm_insurance_payment";
            payout = 1000.0;
            break;
        case 9:
        case 10:
            message = "adm_insurance_payment";
            payout = 2500.0;
            break;
    }
    player.addMessageToArrivalReport(expandMissionText(message));
    player.credits += payout;
}

//This deals with business on SecCom stations.
this.SecComBusiness = function(choice)
{
    switch(missionVariables.seccom_station)
    {
        case "SECCOM_REVISIT": // value is reset by launching from seccom station with station script.
        {
            var seccom_welcome_screen;
            var vicbond_count = Math.floor(missionVariables.vicbond_count / 25);
            switch(vicbond_count)
            {
                //Setting welcome screen based on how much support they gave to the Navy.
                case 0:
                    seccom_welcome_screen = "seccom_welcome1";
                    break;

                case 1:
                    seccom_welcome_screen = "seccom_welcome2";
                    player.ship.fuel = 7.0;
                    break;

                case 2:
                case 3:
                    seccom_welcome_screen = "seccom_welcome3";
                    player.ship.fuel = 7.0;
                    break;

                default :
                    seccom_welcome_screen = "seccom_welcome4";
                    player.ship.fuel = 7.0;
                    break;
            }
            mission.runScreen({title: "Galactic Navy", messageKey: seccom_welcome_screen, choicesKey: "seccom_welcome_choice"}, this.SecComBusiness);
            missionVariables.seccom_station = "SECCOM_VISIT";
            break;
        }
	
	// The numbers in front of the cases is to ensure that they display in a logical order in the mission screen.
        case "SECCOM_VISIT":
        {
            switch(choice)
            {
                //This section handles the buying of victory bonds.
                case "1buy_vicbond":
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "buy_victory_bonds", choicesKey: "vicbond_purchase_choice"}, this.SecComBusiness);
                    break;
                }

                case "1one_vicbond":
                {
                    if(player.credits >= 1000.0)
                    {
			player.credits -= 1000;
                        missionVariables.vicbond_count++;
                        mission.runScreen({title: "Galactic Navy", messageKey: "victory_bond_purchased", overlay: "vicbond.png", choicesKey: "vicbond_purchase_choice2"}, this.SecComBusiness);
                    }

                    else
                    {
                        mission.runScreen({title: "Galactic Navy", messageKey: "no_cash_for_bond", choicesKey: "vicbond_purchase_choice3"}, this.SecComBusiness);
                    }
                    break;
                }

                case "2no_vicbond":
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "vicbond_not_purchased", choicesKey: "vicbond_purchase_choice3"}, this.SecComBusiness);

                    break;
                }
                //End victory bond buying section.

                //This section handles the checking of news.
                case "2check_newsbrief":
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_newsbrief", choicesKey: "seccom_newsbrief_choice"}, this.SecComBusiness);
                    break;
                }
                //End checking of news section.

                //Begin reserve duty section.
                case "3check_reserveduty":
                {
                    if(missionVariables.reserve_offer == "OFFER_OPEN")
                    {
                        switch(missionVariables.reserve_status)
                        {
                            case "STATUS_CIVILIAN":
                            {
                                //Begin checks to determine if the player is ready to sign up.
                                if(player.score < 8) //This guy's just harmless. He should be in a nursery.
                                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_harmless", choicesKey: "seccom_green_choice"}, this.SecComBusiness);

                                else if(player.score < 16) //This guy's mostly harmless. He should stick to the milk runs.
                                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_mostlyharmless", choicesKey: "seccom_green_choice"}, this.SecComBusiness);

                                else if(player.score < 32) //This guy's poor. This is the Navy, not the Trader's Guild.
                                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_poor", choicesKey: "seccom_green_choice"}, this.SecComBusiness);
                        
                                 else if(player.score < 64) //This guy's merely average. Still a ways to go.
                                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_average", choicesKey: "seccom_green_choice"}, this.SecComBusiness);

                                 else if(player.score < 128) //This guy's above average. Not quite yet there.
                                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_aboveaverage", choicesKey: "seccom_green_choice"}, this.SecComBusiness);

                                else                       //This guy's finally competent enough to fly with us.
                                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_reserve_enlist", choicesKey: "reserve_enlist_choice"}, this.SecComBusiness);

                                break;
                            }

                            //Giving the player their mission.
                            case "STATUS_ENLISTED":
                            {
                                missionVariables.reserve_offer = "OFFER_ACCEPTED";
                                missionVariables.reserve_counter = clock.seconds;
                                missionVariables.reserve_planetnum = this.ReserveDutySystemDetermination();
                                missionVariables.reserve_planetname = System.systemNameForID(missionVariables.reserve_planetnum);
                                missionVariables.reserve_duty = this.ReserveDutyDetermination();
                                mission.markSystem(missionVariables.reserve_planetnum);

                                switch(missionVariables.reserve_duty)
                                {
                                    case "SYSTEM_INVASION":
                                    {
                                        mission.runScreen({title: "Galactic Navy", messageKey: "reserve_duty_invasion", choicesKey: "seccom_reserveduty_choice"}, this.SecComBusiness);
                                        mission.setInstructionsKey("system_invasion_desc");
                                        break;
                                    }

                                    case "POLICE_ACTION":
                                    {
                                        mission.runScreen({title: "Galactic Navy", messageKey: "reserve_duty_police", choicesKey: "seccom_reserveduty_choice"}, this.SecComBusiness);
                                        mission.setInstructionsKey("police_action_desc");
                                        break;
                                    }

                                    case "FLEET_INTERCEPT":
                                    {
                                        mission.runScreen({title: "Galactic Navy", messageKey: "reserve_duty_fleet", choicesKey: "seccom_reserveduty_choice"}, this.SecComBusiness);
                                        mission.setInstructionsKey("fleet_intercept_desc");
                                        break;
                                    }
                                }

                                break;
                            }
                        }
                    }
                    
                    //Gives the player an update on the mission.
                    else if(missionVariables.reserve_offer == "OFFER_ACCEPTED")
                    {
                        switch(missionVariables.reserve_duty)
                        {
                            case "SYSTEM_INVASION":
                            {
                                mission.runScreen({title: "Galactic Navy", messageKey: "reserve_duty_invasion_reminder", choicesKey: "seccom_reserveduty_choice"}, this.SecComBusiness);
                                break;
                            }

                            case "POLICE_ACTION":
                            {
                                mission.runScreen({title: "Galactic Navy", messageKey: "reserve_duty_police_reminder", choicesKey: "seccom_reserveduty_choice"}, this.SecComBusiness);
                                break;
                            }

                            case "FLEET_INTERCEPT":
                            {
                                mission.runScreen({title: "Galactic Navy", messageKey: "reserve_duty_intercept_reminder", choicesKey: "seccom_reserveduty_choice"}, this.SecComBusiness);
                                break;
                            }
                            default :
                            {
                                // something went wrong. There was no valid accepted_offer. Show a news brief instead.
                                missionVariables.reserve_offer = "OFFER_OPEN";
                                mission.runScreen({title: "Galactic Navy", messageKey: "seccom_newsbrief", choicesKey: "seccom_newsbrief_choice"}, this.SecComBusiness);
                                break;
                            }
                        }
                    }

                    break;
                }

                case "1reserve_sign_me_up":
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_reserve_intro", choicesKey: "reserve_intro_choice"}, this.SecComBusiness);
                    missionVariables.reserve_status = "STATUS_ENLISTED";
                    break;
                }

                case "2reserve_not_now":
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "seccom_reserve_pass", choicesKey: "seccom_green_choice"}, this.SecComBusiness);
                    break;
                }

                case "3check_leaderboard":
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "squad_leaderboard", choicesKey: "leaderboard_return_choice"}, this.SecComBusiness);
                    break;
                }
                //End reserve duty section.

                case "4back_to_business":
                {
                    missionVariables.seccom_station = "DOING_BUSINESS";
                    break;
                }
            }

            break;				
        }

        case "DOING_BUSINESS":
        {
            var playersortie = missionVariables.player_sortie;
            if(playersortie % 10 == 0) // Every ten reserve sorties.
            {
                var bonus = "reserve_bonus" + playersortie;
                var old_bonus = "reserve_bonus" + (playersortie - 10);
		    
                if(playersortie == 10 && missionVariables[bonus] == undefined) // First bonus.
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "reserve_bonus1"});
                    player.credits += 1000.0;
                    missionVariables[bonus] = "PAID";
                }

                if(playersortie >= 20 && playersortie < 50 && missionVariables[bonus] == undefined) // Bonuses 2, 3, and 4.
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "reserve_bonus5"});
                    player.credits += 5000.0;
                    missionVariables[bonus] = "PAID";
                    delete missionVariables[old_bonus];
                }
                
                if(playersortie >= 50 && playersortie % 100 != 0 && missionVariables[bonus] == undefined) // Bonuses 5 through 9.
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "reserve_bonus10"});
                    player.credits += 10000.0;
                    missionVariables[bonus] = "PAID";
                    delete missionVariables[old_bonus];
                }

                if(playersortie >= 50 && playersortie % 100 == 0 && missionVariables[bonus] == undefined) // Bonuses 10 and onwards.
                {
                    mission.runScreen({title: "Galactic Navy", messageKey: "reserve_bonus25"});
                    player.credits += 25000.0; // every 100 missions an extra bonus
                    missionVariables[bonus] = "PAID";
                    delete missionVariables[old_bonus];
                }
                // Eric: it would have been easier when Matt used just one single mission variable to store the 
                // status and not a new mission variable for each award stage. I now
                // changed the code so that old variables are deleted as they have no longer any use.
                // As bonus will this code now go on and not stop after 100 missions.
                // I can't change the way of storing to stay compatible with old save games.
                
            }
            break;
        }
    }
}

this.ReserveDutySystemDetermination = function()
{
    var thisSystem = system.info;
    
    var targetSystems = SystemInfo.systemsInRange(20); // this only looks at direct distances, not by routes.
    
    if (!targetSystems) return system.ID; // choose current system, should never happen.
    
    var count = targetSystems.length;
    var targetSystem = targetSystems[Math.floor(Math.random()*count)];
    var realDistance = System.infoForSystem(galaxyNumber, targetSystem.systemID).routeToSystem(thisSystem).distance;
    var i = 0;
    while ((!realDistance || realDistance > 20) && i<count)
    {
        // Make sure there is a short route to the system. If not, the system must be in another sector.
        // distance == null for systems in isolated areas.
        targetSystem = targetSystems[i++];
        realDistance = System.infoForSystem(galaxyNumber, targetSystem.systemID).routeToSystem(thisSystem).distance;
    }
    return targetSystem.systemID;
}


//This sets up the mission systems when the player enters them.
this.ReserveDutySystemSetUp = function()
{
	switch(missionVariables.reserve_duty)
	{
		case "SYSTEM_INVASION":
		{
			system.addShips("navy-behemoth", 1, Vector3D(0, 0, 0.5).fromCoordinateSystem("wpu"), 1);
			system.addShips("navy-frigate", 1, Vector3D(0, 0, 0.51).fromCoordinateSystem("wpu"), 1);
			system.addShips("navy-frigate", 1, Vector3D(0, 0, 0.49).fromCoordinateSystem("wpu"), 1);
			system.addShips("reserve-leader", 1, Vector3D(0, 0, 0.02).fromCoordinateSystem("wpu"), 1);
			system.addShips("reserve-ship", 4, Vector3D(0, 0, 0.01).fromCoordinateSystem("wpu"), 1000);
			
			for (var i=0; i<6; i++)
		    {
			var thargoidGroup = new ShipGroup("Thargoid invasion");
			this.addToGroup(system.addShipsToRoute("galnav-thargoid", 3, 0.166*i, 'wp'), thargoidGroup);
			this.addToGroup(system.addShipsToRoute("galnav-thargcruiser", 1, 0.166*i, 'wp'), thargoidGroup);
			this.addToGroup(system.addShipsToRoute("galnav-thargbatship", 1, 0.166*i, 'wp'), thargoidGroup);
		    } 
			this.reserve_enemy_ships = 30;
			break;
		}

		case "FLEET_INTERCEPT":
		{
			var pos1 = Vector3D(0, 0, -0.03).fromCoordinateSystem("wpu"); // intercept pilots
			system.addShips("intercept-behemoth", 1, pos1, 5000);
			system.addShips("intercept-frigate", 2, pos1, 5000);
			system.addShips("intercept-transport", 3, pos1, 5000);
			system.addShips("intercept-sweeper", 1, pos1, 5000);
			system.addShips("picket-reserve", 5, pos1, 1000);
			system.addShips("reserve-talker", 1, pos1, 5000);
            
			var pos2 = Vector3D(0, 0, -0.15).fromCoordinateSystem("wpu"); // thargoid fleet.
			var thargoidGroup = new ShipGroup("Thargoid fleet");
			this.addToGroup(system.addShips("galnav-thargoid", 15, pos2, 5000), thargoidGroup);
			this.addToGroup(system.addShips("galnav-thargcruiser", 10, pos2, 5000), thargoidGroup);
			this.addToGroup(system.addShips("galnav-thargbatship", 5, pos2, 5000), thargoidGroup);
			this.reserve_enemy_ships = 30;
			break;
		}

		case "POLICE_ACTION":
		{
			var hp = system.addShipsToRoute("reserve-hideout", 1, 0.1+Math.random()*0.2, "wp")[0].position;
			system.addShips("pirate-frigate", 1, hp, 5000);
			system.addShips("reserve-pirate", 8, hp, 5000);
			system.addShips("asteroid", 8, Vector3D(0, 0, 0.2).fromCoordinateSystem("wpu"), 10000);
			system.addShips("reserve-lead", 1, [hp.x, hp.y, 15E3], 1); // add them on an interception course.
			system.addShips("reserve-wingman", 4, [hp.x, hp.y, 10E3], 1000);
			this.reserve_enemy_ships = 10;
			break;
		}
	}
}

this.ReserveDutyDetermination = function()
{
	var chance = Math.random();

	if(chance < .34)
		return "SYSTEM_INVASION";

	else if(chance < .66)
		return "FLEET_INTERCEPT";

	else
		return "POLICE_ACTION";
}

//Cleans up the reserve duty variables in order to prevent oddities.
this.ReserveDutyCleanup = function()
{
        var message1 = "The battle at " + missionVariables.reserve_planetname + " is over.";
        var message2 = "Reserve pilots may now leave the system.";
	
	if(missionVariables.reserve_offer == "OFFER_COMPLETED")
	{
                if (player.ship.docked && player.ship.status == "STATUS_DOCKING")
                {
                    player.addMessageToArrivalReport("Gal Navy: " + message1 + " " + message2);
                }
                else
                {
                    player.commsMessage("Gal Navy: " + message1);
                    player.commsMessage("Gal Navy: " + message2);
		}
	}

        mission.setInstructionsKey(null);
        mission.unmarkSystem(missionVariables.reserve_planetnum);
        missionVariables.reserve_offer = "OFFER_OPEN";
        missionVariables.reserve_duty = null;
        missionVariables.reserve_planetnum = null;
        missionVariables.reserve_planetname = null;
        missionVariables.reserve_counter = 0;
        missionVariables.reserve_targets = 0;
	this.reserve_enemy_ships = 0;
}

// Merge ships into one group. Mainly used for Thargoids to minimise friendly attacks,
// because laserhits within a group are ignored in 90% of the cases.
this.addToGroup = function(ships, group)
{
    if (!ships || !group) return;
    for (var i = 0; i < ships.length; i++)
    {
        group.addShip(ships[i]);
        ships[i].group = group;
    }
}

// Code below is used by ship scripts to control chatter. Courtesy of Eric Walch.
this.messageTime = clock.absoluteSeconds // for use of spacing NPC messages by ship scripts.

this.messageAllowed = function()
{
    if((clock.absoluteSeconds - this.messageTime) > 10)
    {
        this.messageTime = clock.absoluteSeconds;
      return true;
    }
   else return false;
}

// Check to see if the system is a Sector Command system.
this.SecComSystemCheck = function()
{
	switch(galaxyNumber)
	{
		case 0:
		{
			if(system.ID == 24 || 
			   system.ID == 193 ||
			   system.ID == 154 ||
			   system.ID == 188 ||
			   system.ID == 125 ||
			   system.ID == 109 ||
			   system.ID == 249 ||
			   system.ID == 150 ||
			   system.ID == 80 ||
			   system.ID == 175 ||
			   system.ID == 120 ||
			   system.ID == 220 ||
			   system.ID == 158 ||
			   system.ID == 15)
				return true;
			else break;
		}
		
		case 1:
		{
			if(system.ID == 248 || 
			   system.ID == 135 ||
			   system.ID == 106 ||
			   system.ID == 82 ||
			   system.ID == 150 ||
			   system.ID == 182 ||
			   system.ID == 33 ||
			   system.ID == 48 ||
			   system.ID == 227 ||
			   system.ID == 96 ||
			   system.ID == 127 ||
			   system.ID == 202 ||
			   system.ID == 243 ||
			   system.ID == 170)
				return true;
			else break;
		}
		
		case 2:
		{
			if(system.ID == 31 || 
			   system.ID == 165 ||
			   system.ID == 245 ||
			   system.ID == 84 ||
			   system.ID == 36 ||
			   system.ID == 21 ||
			   system.ID == 26 ||
			   system.ID == 139 ||
			   system.ID == 251 ||
			   system.ID == 205 ||
			   system.ID == 223 ||
			   system.ID == 204 ||
			   system.ID == 17 ||
			   system.ID == 247)
				return true;
			else break;
		}
		
		case 3:
		{
			if(system.ID == 39 || 
			   system.ID == 221 ||
			   system.ID == 188 ||
			   system.ID == 86 ||
			   system.ID == 9 ||
			   system.ID == 57 ||
			   system.ID == 186 ||
			   system.ID == 237 ||
			   system.ID == 49 ||
			   system.ID == 103 ||
			   system.ID == 254 ||
			   system.ID == 66 ||
			   system.ID == 140 ||
			   system.ID == 110)
				return true;
			else break;
		}
		
		case 4:
		{
			if(system.ID == 249 || 
			   system.ID == 71 ||
			   system.ID == 57 ||
			   system.ID == 144 ||
			   system.ID == 198 ||
			   system.ID == 153 ||
			   system.ID == 204 ||
			   system.ID == 73 ||
			   system.ID == 102 ||
			   system.ID == 222 ||
			   system.ID == 29 ||
			   system.ID == 233 ||
			   system.ID == 152||
			   system.ID == 190)
				return true;
			else break;
		}
		
		case 5:
		{
			if(system.ID == 151 || 
			   system.ID == 227 ||
			   system.ID == 6 ||
			   system.ID == 51 ||
			   system.ID == 60 ||
			   system.ID == 129 ||
			   system.ID == 8 ||
			   system.ID == 53 ||
			   system.ID == 85 ||
			   system.ID == 120 ||
			   system.ID == 208 ||
			   system.ID == 220 ||
			   system.ID == 146 ||
			   system.ID == 66)
				return true;
			else break;
		}
		
		case 6:
		{
			if(system.ID == 214 || 
			   system.ID == 161 ||
			   system.ID == 250 ||
			   system.ID == 127 ||
			   system.ID == 23 ||
			   system.ID == 154 ||
			   system.ID == 234 ||
			   system.ID == 24 ||
			   system.ID == 55 ||
			   system.ID == 64 ||
			   system.ID == 231 ||
			   system.ID == 140 ||
			   system.ID == 202 ||
			   system.ID == 35)
				return true;
			else break;
		}
		
		case 7:
		{
			if(system.ID == 26 || 
			   system.ID == 218 ||
			   system.ID == 9 ||
			   system.ID == 174 ||
			   system.ID == 156 ||
			   system.ID == 251 ||
			   system.ID == 75 ||
			   system.ID == 136 ||
			   system.ID == 184 ||
			   system.ID == 87 ||
			   system.ID == 212 ||
			   system.ID == 116 ||
			   system.ID == 98 ||
			   system.ID == 55)
				return true;
			else break;
		}
		
		default:
		{
			return false;
		}
	}
	
	return false;
}

// Code to set up a Sector Command System.
this.SecComSystemSetUp = function()
{
	var victory_bond_count = missionVariables.vicbond_count;
	var pos1 = Vector3D(0, 0, 2).fromCoordinateSystem("psp"); // SecCom station position.
	var pos2 = Vector3D(0, 0, 2.2).fromCoordinateSystem("psp"); // Between the SecCom station and the sun.
	var pos3 = Vector3D(0, 0, 0.01).fromCoordinateSystem("wpu"); // Witchpoint guard position.
	
	if(victory_bond_count >= 100) // If they have 100 or more victory bonds, they get the 75% discount station.
		system.addShips("navystat75", 1, pos1, 0);
	
	else if(victory_bond_count >=50 && victory_bond_count < 100) // 50 to 99, 50%.
		system.addShips("navystat50", 1, pos1, 0);
	
	else if(victory_bond_count >= 25 && victory_bond_count < 50) // 25 to 49, 25%.
		system.addShips("navystat25", 1, pos1, 0);
	
	else system.addShips("navystat", 1, pos1, 0); // Less than 25, no discount.
	
	// SecCom Station guards.
	system.addShips("picket-frigate", 2, pos1, 5000);
	system.addShips("picket-minesweeper", 2, pos1, 5000);
	system.addShips("picket-transport", 3, pos1, 5000);
	system.addShips(expandDescription("[galNavy-ship]"), 1, pos2, 0);
	
	//Witchpoint guards.
	system.addShips("picket-frigate", 1, pos3, 0);
	system.addShips("picket-viper", 4, pos3, 500);
	
	//Patrolling Behemoth
	system.addShipsToRoute("navy-behemoth", 1, Math.random());
}