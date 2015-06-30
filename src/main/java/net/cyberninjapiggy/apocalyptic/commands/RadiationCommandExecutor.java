/*
 * Copyright (C) 2015 Kaisar Arkhan 
 * Copyright (C) 2014 Nick Schatz
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
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class RadiationCommandExecutor implements CommandExecutor {
  private final Apocalyptic plugin;
  private final DecimalFormat fmt;

  public RadiationCommandExecutor(Apocalyptic plugin) {
    this.plugin = plugin;
    fmt = new DecimalFormat("0.#");
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equalsIgnoreCase("radiation")) {
      if (sender == plugin.getServer().getConsoleSender()) {
        if (args.length == 0) {
          sender.sendMessage("Cannot use this command and no arguments from console.");
        }
        if (args.length == 1) {
          if (plugin.getServer().getPlayer(args[0]).isOnline()) {
            sendRadiationMessage(
                sender,
                plugin.getRadiationManager().getPlayerRadiation(
                    plugin.getServer().getPlayer(args[0])));
          } else {
            sender.sendMessage("Cannot find player \"" + args[0] + "\"");
          }
        }
        if (args.length == 2) {
          if (plugin.getServer().getPlayer(args[0]).isOnline()) {
            if (isNumeric(args[1])) {
              sender.sendMessage("Set radiation");
              plugin.getRadiationManager().setPlayerRadiation(
                  plugin.getServer().getPlayer(args[0]), Double.parseDouble(args[1]));
            } else {
              sender.sendMessage(args[1] + " is not a valid number.");
            }
          } else {
            sender.sendMessage("Cannot find player \"" + args[0] + "\"");
          }
        }
      } else {
        if (args.length == 0 && plugin.canDoCommand(sender, "radiation.self")) {
          sendRadiationMessage(sender,
              plugin.getRadiationManager().getPlayerRadiation((Player) sender));
        }
        if (args.length == 1 && plugin.canDoCommand(sender, "radiation.other")) {
          if (plugin.getServer().getPlayer(args[0]).isOnline()) {

            sendRadiationMessage(
                sender,
                plugin.getRadiationManager().getPlayerRadiation(
                    plugin.getServer().getPlayer(args[0])));
          } else {
            sender.sendMessage("Cannot find player \"" + args[0] + "\"");
          }
        }
        if (args.length == 2 && plugin.canDoCommand(sender, "radiation.change")) {
          if (plugin.getServer().getPlayer(args[0]).isOnline()) {
            if (isNumeric(args[1])) {
              sender.sendMessage("Set radiation");
              plugin.getRadiationManager().setPlayerRadiation(
                  plugin.getServer().getPlayer(args[0]), Double.parseDouble(args[1]));
            } else {
              sender.sendMessage("" + args[1] + " is not a valid number.");
            }
          } else {
            sender.sendMessage("Cannot find player \"" + args[0] + "\"");
          }
        }
      }
      return true;
    }
    return false;
  }

  private static boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");
  }

  private void sendRadiationMessage(CommandSender s, double radiation) {
    ChatColor color = ChatColor.GREEN;

    if (radiation >= 0.8 && radiation < 1.0) {
      color = ChatColor.YELLOW;
    } else if (radiation >= 1.0 && radiation < 5.0) {
      color = ChatColor.RED;
    } else if (radiation >= 5.0 && radiation < 6.0) {
      color = ChatColor.DARK_RED;
    } else if (radiation >= 6.0 && radiation < 9.0) {
      color = ChatColor.LIGHT_PURPLE;
    } else if (radiation >= 9.0 && radiation < 10.0) {
      color = ChatColor.DARK_PURPLE;
    } else if (radiation >= 10.0) {
      color = ChatColor.BLACK;
    }

    s.sendMessage(color + "" + fmt.format(radiation) + " "
        + plugin.getMessages().getCaption("grays"));
  }
}
