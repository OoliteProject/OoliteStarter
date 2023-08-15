/*

asteroids3D.js

Script for Asteroids3D.oxp.


Oolite
Copyright © 2004-2012 Giles C Williams and contributors

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
MA 02110-1301, USA.


Asteroids3D.oxp
Copyright © 2012 "Commander McLane"

This work is licensed under the Creative Commons
Attribution-Noncommercial-Share Alike 3.0 Unported License.

To view a copy of this license, visit
http://creativecommons.org/licenses/by-nc-sa/3.0/ or send a letter
to Creative Commons, 171 Second Street, Suite 300, San Francisco,
California, 94105, USA.

*/


this.name           = "asteroids3D";
this.description	= "Script for Asteroids3D.oxp.";
this.author         = "Commander McLane";
this.copyright      = "© 2012 Commander McLane";
this.license		= "CC-by-nc-sa 3.0";
this.version        = "1.2";


/* functions */

this.startUp = function()
{
    this.levelNames = ["", "BEGINNER", "MEDIUM", "EXPERT"];
    this.disaster = ["Space", "Earthquake", "Flood", "Zero-G", "Vacuum", "Disaster", "Solar Activity", "Trumble", "Hoopy Casino", "Asteroid", "Comet", "Nova", "Environmental", "Thargoid", "Edible Poet"];
    this.victim = ["Orphans", "Victims", "Widows", "Survivors", "Relief", "Protection", "Safety", "Care"];
    this.fund = ["Fund", "Association", "Charity", "Trust", "Agency", "Authority", "Purse", "Foundation", "Society", "Coalition", "Endowment", "Bureau", "Organization", "Consortium", "Aid"];
    this.lastSavingTime = clock.seconds;
}

this.playerWillSaveGame = function()
{
    // time since last saving is stored
    this.lastSavingTime = clock.seconds;
}

this.$playingTimeFromSeconds = function(seconds)
{
    // this function converts the playing time in seconds into a readable string
    var hoursComponent = Math.floor(seconds / 3600);
    var minutesComponent = Math.floor((seconds % 3600) / 60);
    var secondsComponent = Math.floor(seconds % 60);
    var playingTime = "";
    if(hoursComponent > 0) playingTime += hoursComponent + " hour";
    if(hoursComponent > 1) playingTime += "s";
    if(playingTime !== "")
    {
        if(minutesComponent > 0 && secondsComponent > 0) playingTime += ", ";
        else if(minutesComponent > 0) playingTime += " and ";
    }
    if(minutesComponent > 0) playingTime += minutesComponent + " minute";
    if(minutesComponent > 1) playingTime += "s";
    if(playingTime !== "" && secondsComponent > 0) playingTime += " and ";
    if(secondsComponent > 0) playingTime += secondsComponent + " second";
    if(secondsComponent > 1) playingTime += "s";
    return playingTime;
}

this.$scoopedItems = function()
{
    // this function converts the scooped items into a readable string
    var scoopedItems = "";
    if(this.scoopedMinerals === 0 && this.scoopedAlienItems === 0 && this.scoopedAlloys === 0)
    {
        this.extraLineBreak = "\n";
        return scoopedItems;
    }
    if(this.scoopedMinerals > 0)
        scoopedItems += this.scoopedMinerals + "t of minerals";
    if(scoopedItems !== "")
    {
        if(this.scoopedAlienItems > 0 && this.scoopedAlloys > 0)
            scoopedItems += ", ";
        if((this.scoopedAlienItems > 0 && this.scoopedAlloys === 0) || (this.scoopedAlienItems === 0 && this.scoopedAlloys > 0))
            scoopedItems += " and ";
    }
    if(this.scoopedAlienItems > 0)
        scoopedItems += this.scoopedAlienItems + "t of Alien Items";
    if(this.scoopedAlienItems > 0 && this.scoopedAlloys > 0)
        scoopedItems += " and ";
    if(this.scoopedAlloys > 0)
        scoopedItems += this.scoopedAlloys + "t of Alloys";
    scoopedItems = " You also scooped " + scoopedItems + ".";
    this.extraLineBreak = "";
    return scoopedItems;
}

this.playerBoughtEquipment = function(equipment)
{
    // if the player bought themselves a game they get on a mission screen
    if(equipment === "EQ_ASTEROIDS3D")
    {
        // the equipment is removed
        player.ship.removeEquipment("EQ_ASTEROIDS3D");
        // and we check whether they have saved their game
        if(clock.seconds - this.lastSavingTime > 30)
        {
            // if not, a choice to do so is offered
            mission.runScreen({title:"Asteroids 3D", message:"Welcome!\n\nThanks for your interest in playing Asteroids3D.\n\nFor your own safety it is recommended that you save first before playing.", model:"asteroids3D_asteroid", choicesKey:"asteroids3D-saveChoices"}, this.$savingGame);
        }
        else
        {
            // if yes, it's right away to the instructions screen
            this.$showInstructionsScreen();
        }
    }
}

