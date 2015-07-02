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

import java.util.ArrayList;
import java.util.Random;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

// TODO: Change Name?
public class EssentialRepeatingTask implements Runnable {

  final Apocalyptic plugin;
  final Random rand;
  
  public EssentialRepeatingTask(Apocalyptic plugin){
    this.plugin = plugin;
    rand = new Random();
  }
  
  @Override
  public void run() {
    for (World w : plugin.getServer().getWorlds()) {
      Object regions;
      for (Player p : w.getPlayers()) {
        boolean noFallout = false;
        boolean forceFallout = false;
        if (plugin.isWorldGuardEnabled()) {
          regions =
              ((WorldGuardPlugin) plugin.getWorldGuard()).getRegionManager(w).getApplicableRegions(p.getLocation());
          for (ProtectedRegion next : ((ApplicableRegionSet) regions)) {
            for (String s : plugin.getConfig().getStringList("regions.fallout")) {
              if (next.getId().equals(s)) {
                forceFallout = true;
                break;
              }
            }
            for (String s : plugin.getConfig().getStringList("regions.noFallout")) {
              if (next.getId().equals(s)) {
                noFallout = true;
                break;
              }
            }
          }
        }
        if (!noFallout && (plugin.worldEnabledFallout(w.getName()) || forceFallout)) {
          // Acid Rain
          Location l = p.getLocation();
          double temp = Util.getBiomeTemp(l);
          if (p.getEquipment().getHelmet() == null
              && p.getWorld().getHighestBlockYAt(l.getBlockX(), l.getBlockZ()) <= l.getBlockY()
              && p.getWorld().hasStorm() && temp < 1.0D) {
            Util.damageWithCause(p, plugin.getMessages().getCaption("acidRain"), p.getWorld()
                .getDifficulty().ordinal() * 2);
          }
          // Neurological death syndrome
          if (plugin.getRadiationManager().getPlayerRadiation(p) >= 10.0D) {
            ArrayList<PotionEffect> pfx = new ArrayList<>();
            pfx.add(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 2));
            pfx.add(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 2));
            pfx.add(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 2));
            pfx.add(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10 * 20, 2));
            pfx.add(new PotionEffect(PotionEffectType.WEAKNESS, 10 * 20, 2));
            p.addPotionEffects(pfx);
          }
          // Add radiation
          boolean hazmatSuit = plugin.isPlayerWearingHazmatSuit(p);
          boolean aboveLowPoint =
              p.getLocation().getBlockY() > plugin.getConfig().getWorld(w).getInt("radiationBottom");
          boolean belowHighPoint =
              p.getLocation().getBlockY() < plugin.getConfig().getWorld(w).getInt("radiationTop");
          boolean random = rand.nextInt(4) == 0;
          if (!hazmatSuit && aboveLowPoint && belowHighPoint && random) {
            plugin.getRadiationManager().addPlayerRadiation(
                p,
                (p.getWorld().getEnvironment() == Environment.NETHER ? plugin.getConfig().getWorld(w)
                    .getDouble("radiationRate") * 2 : plugin.getConfig().getWorld(w).getDouble(
                    "radiationRate"))
                    * (Math.round(p.getLevel() / 10) + 1));
          }
        }
      }
    }
  }

}
