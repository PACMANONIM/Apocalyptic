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

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Messages {
  private Plugin plugin = null;

  private File configFile = null;
  private FileConfiguration config = null;

  public Messages(Plugin plugin) {
    this.plugin = plugin;
    this.configFile = new File(this.plugin.getDataFolder(), "lang.yml");

    this.reload();
    this.saveDefault();
  }

  public void reload() {
    this.config = YamlConfiguration.loadConfiguration(this.configFile);

    InputStream defaultConfigStream = this.plugin.getResource("lang.yml");
    if (defaultConfigStream != null) {
      YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultConfigStream);
      this.config.setDefaults(defaultConfig);
    }
  }

  public void saveDefault() {
    if (!this.configFile.exists()) {
      this.plugin.saveResource("lang.yml", false);
    } else {
      Map<String, Object> vals = config.getValues(true);
      configFile.delete();
      this.plugin.saveResource("lang.yml", false);
      for (String s : vals.keySet()) {
        config.set(s, vals.get(s));
      }
    }
  }

  public String getCaption(String name) {
    return this.getCaption(name, false);
  }

  public String getCaption(String name, boolean color) {
    String caption = this.config.getString(name);
    if (caption == null) {
      this.plugin.getLogger().warning("Missing caption: " + name);
      caption = "&c[missing caption]";
    }

    if (color) {
      caption = ChatColor.translateAlternateColorCodes('&', caption);
    }
    return caption;
  }
}
