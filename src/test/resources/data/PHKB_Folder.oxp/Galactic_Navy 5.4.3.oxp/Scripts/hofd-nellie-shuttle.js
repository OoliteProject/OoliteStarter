this.name      = "hofd-nellie-shuttle"; 
this.author    = "eric walch"; 
this.copyright = "";
this.description = "ship script for Galactic Navy";
this.version   = "1.0";
this.licence   = "CC-by-NC-SA";


/*
    Originally the shuttle just spawned a scripted escape pod on death.
    Now we launch it through proper launch methods.
    It is still a scripted pod. An alternative would be to make Kurtz
    a character with a script. That way a player could even dump the Kurtz pod again.
*/

this.shipLaunchedEscapePod = function (pod)
{
    delete this.shipDied;
    pod.commsMessage("Bailing out!");
}


this.shipDied = function()
{
    this.ship.abandonShip(); // just a backup to make sure we always launch a pod.
}