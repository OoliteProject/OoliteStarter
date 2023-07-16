# Oolite Starter

The Oolite Starter allows users to more comfortably preconfigure Oolite
for various missions. It's main purpose is to ease OXP management, Oolite versions
and manage save games.

## Requirements (generic package only)

* You need to have Java SDK 17 or newer installed. 
  If you are unfamiliar with installing Java on Linux, follow
  https://www.youtube.com/watch?v=7lzIP-PvHoY
  If you are unfamiliar with installing Java on MacOS, follow
  https://www.youtube.com/watch?v=wXotUgqOdh8
  If you are unfamiliar with installing Java on Windows, follow
  https://www.youtube.com/watch?v=IJ-PJbvJBGs
* You need to have a copy of the generic Oolite Starter unzipped in a directory of
  your choice. Just in case, you can download it from
  https://github.com/HiranChaudhuri/OoliteStarter/releases

## Installation and Uninstallation

### Generic Package

Download the .tar.gz or the .zip file and extract in a directory of your choice.
You likely want to create a shortcut to run.sh or run.cmd.

To remove the software, just delete the directory you have created.

### Linux (.deb)

Download the .deb file. Run

    sudo apt install <path to downloaded .deb>

After that you should see a new icon in your desktop environment.

To remove the software, run

    sudo apt remove oolitestarter

### MacOS (.dmg)

The .dmg download has not yet been successfully installed. If you manage to
succeed, please give a shout.

### Windows (.exe)

Download the .exe file. Tell Windows you want to keep the file regardless of
warnings. Then run the installer. It will automatically install OoliteStarter
and create menu and desktop shortcuts.

To remove the software, go to control panel/apps. Search for OoliteStarter
and choose to uninstall.

## Configuration (Installations)

When you run the application, it will seach the configuration file
$HOME/.oolite-starter.conf

If this file is not present, do not worry. A warning will be displayed and the
application will run. Switch to the Installations tab and add at least one
Oolite installation. Then press Save and the missing file will be created.

For each Oolite installation you can define a number of places in the filesystem.
Since some players have multple such installations in parallel, they can be
configured once.

Oolite-Starter needs to know which installation it should actually use. Select
the one of your choice and press the Activate button. At this time OoliteStarter
will check if some directory is missing and also offers to create it for you.

## Usage (Quickstart)

* Run the application. Depending on your operating system and the package you 
  chose this may mean to just click an icon, a menu entry or use either 
  run.sh or run.cmd. When going for run.sh you may need to set execute
  permissions (chmod +x run.sh)
* The Starter will present you with the list of your save games. Either press
  the 'New' button to run a new game, or choose the save game and press 'Continue'.
  Either of those buttons will run Oolite the way you chose.
* Play as usual.
* If you want to change the installed expansions, switch to the 'Expansions' tab,
  check the list and choose to install, remove, enable or disable single expansions.
  You can also export your currently active expansions as 'Oolite Expansion Set',
  and load it later again or share with your friends.

Happy Flying!

## Usage (Detailed description)

In the following you will find detailed description of each of these use cases:

