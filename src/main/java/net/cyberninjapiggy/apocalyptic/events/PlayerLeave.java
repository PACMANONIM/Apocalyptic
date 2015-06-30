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
import net.cyberninjapiggy.apocalyptic.misc.SavePlayerRadiationTask;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeave implements Listener {

  private final Apocalyptic plugin;

  public PlayerLeave(Apocalyptic plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerLeave(PlayerQuitEvent e) {
    if (!e.getPlayer().getMetadata(plugin.getMetadataKey()).isEmpty())
      new SavePlayerRadiationTask(plugin, e.getPlayer().getUniqueId(), e.getPlayer()
          .getMetadata(plugin.getMetadataKey()).get(0).asDouble()).runTaskAsynchronously(plugin);
  }
}
