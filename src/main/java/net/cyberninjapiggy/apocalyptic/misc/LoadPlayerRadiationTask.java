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

package net.cyberninjapiggy.apocalyptic.misc;

import java.util.UUID;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;

import org.bukkit.scheduler.BukkitRunnable;

public class LoadPlayerRadiationTask extends BukkitRunnable {

  final Apocalyptic plugin;
  final UUID id;

  public LoadPlayerRadiationTask(Apocalyptic plugin, UUID id) {
    this.plugin = plugin;
    this.id = id;
  }

  @Override
  public void run() {
    plugin.getRadiationManager().loadRadiation(id);
  }

}
