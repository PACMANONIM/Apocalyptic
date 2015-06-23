/*
 * Copyright (C) 2015 Kaisar Arkhan
 * Copyright (C) 2014 Nick Schatz
 *
 *     This file is part of Apocalyptic.
 *
 *     Apocalyptic is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Apocalyptic is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Apocalyptic.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.cyberninjapiggy.apocalyptic.commands;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HazmatCommandExecutor implements CommandExecutor {

	private Apocalyptic plugin;

	public HazmatCommandExecutor(Apocalyptic plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command cmd, String label, String[] args) {
		if (label.equals("hazmat")) {
			if (args.length == 0) {
				return false;
			} else if (args.length == 1) {
				if (commandSender instanceof Player) {
					if (plugin.canDoCommand(commandSender, "hazmatArmor.self")) {
						switch (args[0]) {
							case "helmet":
								((Player) commandSender).getInventory().addItem(plugin.getHazmatHood());
								return true;
							case "chest":
								((Player) commandSender).getInventory().addItem(plugin.getHazmatSuit());
								return true;
							case "pants":
								((Player) commandSender).getInventory().addItem(plugin.getHazmatPants());
								return true;
							case "boots":
								((Player) commandSender).getInventory().addItem(plugin.getHazmatBoots());
								return true;
							default:
								return false;
						}
					} else {
						commandSender.sendMessage(ChatColor.RED + "You don't have permission.");
					}
				} else {
					commandSender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
				}
			} else if (args.length == 2) {
				if (plugin.canDoCommand(commandSender, "hazmatArmor.other")) {
					Player player;
					if ((player = Bukkit.getPlayer(args[1])) == null) {
						commandSender.sendMessage("Cannot find that player.");
						return true;
					}
					switch (args[0]) {
						case "helmet":
							player.getInventory().addItem(plugin.getHazmatHood());
							return true;
						case "chest":
							player.getInventory().addItem(plugin.getHazmatSuit());
							return true;
						case "pants":
							player.getInventory().addItem(plugin.getHazmatPants());
							return true;
						case "boots":
							player.getInventory().addItem(plugin.getHazmatBoots());
							return true;
						default:
							return false;
					}
				} else {
					commandSender.sendMessage(ChatColor.RED + "You don't have permission.");
				}

			}
		}

		return false;
	}
}