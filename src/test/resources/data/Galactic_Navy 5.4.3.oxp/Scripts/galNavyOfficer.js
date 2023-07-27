this.name      = "galNavyOfficer"; 
this.author    = "eric walch"; 
this.copyright = "";
this.description = "ship script for a Galactic Navy officer";
this.version   = "1.0";
this.licence   = "CC-by-NC-SA";

// This script will only work with Oolite 1.75. 
// To make this code work, the scripted officer pods must removed from shipdata to allow the pilots be added as slaves (temporary) to the manifest.
// Advantage is that the pilot realy takes cargo spece untill docked.
// This code is untested!!!!!

this.unloadCharacter = function ()
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
