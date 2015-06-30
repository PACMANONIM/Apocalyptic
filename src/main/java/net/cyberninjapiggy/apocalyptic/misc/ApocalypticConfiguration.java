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

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;

/**
 * Wrapper class for YamlConfiguration. Provides Apocalyptic-specific services such as getting the
 * world configuration section.
 */
public class ApocalypticConfiguration extends YamlConfiguration {
  public void update(JavaPlugin plugin) {
    Map<String, Object> vals = this.getValues(true);
    new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "config.yml").delete();
    plugin.saveDefaultConfig();
    for (String s : vals.keySet()) {
      if (s.equals("meta.version")) {
        continue;
      }
      if (!this.contains(s))
        this.createSection(s);
      this.set(s, vals.get(s));
    }
  }

  public ConfigurationSection getWorld(String world) {
    return this.getConfigurationSection("worlds." + world);
  }

  public ConfigurationSection getWorld(World world) {
    return getWorld(world.getName());
  }
}
