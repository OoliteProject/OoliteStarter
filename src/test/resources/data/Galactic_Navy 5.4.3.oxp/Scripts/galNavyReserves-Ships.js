this.name      = "galNavyReserves-Ships";
this.author    = "Eric Walch";
this.copyright = "� 2009";
this.description = "Ship script for Galactic Navy reserve ships.";
this.version   = "Galactic Navy Build 133 / Galactic Navy Reserve Ship 1";
this.licence		= "CC-by-NC-SA";


/*
Send all messages through the main script for validation to avoid all ships constantly broadcasting messages.
*/

this.sendReserveAttacked = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-attacked]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveWarning = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-warning]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveKill = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-kill]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveMayday = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-mayday]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveHail = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-hail]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveHail2 = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-hail2]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveHail3 = function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-hail3]"))
    // no messages within 10 secs to avoid screen clutter.
}

this.sendReserveTarget= function()
{
   if(worldScripts["GalNavy"].messageAllowed())
      this.ship.commsMessage(expandDescription("[reserve-target]"))
    // no messages within 10 secs to avoid screen clutter.
}