this.$savingGame = function(choice)
{
    // if the choice was to play anyway, fast forward to the instructions screen
    if(choice === "2")
    {
        this.$showInstructionsScreen();
    }
}

this.$showInstructionsScreen = function()
{
    // four playing choices are offered
    mission.runScreen({title:"Asteroids 3D", message:"Welcome!\n\nCare for a game of Asteroids3D?\n\nChoose your level or exit the game.", model:"asteroids3D_asteroid", choicesKey:"asteroids3D-gameChoices"}, this.$playingGame);
}

this.$playingGame = function(choice)
{
    // if the choice was to end the game, or F1 was pressed, nothing further happens
    if(choice === "4" || !choice) return;
    // the number of debris parts created by each object is set according to the difficulty level (from 2 to 4)
    this.debrisNumber = parseInt(choice) + 1;
    // a flying saucer will appear after each 150 to 90 seconds, according to difficulty level
    this.thargoidFrequency = 180 - (30 * choice);
    // a mission variable is set
    missionVariables.asteroids3D = "GAME_STARTED";
    // the level is stored
    this.level = this.levelNames[choice];
    // the player is launched
    player.ship.launch();
}

this.shipWillLaunchFromStation = function(station)
{
    // if this launch is part of the game (= mission variable is set) ...
    if(missionVariables.asteroids3D === "GAME_STARTED")
    {
        // we remember the current station
        this.station = station;
        // the player is moved into deep space immediately, always to a spot where the planet is right in front of the sun
        player.ship.position = system.sun.position.add(system.mainPlanet.position.subtract(system.sun.position).direction().multiply(3500000));
        // eight asteroids are added around the player and begin their flight
        system.addShips("asteroids3D_asteroid", 8, player.ship.position, 8000);
        // some information is briefly displayer
        player.consoleMessage("Your game has started.", 10);
        player.consoleMessage("Shoot all asteroids and scoop or shoot the debris.", 10);
        player.consoleMessage("Please adjust your scanner magnification to 3:1.", 10);
        // the timer that will create a flying saucer with the previously set frequency is started 
        this.spawnThargoidsTimer = new Timer(this, this.$spawnThargoid, this.thargoidFrequency, this.thargoidFrequency);
        // the timer that checks for finishing the game by clearing all objects is started
        this.checkForStuffTimer = new Timer(this, this.$checkForRemainingStuff, 3, 3);
        // the time, money, and kills at starting are stored
        this.gameStartTime = clock.absoluteSeconds;
        this.gameStartCredits = player.credits;
        this.gameStartScore = player.score;
        // we're also keeping track of any scooped things
        this.scoopedMinerals = 0;
        this.scoopedAlloys = 0;
        this.scoopedAlienItems = 0;
    }
}

this.$spawnThargoid = function()
{
    // a flying saucer is added at the edge of the scanner each time the timer fires
    system.addShips("asteroids3D_thargoid", 1, player.ship.position.add(Vector3D.randomDirection(8000)), 0);
}

this.shipScoopedOther = function(other)
{
    // each scooped item during the game raises a counter
    if(missionVariables.asteroids3D === "GAME_STARTED")
    {
        switch(other.name)
        {
            case "Splinter":
                this.scoopedMinerals++;
                break;
                
            case "Metal fragment":
                this.scoopedAlloys++;
                break;
            
            case "Thargoid Robot Fighter":
                this.scoopedAlienItems++;
        }
    }
}

this.$checkForRemainingStuff = function(other)
{
    // if the game still runs and there are no objects left (because they're either shot or scooped) ...
    if(missionVariables.asteroids3D === "GAME_STARTED" && system.countShipsWithRole("asteroids3D", player.ship, 9000) === 0)
    {
        // the playing time and the earned money are calculated
        this.playingTime = this.$playingTimeFromSeconds(clock.absoluteSeconds - this.gameStartTime);
        this.earnedCredits = Math.round((player.credits - this.gameStartCredits) * 10) / 10;
        // but this isn't a get-rich-quick scheme
        player.credits = this.gameStartCredits;
        player.score = this.gameStartScore;
        player.ship.manifest.minerals -= this.scoopedMinerals;
        player.ship.manifest.alienItems -= this.scoopedAlienItems;
        player.ship.manifest.alloys -= this.scoopedAlloys;
        // the player gets a message
        player.consoleMessage("Congratulations!", 6);
        player.consoleMessage("You finished your game of Asteroids3D.", 6);
        // the mission variable is set
        missionVariables.asteroids3D = "GAME_FINISHED";
        // the two timers are stopped and deleted
        this.spawnThargoidsTimer.stop();
        delete this.spawnThargoidsTimer;
        this.checkForStuffTimer.stop();
        delete this.checkForStuffTimer;
        // if the station where the game was bought still exists, the player is docked with it
        if(this.station.isValid) this.station.dockPlayer();
        // or else with the main station
        else system.mainStation.dockPlayer();
    }
}

