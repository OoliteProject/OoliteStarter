// this is a world event handler
// see https://wiki.alioth.net/index.php/Oolite_JavaScript_Reference:_World_script_event_handlers

"use strict";

this.name		= "oolite-starter-oxp";
this.author		= "Hiran";
this.license		= "GPL3";
this.description	= "Partner of the Oolite Starter";
this.version		= "0.1";

this.pushdata = false;

// brought in via https://github.com/maikschulz/oolite-mqtt-bridge/blob/master/oolite-mqtt-bridge.oxp/Config/script.js
this.startUpComplete = function()
{
    var callbackCounter = 0.0;
    var prevMsg = {
        'speed': player.ship.speed,
        'maxSpeed': player.ship.maxSpeed,
    };

    this.$fcb = addFrameCallback(function (delta)
    {
        if (pushdata!=true)
            return;
        if (!debugConsole)
            return;

        // do not update more than 10 times per second
        callbackCounter += delta;
        if (callbackCounter < 0.1)
            return;
        callbackCounter = 0.0;

        var msg = {
            'msgType': "controls",
            'speed': player.ship.speed,
            'maxSpeed': player.ship.maxSpeed,
            'serviceLevel': player.ship.serviceLevel,
            'targetSystem': player.ship.targetSystem,
            'nextSystem': player.ship.nextSystem,
            'previousSystem': player.ship.previousSystem,
            'legalStatus': player.legalStatus,
            'score': player.score,
            'rank': player.rank,
            'credits': player.credits,
            'escapePodRescueTime': player.escapePodRescueTime
        }
        if (msg.speed !== prevMsg.speed || msg.maxSpeed !== prevMsg.maxSpeed
                || msg.serviceLevel !== prevMsg.serviceLevel || msg.targetSystem !== prevMsg.targetSystem
                || msg.nextSystem !== prevMsg.nextSystem || msg.previousSystem !== prevMsg.previousSystem
                || msg.legalStatus !== prevMsg.legalStatus || msg.score !== prevMsg.score
                || msg.rank !== prevMsg.rank || msg.credits !== prevMsg.credits
                || msg.escapePodRescueTime !== prevMsg.escapePodRescueTime
                ) {
            debugConsole.consoleMessage(JSON.stringify(msg));
            prevMsg = msg;
        }

        //debugConsole.consoleMessage(JSON.stringify(msg));
    });
    commsMessageReceived("", {'displayName': this.name });
}

