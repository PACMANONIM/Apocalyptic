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

package net.cyberninjapiggy.apocalyptic.misc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ZombieHelper {
  public static boolean canZombieSpawn(Location l) {
    World w = l.getWorld();
    int x = l.getBlockX();
    int y = l.getBlockY();
    int z = l.getBlockZ();
    boolean playerTooClose = false;
    for (Player p : w.getEntitiesByClass(Player.class)) {
      if (p.getLocation().distance(l) < 16) {
        playerTooClose = true;
        break;
      }
    }
    return w.getBlockAt(l).getLightLevel() <= 7 && !w.getBlockAt(x, y, z).getType().isTransparent()
        && w.getBlockAt(x, y + 1, z).getType().isTransparent()
        && w.getBlockAt(x, y + 2, z).getType().isTransparent() && !playerTooClose;
  }
}