![](https://www.plantuml.com/plantuml/svg/TP5FImCn4CNl-HH3x_q2FKWN6gG75LhqFcmU4ioViZDP1V6xQxIuOul7UUybxvVCLfGdRJcv4uyl_0mwyenRpX1wTqHDCBA0xl5gfWEaPHXXXmI5JI1L3kx3_TwxycydBX9heJikNqf6qHMnq6BOndqHvPIkIlg9dJVoC6gS-SSOlCs3pzOLMCyU_1-yXvgYWfumTHIvX3pZi5vo3Y6OwJXoiMt8TSPtszx8FaJzxmKsrtVH8JUFWcb3w_NDiXuZBZzeHKlit1Obj9m-0G00)

### SaveGames

![](https://www.plantuml.com/plantuml/svg/SoWkIImgAStDuIf8JCvEJ4zL24uiItLFp4qjLgZcKb0eBKvDJYnELV1BBKVY1QKMb-Qc8WcufEQabgIYgCZ9JqpXgkNYiWejJYsoKj3LjGCRWk32J468m8n1YC3CuN98pKi1kX80)

#### New

Starts Oolite the usual way. You can use it to start a new commander.

#### Resume

Starts Oolite with the selected savegame. The game will be loaded from disk so
you can continue that game. Requires that you saved the game before from
within Oolite.

#### Delete

Deletes the selected savegame.

#### Reload

Loads data freshly from disk. This is similar to pressing reload in your web 
browser. OoliteStarter reloads the data automatically when a change is likely to
have happened. If you still feel the data might be outdated just click here.

### Expansion Management

![](https://www.plantuml.com/plantuml/svg/LOr1gi9G34NdMKNel1leuDD2t-0Fd1HSG6Y35DEqD7aYY7lNXw9nUC-vkMdDQVUy26mqdkd4MANhHchpgdWWVPA5rIiamw-u0M8s7iaO_zMSH0BfMTYvNFGfu6xq-K8lqQMVFhrLW0FBIbFf3Wdk0CtHU6-mhly-7mFgKqnTJ64-qB8ABUkK5ta0)

Expansions follow this lifecycle. It should explain why in Oolite Versions 
Management so many directories need to be configured.

![](https://www.plantuml.com/plantuml/svg/SoWkIImgAStDuOhMYbNGrRLJy4tCIqnFJN5CISdFAxPIyCm3giZ9uK8HLa2eSqbDJ2x9B4iiGMXnoInEJCf9vSAbGW7J5jSyX0w4Cg0weAki559ISr9BCCt3gP0BOGyUvs5r5vg3S5X0s0aEgNafm5050000)

While OXZs that are listed on the Expansion Manager's manifest can have a full
lifecycle through install and delete, OXPs that are found in the Addons folder
cannot be installed. They do not come with a download URL.

#### Install

Installs an OXZ from the Expansion Manager's manifest.

#### Delete

Deletes the selected OXZ or OXP from your disk.

#### Activate

Activates an OXZ or OXP. This is done by moving it to the folder that Oolite
will read on startup (the Addons and ManagedAddons folders).

#### Deactivate

Deactivates an expansion by moving it to a folder where Oolite will not find it
(the DeactivatedAddons and ManagedDeactivatedAddons folders).
The expansion remains in that hidden location until you either delete or enable
it.

#### Reload

Loads data freshly from disk. This is similar to pressing reload in your web 
browser. OoliteStarter reloads the data automatically when a change is likely to
have happened. If you still feel the data might be outdated just click here.

### Oolite Versions Management

![](https://www.plantuml.com/plantuml/svg/JP2_2eD03CRtF4LmzmKSYWuT74f1QVTm5yhHCnOJNKhVlQSk6N_v_doGLeqMMwSC7mflUZ5MzpadOtpoecaMnHj9kKyiLY4fzb8YEPCL4R-0k2e7KiOkHaTZ874uy3HllEU7tfslCPcl29OsiiFNUTz86yCFe7aeBmtMzMMVStZiUHvwH-UE3tlrR8UM9PP7_040)

#### Add

Brings up a configuration dialog that allows you entering all the relevant
directory paths for Oolite. Once you press Ok you can see the new version
to be added to the list of versions.

#### Scan

In case you want OoliteStarter to search for Oolite on your behalf just click
this button. While the search is running you can already accept one of the 
Oolite home directories found so far. As soon as you click Ok the system
switches to Add mode allowing you to tweak the settings before they are added
to the list of versions.

#### Remove

Removes the selected version from the list.

#### Edit

Allows you to change settings in the selected version.

#### Activate

Since you can have multiple versions configured, OoliteStarter needs to know
which of the settings to work with. The Select button will activate the selected
version. Only one version can be active at any time. It is marked with a star.

#### Save

All the version settings are just held in RAM and lost when you exit 
OoliteStarter. Press Save if you want these settings to be available when you
next time run OoliteStarter.

The configuration data is stored in $HOME/.oolite-starter.conf.