this.alertConditionChanged = function(newCondition, oldCondition)
{
    if (!debugConsole)
        return;
    if (this.pushdata!=true)
        return;

    var msg = {
        'msgType': "alert",
        'alertCondition': newCondition
    }
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.commsMessageReceived = function(message, sender)
{
    if (!debugConsole)
        return;
    if (this.pushdata!=true)
        return;

    var msg = {
        'msgType': "comms",
        'message': message,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.playerWillSaveGame = function(reason)
{
    log(this.name, "playerWillSaveGame(" + reason + ") -> storing resourcePaths");

    missionVariables["ooliteStarter_oxpList"] = oolite.resourcePaths;
}

///// Ship Script Events

this.shipWillDockWithStation = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "shipWillDockWithStation(" + station + ")");

    var msg = {
        'msgType': "shipWillDockWithStation",
        'station': station,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipDockedWithStation = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "shipWillDockWithStation(" + station + ")");

    var msg = {
        'msgType': "shipDockedWithStation",
        'station': station,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipWillLaunchFromStation = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "shipWillLaunchFromStation(" + station + ")");

    var msg = {
        'msgType': "shipWillLaunchFromStation",
        'station': station
    }
    // 'sender': sender.displayName
    // 'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipLaunchedFromStation = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "shipLaunchedFromStation(" + station + ")");

    var msg = {
        'msgType': "shipLaunchedFromStation",
        'station': station
        //,
        //'sender': sender.displayName
        //'sender': sender
    }
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.stationWithdrewDockingClearance = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "stationWithdrewDockingClearance()");

    var msg = {
        'msgType': "stationWithdrewDockingClearance",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.playerWillEnterWitchspace = function()
{
    if (!debugConsole)
        return;
    log(this.name, "playerWillEnterWitchspace()");

    var msg = {
        'msgType': "playerWillEnterWitchspace",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipExitedWormhole = function()
{
    if (!debugConsole)
        return;
    log(this.name, "shipExitedWormhole()");

    var msg = {
        'msgType': "shipExitedWormhole",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipWitchspaceBlocked = function()
{
   log(this.name, "shipWitchspaceBlocked()");

    var msg = {
        'msgType': "shipWitchspaceBlocked",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.wormholeSuggested = function(wormhole)
{
    if (!debugConsole)
        return;
    log(this.name, "wormholeSuggested(" + wormhole + ")");

    var msg = {
        'msgType': "wormholeSuggested",
        'wormhole': wormhole,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipEnteredStationAegis = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "shipEnteredStationAegis(" + station + ")");

    var msg = {
        'msgType': "shipEnteredStationAegis",
        'station': station,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipExitedStationAegis = function(station)
{
    if (!debugConsole)
        return;
    log(this.name, "shipExitedStationAegis(" + station + ")");

    var msg = {
        'msgType': "shipExitedStationAegis",
        'station': station,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipEnteredPlanetaryVicinity = function(planet)
{
    if (!debugConsole)
        return;
    log(this.name, "shipEnteredPlanetaryVicinity(" + planet + ")");

    var msg = {
        'msgType': "shipEnteredPlanetaryVicinity",
        'planet': planet,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipExitedPlanetaryVicinity = function(planet)
{
    if (!debugConsole)
        return;
    log(this.name, "shipExitedPlanetaryVicinity(" + planet + ")");

    var msg = {
        'msgType': "shipExitedPlanetaryVicinity",
        'planet': planet,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipApproachingPlanetSurface = function(planet)
{
    if (!debugConsole)
        return;
    log(this.name, "shipApproachingPlanetSurface(" + planet + ")");

    var msg = {
        'msgType': "shipApproachingPlanetSurface",
        'planet': planet,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipLeavingPlanetSurface = function(planet)
{
    if (!debugConsole)
        return;
    log(this.name, "shipLeavingPlanetSurface(" + planet + ")");

    var msg = {
        'msgType': "shipLeavingPlanetSurface",
        'planet': planet,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.cascadeWeaponDetected = function(weapon)
{
    if (!debugConsole)
        return;
    log(this.name, "cascadeWeaponDetected(" + weapon + ")");

    var msg = {
        'msgType': "cascadeWeaponDetected",
        'weapon': weapon,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.defenseTargetDestroyed = function(target)
{
    if (!debugConsole)
        return;
    log(this.name, "defenseTargetDestroyed(" + target + ")");

    var msg = {
        'msgType': "defenseTargetDestroyed",
        'target': target,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.escortAttack = function(target)
{
    if (!debugConsole)
        return;
    log(this.name, "escortAttack(" + target + ")");

    var msg = {
        'msgType': "escortAttack",
        'target': target,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.helpRequestReceived = function(ally, enemy)
{
    if (!debugConsole)
        return;
    log(this.name, "helpRequestReceived(" + ally + ", " + enemy + ")");

    var msg = {
        'msgType': "helpRequestReceived",
        'ally': ally,
        'enemy': enemy,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipAttackedOther = function(other)
{
    if (!debugConsole)
        return;
    log(this.name, "shipAttackedOther(" + other + ")");

    var msg = {
        'msgType': "shipAttackedOther",
        'other': other,
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipAttackedWithMissile = function(missile, whom)
{
    if (!debugConsole)
        return;
    log(this.name, "shipAttackedWithMissile(" + missile + ", " + whom + ")");

    var msg = {
        'msgType': "shipAttackedWithMissile",
        'missile': missile,
        'whom': whom
    }
    //'sender': sender.displayName
    //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipAttackerDistracted = function(whom)
{
    if (!debugConsole)
        return;
    log(this.name, "shipAttackerDistracted(" + whom + ")");

    var msg = {
        'msgType': "shipAttackerDistracted",
        'whom': whom
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipBeingAttacked = function(whom)
{
    if (!debugConsole)
        return;
    log(this.name, "shipBeingAttacked(" + whom + ")");

    var msg = {
        'msgType': "shipBeingAttacked",
        'whom': whom
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipBeingAttackedByCloaked = function()
{
    if (!debugConsole)
        return;
    log(this.name, "shipBeingAttackedByCloaked()");

    var msg = {
        'msgType': "shipBeingAttackedByCloaked",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipBeingAttackedUnsuccessfully = function(whom)
{
    if (!debugConsole)
        return;
    log(this.name, "shipBeingAttackedUnsuccessfully(" + whom + ")");

    var msg = {
        'msgType': "shipBeingAttackedUnsuccessfully",
        'whom': whom
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

 this.shipCloakActivated = function()
 {
    if (!debugConsole)
        return;
    log(this.name, "shipCloakActivated(" + whom + ")");

    var msg = {
        'msgType': "shipCloakActivated",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
 }
 
  this.shipCloakDeactivated = function()
 {
    if (!debugConsole)
        return;
    log(this.name, "shipCloakDeactivated(" + whom + ")");

    var msg = {
        'msgType': "shipCloakDeactivated",
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
 }
 
 this.shipTargetDestroyed = function(target)
{
    if (!debugConsole)
        return;
    log(this.name, "shipTargetDestroyed(" + target + ")");

    var msg = {
        'msgType': "shipTargetDestroyed",
        'target': target,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipDied = function(whom, why)
{
    if (!debugConsole)
        return;
    log(this.name, "shipDied(" + whom + + ", " + why + ")");

    var msg = {
        'msgType': "shipDied",
        'whom': whom,
        'why': why,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipEnergyBecameFull = function()
{
     // Your code here
}

this.shipEnergyIsLow = function()
{
     // Your code here
}

this.shipHitByECM = function(pulsesRemaining)
{
    if (!debugConsole)
        return;
    log(this.name, "shipHitByECM(" + pulsesRemaining + ")");

    var msg = {
        'msgType': "shipHitByECM",
        'pulsesRemaining': pulsesRemaining,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipFiredMissile = function(missile, target)
{
     // Your code here
}

this.shipKilledOther = function(whom,damageType)
{
     // Your code here
}

this.shipReleasedEquipment = function(mine)
{
    if (!debugConsole)
        return;
    log(this.name, "shipReleasedEquipment(" + mine + ")");

    var msg = {
        'msgType': "shipReleasedEquipment",
        'mine': mine,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipTargetAcquired = function(target)
{
    if (!debugConsole)
        return;
    log(this.name, "shipTargetAcquired(" + target + ")");

    var msg = {
        'msgType': "shipTargetAcquired",
        'target': target
    }
    //,
    //'sender': sender.displayName
    //'sender': sender
    log(this.name, "shipTargetAcquired: " + msg);
    
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipTargetCloaked = function()
{
     // Your code here
}

this.shipTargetLost = function(target)
{
    if (!debugConsole)
        return;
    log(this.name, "shipTargetLost(" + target + ")");

    var msg = {
        'msgType': "shipTargetLost",
        'target': target,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipTakingDamage = function(amount, whom, type)
{
     // Your code here
}

//this.cargoDumpedNearby = function(cargo: ship, releasedBy: ship)
this.cargoDumpedNearby = function(cargo, releasedBy)
{
     // Your code here
}

//this.commsMessageReceived = function(message: string, sender: ship)
this.commsMessageReceived = function(message, sender)
{
     // Your code here
}

//this.distressMessageReceived = function(aggressor: ship, sender: ship)
this.distressMessageReceived = function(aggressor, sender)
{
     // Your code here
}

this.equipmentAdded = function(equipmentKey)
{
    if (!debugConsole)
        return;
    log(this.name, "equipmentAdded(" + equipmentKey + ")");

    var msg = {
        'msgType': "equipmentAdded",
        'equipmentKey': equipmentKey
    }
    // ,
    // 'sender': sender.displayName
    // 'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.equipmentRemoved = function(equipmentKey)
{
    if (!debugConsole)
        return;
    log(this.name, "equipmentRemoved(" + equipmentKey + ")");

    var msg = {
        'msgType': "equipmentRemoved",
        'equipmentKey': equipmentKey
    }
    // ,
    // 'sender': sender.displayName
    // 'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipAchievedDesiredRange = function()
{
     // Your code here
}

this.shipAIFrustrated = function(context)
{
    if (!debugConsole)
        return;
    log(this.name, "shipAIFrustrated(" + context + ")");

    var msg = {
        'msgType': "shipAIFrustrated",
        'context': context,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipBountyChanged = function(delta,reason)
{
     // Your code here
}

this.shipCloseContact = function(otherShip)
{
    if (!debugConsole)
        return;
    log(this.name, "shipCloseContact(" + otherShip + ")");

    var msg = {
        'msgType': "shipCloseContact",
        'otherShip': otherShip,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipDumpedCargo = function(cargo)
{
    if (!debugConsole)
        return;
    log(this.name, "shipDumpedCargo(" + cargo + ")");

    var msg = {
        'msgType': "shipDumpedCargo",
        'cargo': cargo,
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipNowFacingDestination = function()
{
     // Your code here
}

this.shipReachedEndPoint = function()
{
     // Your code here
}

this.shipReachedNavPoint = function()
{
     // Your code here
}

this.shipScoopedOther = function(whom)
{
    if (!debugConsole)
        return;
    log(this.name, "shipScoopedOther(" + whom + ")");

    var msg = {
        'msgType': "shipScoopedOther",
        'whom': whom
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}

this.shipLaunchedEscapePod = function(escapepod, passengers)
{
    if (!debugConsole)
        return;
    log(this.name, "shipLaunchedEscapePod(" + escapepod + ", " + passengers + ")");

    var msg = {
        'msgType': "shipLaunchedEscapePod",
        'escapepod': escapepod,
        'passengers': passengers
    }
        //,
        //'sender': sender.displayName
        //'sender': sender
    debugConsole.consoleMessage(JSON.stringify(msg));
}