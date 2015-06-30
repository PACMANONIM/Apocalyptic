# Apocalyptic [![Build Status](https://drone.io/github.com/captainfroster/Apocalyptic/status.png)](https://drone.io/github.com/captainfroster/Apocalyptic/latest)

Apocalyptic is a Bukkit Plugin that adds Nuclear / Zombie Post-Apocalypse elements to your server.

## Downloads / Releases
You can download a Development Build of this Project from [here](https://drone.io/github.com/captainfroster/Apocalyptic/files)

## Installation

Download Apocalyptic.jar and add to your /plugins/ folder.
If you want world generation, add this snippet to the bottom of bukkit.yml.
```
worlds:
  <world>:
    generator: Apocalyptic
```
Where `<world>` is the name of the world you want to generate.

If you use Multiverse, run this command to make a new Apocalyptic world.

`/mv create <world> normal -g Apocalyptic`

Again, where `<world>` is the name of the world you want to generate.

Run the server. This will generate some of the world and create config files.

Done! Have fun!

## To-do List
- [x] Make it Compatible with 1.8
- [X] Use PreparedStatements instead of Plain Query in Database Updates
- [X] Add support for External Database ( MySQL )
- [X] Clean the Code ( again )
- [X] Better Zombie spawn picking
- [ ] Make Zombie Spawn Mechanic doesn't affect Main Server Thread to gain performance
- [ ] Fix Zombie Spawn checker or Fix Zombie spawn picking?
- [ ] Natural spawning for sugarcane and cactus

## Notes
>I won't create a BukkitDev / Curse's Minecraft Bukkit Plugin Page, because I don't have the time to update. If I do, The Page will most likely abandoned or lack of updates.

>This is a forked version of [epicfacecreeper's Apocalyptic](https://github.com/epicfacecreeper/Apocalyptic). I created this fork because the original project looks like It's not being maintained ( Last Commit Aug 11, 2014 ), because My Server is using this plugin I decided to modify this plugin to be 1.8 Compatible.