this.shipWillDockWithStation = this.shipWillEnterWitchspace = function()
{
    // if the player docks with something while there are still objects around ...
    if(missionVariables.asteroids3D === "GAME_STARTED")
    {
        // the mission variable is set accordingly
        missionVariables.asteroids3D = "GAME_ABANDONED";
        // no gain from an abandoned game
        player.credits = this.gameStartCredits;
        player.score = this.gameStartScore;
        player.ship.manifest.minerals -= this.scoopedMinerals;
        player.ship.manifest.alienItems -= this.scoopedAlienItems;
        player.ship.manifest.alloys -= this.scoopedAlloys;
        // the two timers are stopped and deleted
        this.spawnThargoidsTimer.stop();
        delete this.spawnThargoidsTimer;
        this.checkForStuffTimer.stop();
        delete this.checkForStuffTimer;
    }
}

this.missionScreenOpportunity = function()
{
    if(missionVariables.asteroids3D)
        {
        // if the game was abandoned by prematurely docking ...
        if(missionVariables.asteroids3D === "GAME_ABANDONED")
        {
            // a mission screen confirms the end of the game
            mission.runScreen({title:"Asteroids3D", message:"Notification:\n\nYou have cancelled your game by jumping out or docking before clearing all asteroids.\n\nYou don't make it on the high score list.\n\nHave a nice day, commander.", model:"asteroids3D_asteroid"});
        }
        // if the game was properly ended ...
        if(missionVariables.asteroids3D === "GAME_FINISHED")
        {
            // a mission screen confirms this and offers another game
            mission.runScreen({title:"Asteroids3D", message:"Welcome back!\n\nYou finished a game of Asteroids3D in " + this.level + " level. You have cleared the asteroid field in " + this.playingTime + ", and earned " + this.earnedCredits + " ₢ along the way." + this.$scoopedItems() + "\n\n As it's just a game for fun, you have decided to give all your winnings to charity. The " + system.name + "ian " + this.disaster[Math.floor(Math.random() * this.disaster.length)] + " " + this.victim[Math.floor(Math.random() * this.victim.length)] + " " + this.fund[Math.floor(Math.random() * this.fund.length)] + " has already received your generous donation, and wishes to express its heartfelt gratitude.\n\nWe hope you enjoyed your game.\n\n\n" + this.extraLineBreak + "Want to play again?\n\nChoose your level or exit the game.", model:"asteroids3D_asteroid", choicesKey:"asteroids3D-gameChoices"}, this.$playingGame);
        }
        // and everything is cleared
        delete missionVariables.asteroids3D;
        delete this.gameStartTime;
        delete this.playingTime;
        delete this.gameStartCredits;
        delete this.earnedCredits;
        delete this.gameStartScore;
        delete this.scoopedMinerals;
        delete this.scoopedAlloys;
        delete this.scoopedAlienItems;
        delete this.extraLineBreak;
    }
}

this.shipDied = function()
{
    if(missionVariables.asteroids3D === "GAME_STARTED")
    {
        // the mission variable is set
        missionVariables.asteroids3D = "PLAYER_DIED";
        // the two timers are stopped and deleted
        this.checkForStuffTimer.stop();
        delete this.checkForStuffTimer;
        this.spawnThargoidsTimer.stop();
        delete this.spawnThargoidsTimer;
        // the remaining asteroids and saucers are removed
        system.shipsWithRole("asteroids3D").forEach(function(ship){ship.remove()}, this);
        // wait a couple of seconds, then display a screen
        this.waitTimer = new Timer(this, this.$displayGameOverScreen, 5);
    }
}

this.$displayGameOverScreen = function()
{
    // game over screen in case the player was killed while playing Asteroids3D
    mission.runScreen({title:"Asteroids3D", message:"Hi, Commander!\n\nIt seems you've lost this game. The good news is that it was entirely virtual, and no harm was done to you or your ship.\n\nYou can try again if you want.", model:"asteroids3D_asteroid"});
    // the wait timer is deleted
    delete this.waitTimer;
}
