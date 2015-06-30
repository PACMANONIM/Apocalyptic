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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import lib.PatPeter.SQLibrary.Database;
import net.cyberninjapiggy.apocalyptic.Apocalyptic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class RadiationManager {
  private final Database db;
  private final Apocalyptic plugin;

  public RadiationManager(Database db, Apocalyptic plugin) {
    this.db = db;
    this.plugin = plugin;
  }

  public void saveRadiation(Player p) throws SQLException {
    db.open();
    if (!p.getMetadata(plugin.getMetadataKey()).isEmpty()) {
      if (db
          .query(
              "SELECT COUNT(*) AS \"exists\" FROM " + plugin.getTablePrefix() + "radiationLevels WHERE player=\"" + p.getUniqueId() + "\";").getInt("exists") > 0) { //$NON-NLS-3$
        PreparedStatement ps =
            db.prepare("UPDATE " + plugin.getTablePrefix()
                + "radiationLevels SET level=? WHERE player=?;");

        ps.setDouble(1, p.getMetadata(plugin.getMetadataKey()).get(0).asDouble());
        ps.setString(2, p.getUniqueId().toString());

        ps.executeUpdate();
      } else {
        PreparedStatement ps =
            db.prepare("INSERT INTO " + plugin.getTablePrefix()
                + "radiationLevels (player, level) VALUES (?,?)");

        ps.setString(1, p.getUniqueId().toString());
        ps.setDouble(2, p.getMetadata(plugin.getMetadataKey()).get(0).asDouble());

        ps.executeUpdate();
      }
    }
    db.close();
  }

  public void saveRadiation(UUID id, double value) throws SQLException {
    Player p = Bukkit.getPlayer(id);

    db.open();

    if (db
        .query(
            "SELECT COUNT(*) AS \"exists\" FROM " + plugin.getTablePrefix() + "radiationLevels WHERE player=\"" + id + "\";").getInt("exists") > 0) { //$NON-NLS-3$
      PreparedStatement ps =
          db.prepare("UPDATE " + plugin.getTablePrefix()
              + "radiationLevels SET level=? WHERE player=?;");

      ps.setDouble(1, value);
      ps.setString(2, id.toString());

      ps.executeUpdate();
    } else {
      PreparedStatement ps =
          db.prepare("INSERT INTO " + plugin.getTablePrefix()
              + "radiationLevels (player, level) VALUES (?,?)");

      ps.setString(1, id.toString());
      ps.setDouble(2, value);

      ps.executeUpdate();
    }
    
    db.close();
  }

  public void loadRadiation(Player p) {
    db.open();
    ResultSet result;
    try {
      result =
          db.query("SELECT * FROM " + plugin.getTablePrefix() + "radiationLevels WHERE player=\""
              + p.getUniqueId() + "\"");
      while (result.next()) {
        p.setMetadata(plugin.getMetadataKey(),
            new FixedMetadataValue(plugin, result.getDouble("level")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    db.close();
  }

  public void loadRadiation(UUID id) {
    Player p = Bukkit.getPlayer(id);

    db.open();
    ResultSet result;
    try {
      result =
          db.query("SELECT * FROM " + plugin.getTablePrefix() + "radiationLevels WHERE player=\""
              + id + "\"");
      while (result.next()) {
        p.setMetadata(plugin.getMetadataKey(),
            new FixedMetadataValue(plugin, result.getDouble("level")));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    db.close();
  }

  /**
   *
   * @param p the player which to add radiation to
   * @param level the amount of radiation (in grays) to add to the player
   */
  public void addPlayerRadiation(Player p, double level) {

    // p.setMetadata(apocalyptic.getMetadataKey(), new
    // FixedMetadataValue(p.getMetadata(apocalyptic.getMetadataKey()).g));
    double oldRadiation = 0;
    if (p.getMetadata(plugin.getMetadataKey()).size() > 0) {
      oldRadiation = p.getMetadata(plugin.getMetadataKey()).get(0).asDouble();
      p.setMetadata(plugin.getMetadataKey(), new FixedMetadataValue(plugin, oldRadiation + level));
    } else {
      p.setMetadata(plugin.getMetadataKey(), new FixedMetadataValue(plugin, level));
    }

    if (getPlayerRadiation(p) >= 0.8 && getPlayerRadiation(p) < 1.0) {
      p.sendMessage(new String[] {
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radiationCriticalWarning"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radBloodWarning")});
    }
    if (oldRadiation < 1.0 && getPlayerRadiation(p) >= 1.0 && getPlayerRadiation(p) < 6.0) {
      p.sendMessage(new String[] {
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radDangerLevel"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radBlood"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("takemoredamage")});
    }
    if (oldRadiation < 6.0 && getPlayerRadiation(p) >= 6.0 && getPlayerRadiation(p) < 10.0) {
      p.sendMessage(new String[] {
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radiationCritical"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radBloodStomach"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("takeMoreDamageandNoEat")});
    }
    if (oldRadiation < 10.0 && getPlayerRadiation(p) >= 10) {
      p.sendMessage(new String[] {
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radDeadly"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radAll"),
          ChatColor.RED + plugin.getMessages().getCaption("warning") + " " + ChatColor.GOLD
              + plugin.getMessages().getCaption("radAllExplain")});
    }

  }

  /**
   *
   * @param p the player
   * @return the radiation level (in grays) of the specified player
   */
  public double getPlayerRadiation(Player p) {
    if (p.getMetadata(plugin.getMetadataKey()).size() > 0) {
      return p.getMetadata(plugin.getMetadataKey()).get(0).asDouble();
    }
    return 0;
  }

  /**
   *
   * @param p the player which to set the radiation level of
   * @param radiation the level of radiation (in grays) that the player is set to
   */
  public void setPlayerRadiation(Player p, double radiation) {
    addPlayerRadiation(p, getPlayerRadiation(p) * -1);
    addPlayerRadiation(p, radiation);
  }
}
