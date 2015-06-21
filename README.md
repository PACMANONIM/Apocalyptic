# Apocalyptic


Apocalyptic plugin for Bukkit

>This is a forked version of [epicfacecreeper's Apocalyptic](https://github.com/epicfacecreeper/Apocalyptic). I created this fork because the original project looks like It's not being maintained ( Last Commit Aug 11, 2014 ), because My Server is using this plugin I decided to modify this plugin to be 1.8 Compatible.

[Original Project page and downloads](http://dev.bukkit.org/bukkit-plugins/apocalyptic/)

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
