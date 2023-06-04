# Oolite Starter

The Oolite Starter allows users to more comfortably preconfigure Oolite
for various missions. It's main purpose is to ease OXP management, Oolite versions
and manage save games.

## Requirements

* You need to have Java SDK 17 or newer installed. 
  If you are unfamiliar with installing Java on Linux, follow
  https://www.youtube.com/watch?v=7lzIP-PvHoY
  If you are unfamiliar with installing Java on MacOS, follow
  https://www.youtube.com/watch?v=wXotUgqOdh8
  If you are unfamiliar with installing Java on Windows, follow
  https://www.youtube.com/watch?v=IJ-PJbvJBGs
* You need to have a copy of Oolite Starter unzipped in a directory of
  your choice. Just in case, you can download it from
  https://github.com/HiranChaudhuri/OoliteStarter/releases

## Configuration

Rename (or copy) the file oolite-starter.example.properties to 
oolite-starter.properties, then edit it to contain the correct settings according
to the comments in that file. If the starter cannot find the file on it's own,
you can help by setting a system property like this:

java -Doolite.starter.configuration=/path/to/oolite-starter.properties -jar Oolite-Starter.jar

## Usage

* Execute the startup script - depending on your operating system this may be
  either run.sh or run.cmd. When going for run.sh you may need to set execute
  permissions (chmod +x run.sh)
* The Starter will present you with the list of your save games. Either press
  the 'New' button to run a new game, or choose the save game and press 'Continue'.
  Either of those buttons will run Oolite the way you chose.
* Play as usual.
* If you want to change the installed expansions, switch to the 'Expansions' tab,
  check the list and choose to install, remove, enable or disable expansions.
  You can also export your currently active expansions as 'Oolite Expansion Set',
  to load it later again or share with your friends.

Happy Flying!