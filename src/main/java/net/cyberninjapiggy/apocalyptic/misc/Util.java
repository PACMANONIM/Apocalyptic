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

package net.cyberninjapiggy.apocalyptic.misc;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Util {
    public static String title(String str) {
        String[] strs = new String[str.split(" ").length];
        int i=0;
        for (String s : str.split(" ")) {
            strs[i] = s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
            i++;
        }
        String titled = "";
        for (int j=0;j<strs.length;j++) {
            titled += strs[j];
            if (j != strs.length-1) {
                titled += " ";
            }
        }
        return titled;
    }
    
    public static ItemStack setName(ItemStack is, String name){
        ItemMeta m = is.getItemMeta();
        m.setDisplayName(name);
        is.setItemMeta(m);
        return is;
    }
    
    public static void damageWithCause(Player p, String damager, int damage){
        LivingEntity e = p.getWorld().spawn(p.getLocation().add(0,-15,0), Squid.class);
        e.setCustomNameVisible(true);
        e.setCustomName(ChatColor.translateAlternateColorCodes('&', damager));
        e.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
        p.damage(damage, e);
        e.remove();
    }
    
    public static double getBiomeTemp(Location l) {
        int x = l.getBlockX();
        int z = l.getBlockZ();

        return l.getWorld().getTemperature(x, z);
    }
}
