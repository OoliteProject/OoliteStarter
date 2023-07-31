/*

asteroids3D-object.js

Ship script for Asteroids3D-objects.


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


this.name           = "asteroids3D-object";
this.description	= "Ship script for Asteroids3D-objects; keeps them around the player.";
this.author         = "Commander McLane";
this.copyright      = "© 2012 Commander McLane";
this.license		= "CC-by-nc-sa 3.0";
this.version        = "1.2";


/* functions */

this.shipSpawned = function()
{
    // the timer that checks the distance to the player ten times per second is started
    this.checkDistanceTimer = new Timer(this, this.$checkDistanceToPlayer, 0.1, 0.1);
    // thargoids and thargons don't need debris number or velocity
    // but gets aimed at the player
    if(this.ship.isThargoid)
    {
        this.ship.target = player.ship;
        this.ship.AIState = "ATTACK_SHIP";
        return;
	}// the number of parts each object is going to split into is set according to the player's difficulty choice (between 2 and 4)
    this.debrisNumber = worldScripts.asteroids3D.debrisNumber;
    // each object gets a random velocity (random speed up to 320 in a random direction)
    this.ship.velocity = Vector3D.random(320);
}

this.$checkDistanceToPlayer = function()
{
	// the position of the object relative to the player is established
    this.relativePosition = player.ship.position.subtract(this.ship.position);
    // if the object is farther than 8533 meters away ...
    if(this.relativePosition.magnitude() > 8533)
    {
        // ... it's moved to the exact opposite position, but 5% closer to the player
        this.ship.position = player.ship.position.add(this.relativePosition.multiply(0.95));
    }
}

this.shipDied = function()
{
    // the timer is stopped and deleted
    this.checkDistanceTimer.stop();
    delete this.checkDistanceTimer;
    // if the player has died no boulders or splinters are spawned
    if(!player.ship.isValid) return;
	// destroyed asteroids spawn the established number of boulders
    if(this.ship.primaryRole === "asteroids3D_asteroid") this.ship.spawn("asteroids3D_boulder", this.debrisNumber);
	// destroyed boulders spawn the established number of splinters
	if(this.ship.primaryRole === "asteroids3D_boulder") this.ship.spawn("asteroids3D_splinter", this.debrisNumber);
}
