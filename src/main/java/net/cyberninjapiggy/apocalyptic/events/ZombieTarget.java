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

package net.cyberninjapiggy.apocalyptic.events;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class ZombieTarget implements Listener {
  private final Apocalyptic plugin;

  public ZombieTarget(Apocalyptic plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onEntityTarget(EntityTargetEvent e) {
    if (!(e.getEntityType() == EntityType.ZOMBIE)
        || !(plugin.worldEnabledZombie(e.getEntity().getWorld().getName()))) {
      return;
    }
    if (e.getTarget() == null) {
      double searchRadius =
          plugin.getConfig().getWorld(e.getEntity().getWorld())
              .getDouble("mobs.zombies.targetRange") * 2;
      for (Entity ent : e.getEntity().getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
        if (ent.getType() != EntityType.ZOMBIE) {
          continue;
        }
        Zombie z = (Zombie) ent;
        if (z.getTarget() == null) {
          z.setTarget((LivingEntity) e.getTarget());
        }
      }
    } else {
      if (e.getTarget() instanceof Player) {
        Player humanEntity = (Player) e.getTarget();
        if (humanEntity.isSneaking()
            && humanEntity.getLocation().distance(e.getEntity().getLocation()) > 8) {
          e.setTarget(null);
        }
      }
    }
  }
}
