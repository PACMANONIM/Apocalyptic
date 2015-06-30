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

import java.util.UUID;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;
import net.cyberninjapiggy.apocalyptic.misc.ZombieHelper;
import net.minecraft.server.v1_8_R1.AttributeInstance;
import net.minecraft.server.v1_8_R1.AttributeModifier;
import net.minecraft.server.v1_8_R1.EntityInsentient;
import net.minecraft.server.v1_8_R1.GenericAttributes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class MonsterSpawn implements Listener {
  private final Apocalyptic plugin;
  private final UUID zombieSpeedUUID = UUID.fromString("fb972eb0-b792-4ec3-b255-5740974f6eed");

  public MonsterSpawn(Apocalyptic plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntitySpawn(CreatureSpawnEvent event) {
    LivingEntity entity = event.getEntity();

    if (entity.getType() == EntityType.ZOMBIE
        && plugin.worldEnabledZombie(entity.getLocation().getWorld().getName())) {
      EntityInsentient nmsEntity = (EntityInsentient) ((CraftEntity) entity).getHandle();

      AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.d);

      AttributeModifier modifier =
          new AttributeModifier(zombieSpeedUUID, "Apocalyptic movement speed modifier", plugin
              .getConfig().getWorld(event.getEntity().getWorld())
              .getDouble("mobs.zombies.speedMultiplier"), 1);

      attributes.b(modifier);
      attributes.a(modifier);
    }
  }

  @EventHandler
  public void onMonsterSpawn(CreatureSpawnEvent e) {
    if (e.getEntityType() == EntityType.ZOMBIE
        && plugin.worldEnabledZombie(e.getLocation().getWorld().getName())) {
      if (e.getEntity().getWorld().getEntitiesByClass(Zombie.class).size() >= plugin.getConfig()
          .getWorld(e.getLocation().getWorld()).getInt("mobs.zombies.spawnLimit")) {
        e.setCancelled(true);
        return;
      }

      Location l = e.getLocation();

      if (plugin.getRandom().nextInt(300) == 0
          && plugin.getConfig().getWorld(e.getLocation().getWorld())
              .getBoolean("mobs.mutants.zombie")) {
        e.setCancelled(true);
        l.getWorld().spawnEntity(l, EntityType.GIANT);
        return;
      }

      e.getEntity().setMaxHealth(
          plugin.getConfig().getWorld(e.getEntity().getWorld()).getDouble("mobs.zombies.health"));
      e.getEntity().setHealth(
          plugin.getConfig().getWorld(e.getEntity().getWorld()).getDouble("mobs.zombies.health"));

      if (e.getSpawnReason() != SpawnReason.CUSTOM && e.getSpawnReason() != SpawnReason.SPAWNER) {
        int hordeSize =
            plugin.getRandom().nextInt(
                plugin.getConfig().getWorld(e.getEntity().getWorld())
                    .getInt("mobs.zombies.hordeSize.max")
                    - plugin.getConfig().getWorld(e.getEntity().getWorld())
                        .getInt("mobs.zombies.hordeSize.min"))
                + plugin.getConfig().getWorld(e.getEntity().getWorld())
                    .getInt("mobs.zombies.hordeSize.min");
        int failedAttempts = 0;
        for (int i = 0; i < hordeSize;) {
          double angle = Math.random() * 360;
          double radius = Math.random() * 255;

          int addX = Math.round((float) Math.cos(angle));
          int addZ = Math.round((float) (Math.sin(angle) * radius));

          Location spawnPoint = l.clone().add(addX, 0, addZ);

          spawnPoint.setY(l.getWorld().getHighestBlockYAt(spawnPoint));

          if (!ZombieHelper.canZombieSpawn(spawnPoint) && failedAttempts <= 10) {
            failedAttempts++;
            continue;
          }

          failedAttempts = 0;
          Zombie zombie = (Zombie) l.getWorld().spawnEntity(spawnPoint, EntityType.ZOMBIE);
          EntityEquipment equipment = zombie.getEquipment();
          if (equipment.getHelmet() != null
              && !zombie.isBaby()
              && !plugin.getConfig().getWorld(zombie.getWorld())
                  .getBoolean("mobs.zombies.burnInDaylight")) {
            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 2);
            equipment.setHelmet(head);
            equipment.setHelmetDropChance(0f);
          }

          i++;
        }
      }

    }

    if (e.getEntityType() == EntityType.CREEPER) {
      if (plugin.getConfig().getBoolean(
          "worlds." + e.getLocation().getWorld().getName() + ".mobs.mutants.creeper")) {
        if (plugin.getRandom().nextInt(100) == 0) {
          ((Creeper) e.getEntity()).setPowered(true);
          return;
        }
      }
    }

    if (e.getEntityType() == EntityType.SKELETON) {
      if (plugin.getConfig().getBoolean(
          "worlds." + e.getLocation().getWorld().getName() + ".mobs.mutants.skeleton")) {
        if (plugin.getRandom().nextInt(100) == 0) {
          ((Skeleton) e.getEntity()).setSkeletonType(Skeleton.SkeletonType.WITHER);
          e.getEntity().getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD, 0));
        }
      }
    }
  }

}
