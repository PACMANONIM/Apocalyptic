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

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerDamaged implements Listener {
  private final Apocalyptic plugin;

  public PlayerDamaged(Apocalyptic plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerDamagedByEntity(EntityDamageByEntityEvent e) {
    if (e.getEntityType() == EntityType.PLAYER) {
      if (plugin.worldEnabledZombie(e.getEntity().getWorld().getName())
          && e.getDamager().getType() == EntityType.ZOMBIE) {
        e.setDamage(e.getDamage()
            * plugin.getConfig().getWorld(e.getEntity().getWorld())
                .getInt("mobs.zombies.damageMultiplier"));

        if (plugin.getConfig().getWorld(e.getEntity().getWorld())
            .getBoolean("mobs.zombies.effects.hunger")) {
          ((Player) e.getEntity())
              .addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 1));
        }

        if (plugin.getConfig().getWorld(e.getEntity().getWorld())
            .getBoolean("mobs.zombies.effects.weakness")) {
          ((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40,
              1));
        }

        if (plugin.getConfig().getWorld(e.getEntity().getWorld())
            .getBoolean("mobs.zombies.effects.slowness")) {
          ((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
        }

        if (plugin.getConfig().getWorld(e.getEntity().getWorld())
            .getBoolean("mobs.zombies.effects.nausea")) {
          ((Player) e.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 40,
              1));
        }
      }
    }
  }

  @EventHandler
  public void onPlayerDamaged(EntityDamageEvent e) {
    if (e.getEntityType() == EntityType.PLAYER) {
      if (plugin.worldEnabledFallout(e.getEntity().getWorld().getName())) {

        if (plugin.getRadiationManager().getPlayerRadiation((Player) e.getEntity()) >= 10) {
          e.setDamage(e.getDamage() * 4);
        } else if (plugin.getRadiationManager().getPlayerRadiation((Player) e.getEntity()) >= 1) {
          e.setDamage(e.getDamage() * 2);
        }
      }
    }
  }

}
