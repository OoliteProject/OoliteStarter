this.name = "hofd";
this.author = "matt634, Nemoricus";
this.copyright = "(c) matt634";
this.description = "This is the mission script for the wayward admiral. (Part of Galactic Navy.oxp)"
this.version = "5.3.0";

//Code to add mission ships after launching from a station in Galaxy 6, System 214.
this.shipWillLaunchFromStation = function ()
{
	//Making sure that the mission isn't already over.
	if(missionVariables.hofd != "HOFD_COMPLETE")
	{
		//Adding in the Admiral's ships if they aren't already present.
		if(galaxyNumber == 5 && system.ID == 214)
		{
			//The Admiral's command ship and escort initially appear on the planet-sun line.
			if(missionVariables.hofd == "HOFD_ARRIVED")
			{
				if(system.countShipsWithPrimaryRole("hofd-nellie") == 0)
				{
					this.addNelly(Vector3D(0, 0, 0.15).fromCoordinateSystem("psu"));
				}
			}

			//The Admiral's command ship and escort move to the witchpoint-planet 
			//line after the player meets with him.
			if(missionVariables.hofd == "HOFD_MEETING_OVER" || missionVariables.hofd == "HOFD_SERVICE")
			{
				if(system.countShipsWithPrimaryRole("hofd-nellie") == 0)
				{
					this.addNelly(Vector3D(0, 0, 0.2).fromCoordinateSystem("wpu"));
				}
			}
		}
	}
    else
    {
        this.cleanUp(); // mission is over.
    }
}

//Code to add Admiral's command ship and escorts.
this.addNelly = function (position)
{
    system.addShips("hofd-nellie", 1, position, 1);
    system.addShips("picket-frigate", 3, position, 5000);
    system.addShips("picket-transport", 5, position, 5000);
    system.addShips("picket-minesweeper", 2, position, 5000);
    system.addShips("picket-atlas", 1, position, 5000);
}

//Code to add mission ships after entering specific systems from Witchspace.
this.shipWillExitWitchspace = function()
{
	//Making sure that the mission isn't already completed.
	if(missionVariables.hofd != "HOFD_COMPLETE")
	{
		if(galaxyNumber == 5)
		{
			//Adding hunters.
			//
			//Nemoricus: If anyone looking through this code would knows what was in matt634's mind
			//when he added these hunters, let me know. I certainly don't know.
			//Eric: This are the last 4 systems the player must pass on his way to the mission system.
			if(missionVariables.hofd == "HOFD_IN_ROUTE" && (system.ID == 3 || system.ID == 84 || system.ID == 236 || system.ID == 111))
			{
				system.addShipsToRoute("hofd-headhunter", 5, null, "wp");
			}

			//This adds in the Admiral's ships when you enter System 214. They start off on the planet-sun line.
			if(system.ID == 214 && (missionVariables.hofd == "HOFD_IN_ROUTE" || missionVariables.hofd == "HOFD_ARRIVED" || 
                        missionVariables.hofd == "HOFD_MEETING_OVER" || missionVariables.hofd == "HOFD_SERVICE"))
			{
				this.addNelly(Vector3D(0, 0, 0.15).fromCoordinateSystem("psu"));
			}
		}

		//Setting up for the news update after the Admiral has met one of his three fates.
		//Nemoricus: I'm choosing to set this up regardless of galaxy in case the player
		//decides to change galaxies immediately after.

		switch(missionVariables.hofd)
		{
			case "HOFD_PREP_UPDATE":
			{
				missionVariables.hofd = "HOFD_UPDATE";
				break;
			}

			case "KURTZ_KILLED":
			{
				missionVariables.hofd = "HOFD_PREP_UPDATE2";
				break;
			}

			case "KURTZ_ESCAPED":
			{
				missionVariables.hofd = "HOFD_PREP_UPDATE3";
				break;
			}
		}
	}
}


