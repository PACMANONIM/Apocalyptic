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

package net.cyberninjapiggy.apocalyptic.events;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMove implements Listener {
  private final Apocalyptic plugin;

  public PlayerMove(Apocalyptic plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent e) {
    if (plugin.worldEnabledFallout(e.getPlayer().getWorld().getName())) {
      if (e.getTo().getWorld().getBlockAt(e.getTo()).getType() == Material.WATER
          || e.getTo().getWorld().getBlockAt(e.getTo()).getType() == Material.STATIONARY_WATER) {
        if (plugin.getConfig().getDouble("maxRadiationWashable") >= plugin.getRadiationManager()
            .getPlayerRadiation(e.getPlayer()))
          plugin.getRadiationManager().setPlayerRadiation(e.getPlayer(), 0.0);
      }
    }
  }

}
