/*
 * Copyright (C) 2015 Kaisar Arkhan Copyright (C) 2014 Nick Schatz
 * 
 * This file is part of Apocalyptic.
 * 
 * Apocalyptic is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Apocalyptic is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Apocalyptic. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package net.cyberninjapiggy.apocalyptic.commands;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ApocalypticCommandExecutor implements CommandExecutor {
  private final Apocalyptic plugin;
  private final String[] radiationHelp;

  public ApocalypticCommandExecutor(Apocalyptic plugin) {
    this.plugin = plugin;
    radiationHelp =
        new String[] {
            ChatColor.DARK_BLUE + " ======= " + ChatColor.AQUA + "RADIATION" + ChatColor.DARK_BLUE
                + " ======= ",
            "Radioactive gamma and beta particles permeate the world,",
            "damaging your skin and destroying your brain.",
            "Check your radiation with the command \"/radiation\".\n",
            ChatColor.RED + "Harmful effects of Radiation:",
            "If you have a radiation level above 1 Gray, you will take",
            "twice as much damage.",
            "If you have a radiation level above 5 Grays, you will not",
            "be able to eat, and instead barf up anything you attempt to.",
            "If you have a radiation level above 10 Grays, your brain will",
            "start dying.",
            "You can wash off your radiation by standing in water.",
            "If you have more than " + plugin.getConfig().getString("maxRadiationWashable")
                + " Grays, you will not be able to wash and", "require a cure instead."};
  }

  @Override
  public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
    if (label.equals("apocalyptic") && args.length == 1) {
      switch (args[0]) {
        case "reload":
          if (!plugin.canDoCommand(cs, "apocalyptic.reload")) {
            cs.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }

          plugin.reloadConfig();
          plugin.getMessages().reload();
          cs.sendMessage("Config & Language reloaded.");

          return true;
        case "stop":
          if (!plugin.canDoCommand(cs, "apocalyptic.stop")) {
            cs.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }

          cs.sendMessage("Stopping plugin...");
          plugin.getServer().getPluginManager().disablePlugin(plugin);

          return true;
        case "radiation":
          if (!plugin.canDoCommand(cs, "apocalyptic.radhelp")) {
            cs.sendMessage(ChatColor.RED + "You don't have permission to do that!");
            return true;
          }

          cs.sendMessage(radiationHelp);

          return true;
      }
    }

    return false;
  }

}