//Offering missions and checking what choices the player has chosen.
this.missionScreenOpportunity = function()
{
	if(player.ship.docked && missionVariables.hofd != "HOFD_COMPLETE")
	{
		//The following bit is to deal with the case of the player being docked at the appropriate station
		//and getting a mission screen right away that's not part of this mission.
		if(galaxyNumber == 5 && missionVariables.hofd != "HOFD_OFFER" && missionVariables.hofd != "HOFD_MEETING") 
		{
			//This section deals with the aftermath of the mission. Put first because it's the most likely case.
			if(player.ship.dockedStation.isMainStation)
			{
				switch(missionVariables.hofd)
				{
					//Player captures the Admiral.
					case "HOFD_UPDATE":
					{
						mission.runScreen({title: "Galactic Navy", messageKey: "hofd_news_update"});
						missionVariables.hofd = "HOFD_COMPLETE";
						break;
					}
					//Player kills the Admiral.
					case "HOFD_PREP_UPDATE2":
					{
						mission.runScreen({title: "Galactic Navy", messageKey: "hofd_news_update2"});
						missionVariables.hofd = "HOFD_COMPLETE";
						break;
					}
	
					//Admiral escapes.
					case "HOFD_PREP_UPDATE3":
					{
						mission.runScreen({title: "Galactic Navy", messageKey: "hofd_news_update3"});
						missionVariables.hofd = "HOFD_COMPLETE";
						mission.unmarkSystem(214);
						break;
					}
				}
			} 
			
			//Checking if the player has docked with a Navy SecCom Station.
			else if(player.ship.dockedStation.name == "Navy SecCom Station" && missionVariables.seccom_station == "DOING_BUSINESS")
			{
				//This section offers the player the HOFD mission.
				if((missionVariables.player_sortie > 9 && !missionVariables.hofd) || (missionVariables.hofd == "HOFD_WAIT" && Math.random() < .1)) 
				{
				    mission.runScreen({title: "Galactic Navy", messageKey: "hofd_offer", choicesKey: "hofd_offer_choice"},
				    function (choice)
				    {
					if (choice === "hofd_accept")
					{
					    mission.runScreen({title: "Galactic Navy", messageKey: "hofd_intro"});
					    missionVariables.hofd = "HOFD_IN_ROUTE";
					    mission.setInstructionsKey("hofd_short_desc");
					    mission.markSystem(214);
					    player.credits += 1500.0;
					}
					if (choice === "hofd_decline")
					{
					    mission.runScreen({title: "Galactic Navy", messageKey: "hofd_decline"});
					    missionVariables.hofd = "HOFD_WAIT";
					}
				    });
				    missionVariables.hofd = "HOFD_OFFER";
				}
				
				//This section rewards the player for their capture of the Admiral.
				if(missionVariables.hofd == "KURTZ_CAPTURED")
				{
					mission.runScreen({title: "Galactic Navy", messageKey: "hofd_welldone"});
					missionVariables.hofd = "HOFD_PREP_UPDATE";
					mission.setInstructionsKey();
					player.credits += 25000.0;
					player.bounty = 0;
				}
			}

			//This section deals with the dealings with the Admiral on his ship.
			else if(player.ship.dockedStation.name == "Behemoth - Nellie")
			{
				//The Admiral makes his request.
				if(missionVariables.hofd == "HOFD_ARRIVED")
				{
				    mission.runScreen({title: "Galactic Navy", messageKey: "hofd_kurtz_offer", choicesKey: "hofd_kurtz_choice"},
				    function (choice)
				    {
					if (choice === "hofd_kurtz_accept")
					{
					    mission.runScreen({title: "Galactic Navy", messageKey: "hofd_service"});
					    missionVariables.hofd = "HOFD_SERVICE";
					    player.bounty += 100;
					}
					if (choice === "hofd_kurtz_decline")
					{
					    mission.runScreen({title: "Galactic Navy", messageKey: "hofd_no_service"});
					    missionVariables.hofd = "HOFD_MEETING_OVER";
					}
				   });
					missionVariables.hofd = "HOFD_MEETING";
				}
				
				else if(missionVariables.hofd == "HOFD_SERVICE" || missionVariables.hofd == "HOFD_MEETING_OVER")
					player.ship.launch(); // Kick the player out after he makes his choice.
			}
		}
	}
}

//Mision is over, no need for further executing handlers
this.cleanUp = function ()
{
    delete this.missionScreenOpportunity;
    delete this.shipWillExitWitchspace;
    delete this.shipWillLaunchFromStation;
}