/*
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

package net.cyberninjapiggy.apocalyptic.events;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;
import net.cyberninjapiggy.apocalyptic.misc.ZombieHelper;
import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 *
 * @author Nick
 */
public class MonsterSpawn implements Listener {
    private final Apocalyptic a;
    private final UUID zombieSpeedUUID = UUID.fromString("fb972eb0-b792-4ec3-b255-5740974f6eed");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event){
        LivingEntity entity = event.getEntity();

        if (entity.getType() == EntityType.ZOMBIE && a.worldEnabledZombie(entity.getLocation().getWorld().getName())){
            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
            EntityZombie zombie = (EntityZombie) nmsEntity;

            AttributeInstance attributes = nmsEntity.getAttributeInstance(GenericAttributes.d);

            AttributeModifier modifier = new AttributeModifier(zombieSpeedUUID, "Apocalyptic movement speed modifier", a.getConfig().getWorld(event.getEntity().getWorld()).getDouble("mobs.zombies.speedMultiplier"), 1);

            attributes.b(modifier);
            attributes.a(modifier);
        }
    }

    @EventHandler
    public void onMonsterSpawn(CreatureSpawnEvent e) {
        
        if (e.getEntityType() == EntityType.ZOMBIE && a.worldEnabledZombie(e.getLocation().getWorld().getName())) {
        	if (e.getEntity().getWorld().getEntitiesByClass(Zombie.class).size() >= 
        			a.getConfig().getWorld(e.getLocation().getWorld()).getInt("mobs.zombies.spawnLimit")) {
        		e.setCancelled(true);
        		return;
        	}
            
            Location l = e.getLocation();
            if (a.getRandom().nextInt(300) == 0 && a.getConfig().getWorld(e.getLocation().getWorld()).getBoolean("mobs.mutants.zombie")) {
                e.setCancelled(true);
                l.getWorld().spawnEntity(l, EntityType.GIANT);
                return;
            }
            e.getEntity().setMaxHealth(a.getConfig().getWorld(e.getEntity().getWorld()).getDouble("mobs.zombies.health"));
            e.getEntity().setHealth(a.getConfig().getWorld(e.getEntity().getWorld()).getDouble("mobs.zombies.health"));
            
            if (e.getSpawnReason() != SpawnReason.CUSTOM && e.getSpawnReason() != SpawnReason.SPAWNER) {
                int hordeSize = a.getRandom().nextInt(
                        a.getConfig().getWorld(e.getEntity().getWorld()).getInt("mobs.zombies.hordeSize.max") - 
                        a.getConfig().getWorld(e.getEntity().getWorld()).getInt("mobs.zombies.hordeSize.min")) + 
                        a.getConfig().getWorld(e.getEntity().getWorld()).getInt("mobs.zombies.hordeSize.min");
                int failedAttempts = 0;
                for (int i=0;i<hordeSize;) {
                    // TODO make point selection better
                    int spotX = 7-a.getRandom().nextInt(14);
                    int spotZ = 7-a.getRandom().nextInt(14);
                    //int spotY = 3-a.getRandom().nextInt(6);
                    Location spawnPoint = l.add(spotX, 0/*spotY*/, spotZ);
                    spawnPoint.setY(l.getWorld().getHighestBlockYAt(spotX, spotZ));
                    if (!ZombieHelper.canZombieSpawn(spawnPoint) && failedAttempts <= 10) {
                    	failedAttempts++;
                    	continue;
                    }
                    failedAttempts = 0;
                    Zombie zombie = (Zombie) l.getWorld().spawnEntity(spawnPoint, EntityType.ZOMBIE);
                    EntityEquipment equipment = zombie.getEquipment();
                    if (equipment.getHelmet() != null && !zombie.isBaby() && !a.getConfig().getWorld(zombie.getWorld()).getBoolean("mobs.zombies.burnInDaylight")) {
                        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte)2);
                        equipment.setHelmet(head);
                        equipment.setHelmetDropChance(0f);
                    }
                    i++;
                    
                }
            }
            
        }
        if (e.getEntityType() == EntityType.CREEPER) {
            if (a.getConfig().getBoolean("worlds." + e.getLocation().getWorld().getName() + ".mobs.mutants.creeper")) {
                if (a.getRandom().nextInt(100) == 0) {
                    ((Creeper) e.getEntity()).setPowered(true);
                    return;
                }
            }
        }
        if (e.getEntityType() == EntityType.SKELETON) {
            if (a.getConfig().getBoolean("worlds." + e.getLocation().getWorld().getName() + ".mobs.mutants.skeleton")) {
                if (a.getRandom().nextInt(100) == 0) {
                    ((Skeleton) e.getEntity()).setSkeletonType(Skeleton.SkeletonType.WITHER);
                    e.getEntity().getEquipment().setItemInHand(new ItemStack(Material.IRON_SWORD, 0));
                }
            }
        }
    }
    public MonsterSpawn(Apocalyptic a) {
        this.a = a;
    }
}
