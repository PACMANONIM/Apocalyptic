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

package net.cyberninjapiggy.apocalyptic;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import lib.PatPeter.SQLibrary.Database;
import lib.PatPeter.SQLibrary.MySQL;
import lib.PatPeter.SQLibrary.SQLite;
import net.cyberninjapiggy.apocalyptic.commands.ApocalypticCommandExecutor;
import net.cyberninjapiggy.apocalyptic.commands.HazmatCommandExecutor;
import net.cyberninjapiggy.apocalyptic.commands.RadiationCommandExecutor;
import net.cyberninjapiggy.apocalyptic.events.MonsterSpawn;
import net.cyberninjapiggy.apocalyptic.events.PlayerChangeWorld;
import net.cyberninjapiggy.apocalyptic.events.PlayerDamaged;
import net.cyberninjapiggy.apocalyptic.events.PlayerEat;
import net.cyberninjapiggy.apocalyptic.events.PlayerJoin;
import net.cyberninjapiggy.apocalyptic.events.PlayerLeave;
import net.cyberninjapiggy.apocalyptic.events.PlayerMove;
import net.cyberninjapiggy.apocalyptic.events.PlayerSpawn;
import net.cyberninjapiggy.apocalyptic.events.ZombieCombust;
import net.cyberninjapiggy.apocalyptic.events.ZombieTarget;
import net.cyberninjapiggy.apocalyptic.generator.RavagedChunkGenerator;
import net.cyberninjapiggy.apocalyptic.misc.ApocalypticConfiguration;
import net.cyberninjapiggy.apocalyptic.misc.Messages;
import net.cyberninjapiggy.apocalyptic.misc.RadiationManager;
import net.cyberninjapiggy.apocalyptic.misc.UUIDFetcher;
import net.cyberninjapiggy.apocalyptic.misc.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public final class Apocalyptic extends JavaPlugin {
  private Logger log;
  private Database db;
  private Random rand;
  private Plugin wg;
  private boolean wgEnabled = true;
  private Messages messages;

  // private static final int dboId = 43663;
  private static final String texturePack =
      "http://www.curseforge.com/media/files/769/14/apocalyptic_texture_pack.zip";

  private static final String METADATA_KEY = "radiation";

  private ItemStack hazmatHood;
  private ItemStack hazmatSuit;
  private ItemStack hazmatPants;
  private ItemStack hazmatBoots;

  private RadiationManager radiationManager;

  private ApocalypticConfiguration cachedConfig;
  private boolean recacheConfig;

  private String tablePrefix;

  @Override
  public void onEnable() {
    messages = new Messages(this);

    log = getLogger();
    rand = new Random();
    wg = getWorldGuard();

    hazmatHood =
        Util.setName(new ItemStack(Material.CHAINMAIL_HELMET, 1), ChatColor.RESET
            + getMessages().getCaption("gasMask"));
    hazmatSuit =
        Util.setName(new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1), ChatColor.RESET
            + getMessages().getCaption("hazmatSuit"));
    hazmatPants =
        Util.setName(new ItemStack(Material.CHAINMAIL_LEGGINGS, 1), ChatColor.RESET
            + getMessages().getCaption("hazmatPants"));
    hazmatBoots =
        Util.setName(new ItemStack(Material.CHAINMAIL_BOOTS, 1), ChatColor.RESET
            + getMessages().getCaption("hazmatBoots"));

    if (wg == null) {
      wgEnabled = false;
    }

    if (!getDataFolder().exists()) {
      if (!getDataFolder().mkdir()) {
        log.severe("Cannot create data folder. Expect terrible errors.");
      }
    }
    messages.saveDefault();
    if (!new File(getDataFolder().getPath() + File.separator + "config.yml").exists()) {
      saveDefaultConfig();
    } else {
      if (!getConfig().getString("meta.version").equals(this.getDescription().getVersion())) {
        // getConfig().update(this);
      }
    }

    if (getConfig().getBoolean("mysql.enable", false)) {
      db =
          new MySQL(log, getMessages().getCaption("logtitle"), getConfig().getString("mysql.host"),
              getConfig().getInt("mysql.port"), getConfig().getString("mysql.database"),
              getConfig().getString("mysql.username"), getConfig().getString("mysql.password"));
      tablePrefix = getConfig().getString("mysql.tablePrefix");

    } else {
      db =
          new SQLite(log, getMessages().getCaption("logtitle"), getDataFolder().getAbsolutePath(),
              "apocalyptic");
      tablePrefix = "";
    }

    if (!db.open()) {
      log.severe(getMessages().getCaption("errNotOpenDatabase"));
      this.setEnabled(false);
      return;
    }

    try {
      if (db.isTable(tablePrefix + "radiationLevels")) {
        ResultSet resultSet = db.query("SELECT * FROM " + tablePrefix + "radiationLevels");
        Map<String, Double> toUpdate = new HashMap<>();
        while (resultSet.next()) {
          String player = resultSet.getString("player");
          double level = resultSet.getDouble("level");
          if (player.length() <= 16) {
            toUpdate.put(player, level);
          }

        }

        if (toUpdate.size() > 0) {
          log.info("Apocalyptic is converting your database. Please stand by...");

          db.query("DROP TABLE radiationLevels");
          db.query("CREATE TABLE " + tablePrefix + "radiationLevels ("
              + "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + "player VARCHAR(36),"
              + "level DOUBLE);");

          UUIDFetcher fetcher = new UUIDFetcher(toUpdate.keySet());
          Map<String, UUID> uuidMap = fetcher.call();

          for (Map.Entry<String, UUID> entry : uuidMap.entrySet()) {
            db.query("INSERT INTO " + tablePrefix + "radiationLevels (player, level) VALUES (\""
                + entry.getValue() + "\", \"" + toUpdate.get(entry.getKey()) + "\");");
          }
        }
      } else {
        db.query("CREATE TABLE " + tablePrefix + "radiationLevels ("
            + "id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," + "player VARCHAR(36),"
            + "level DOUBLE);");
      }

    } catch (SQLException ex) {
      log.log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      log.severe("Exception fetching UUIDs");
      log.log(Level.SEVERE, null, e);
    }
    db.close();

    radiationManager = new RadiationManager(db, this);

    // Disabled because this is a Forked version
    /*
     * if (getConfig().getBoolean("meta.version-check")) { Updater versionCheck = new Updater(this,
     * dboId, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false); if (versionCheck.getResult()
     * != Updater.UpdateResult.DISABLED &&
     * !versionCheck.getLatestName().equals(this.getDescription().getName() + " v" +
     * this.getDescription().getVersion())) { if (getConfig().getBoolean("meta.auto-update")) { new
     * Updater(this, dboId, this.getFile(), Updater.UpdateType.NO_VERSION_CHECK,
     * getConfig().getBoolean("meta.show-download-progress")); } else { log.info(ChatColor.GREEN +
     * getMessages().getCaption("updateAvaliable") + ": " + versionCheck.getLatestName());
     * //$NON-NLS-3$ } } }
     */

    // CommandExecutors
    getCommand("radiation").setExecutor(new RadiationCommandExecutor(this));
    getCommand("apocalyptic").setExecutor(new ApocalypticCommandExecutor(this));
    getCommand("hazmat").setExecutor(new HazmatCommandExecutor(this));

    // Register Listeners
    getServer().getPluginManager().registerEvents(new PlayerSpawn(this), this);
    getServer().getPluginManager().registerEvents(new MonsterSpawn(this), this);
    getServer().getPluginManager().registerEvents(new PlayerEat(this), this);
    getServer().getPluginManager().registerEvents(new PlayerDamaged(this), this);
    getServer().getPluginManager().registerEvents(new PlayerMove(this), this);
    getServer().getPluginManager().registerEvents(new PlayerChangeWorld(this), this);
    getServer().getPluginManager().registerEvents(new PlayerLeave(this), this);
    getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
    getServer().getPluginManager().registerEvents(new ZombieTarget(this), this);
    getServer().getPluginManager().registerEvents(new ZombieCombust(this), this);

    // Add recipes
    ShapedRecipe hazardHelmetR = new ShapedRecipe(hazmatHood);
    hazardHelmetR.shape("SSS", "S S");
    hazardHelmetR.setIngredient('S', Material.SPONGE);

    ShapedRecipe hazardChestR = new ShapedRecipe(hazmatSuit);
    hazardChestR.shape("S S", "SSS", "SSS"); //$NON-NLS-3$
    hazardChestR.setIngredient('S', Material.SPONGE);

    ShapedRecipe hazardPantsR = new ShapedRecipe(hazmatPants);
    hazardPantsR.shape("SSS", "S S", "S S"); //$NON-NLS-3$
    hazardPantsR.setIngredient('S', Material.SPONGE);

    ShapedRecipe hazardBootsR = new ShapedRecipe(hazmatBoots);
    hazardBootsR.shape("S S", "S S");
    hazardBootsR.setIngredient('S', Material.SPONGE);
    int start = 306;
    // Loop through iron/diamond/gold
    for (int i = 0; i <= 3; i++) {
      // Loop through pieces of the armor in the set
      for (int j = 0; j <= 3; j++) {
        int mId = start + (i * 4) + j;
        @SuppressWarnings("deprecation")
        Material mat = Material.getMaterial(mId);
        ShapelessRecipe recipe =
            new ShapelessRecipe(Util.setName(
                new ItemStack(mat),
                ChatColor.RESET + messages.getCaption("hazmat") + " "
                    + Util.title(mat.name().replace("_", " ").toLowerCase())));
        recipe.addIngredient(mat);
        @SuppressWarnings("deprecation")
        Material chain = Material.getMaterial(start - 4 + j);
        recipe.addIngredient(chain);

        getServer().addRecipe(recipe);
      }
    }
    start = 298;
    for (int j = 0; j <= 3; j++) {
      int mId = start + j;
      @SuppressWarnings("deprecation")
      Material mat = Material.getMaterial(mId);
      ShapelessRecipe recipe =
          new ShapelessRecipe(Util.setName(
              new ItemStack(mat),
              ChatColor.RESET + messages.getCaption("hazmat") + " "
                  + Util.title(mat.name().replace("_", " ").toLowerCase())));
      recipe.addIngredient(mat);
      @SuppressWarnings("deprecation")
      Material chain = Material.getMaterial(mId + 4);
      recipe.addIngredient(chain);
      // log.info(chain.name() + " " + mat.name());

      getServer().addRecipe(recipe);
    }

    getServer().addRecipe(hazardBootsR);
    getServer().addRecipe(hazardPantsR);
    getServer().addRecipe(hazardChestR);
    getServer().addRecipe(hazardHelmetR);

    // Schedules

    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        for (World w : getServer().getWorlds()) {
          Object regions;
          for (Player p : w.getPlayers()) {
            boolean noFallout = false;
            boolean forceFallout = false;
            if (wgEnabled) {
              regions =
                  ((WorldGuardPlugin) wg).getRegionManager(w).getApplicableRegions(p.getLocation());
              for (ProtectedRegion next : ((ApplicableRegionSet) regions)) {
                for (String s : getConfig().getStringList("regions.fallout")) {
                  if (next.getId().equals(s)) {
                    forceFallout = true;
                    break;
                  }
                }
                for (String s : getConfig().getStringList("regions.noFallout")) {
                  if (next.getId().equals(s)) {
                    noFallout = true;
                    break;
                  }
                }
              }
            }
            if (!noFallout && (worldEnabledFallout(w.getName()) || forceFallout)) {
              // Acid Rain
              Location l = p.getLocation();
              double temp = Util.getBiomeTemp(l);
              if (p.getEquipment().getHelmet() == null
                  && p.getWorld().getHighestBlockYAt(l.getBlockX(), l.getBlockZ()) <= l.getBlockY()
                  && p.getWorld().hasStorm() && temp < 1.0D) {
                Util.damageWithCause(p, getMessages().getCaption("acidRain"), p.getWorld()
                    .getDifficulty().ordinal() * 2);
              }
              // Neurological death syndrome
              if (radiationManager.getPlayerRadiation(p) >= 10.0D) {
                ArrayList<PotionEffect> pfx = new ArrayList<>();
                pfx.add(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 2));
                pfx.add(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 2));
                pfx.add(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 2));
                pfx.add(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10 * 20, 2));
                pfx.add(new PotionEffect(PotionEffectType.WEAKNESS, 10 * 20, 2));
                p.addPotionEffects(pfx);
              }
              // Add radiation
              boolean hazmatSuit = playerWearingHazmatSuit(p);
              boolean aboveLowPoint =
                  p.getLocation().getBlockY() > getConfig().getWorld(w).getInt("radiationBottom");
              boolean belowHighPoint =
                  p.getLocation().getBlockY() < getConfig().getWorld(w).getInt("radiationTop");
              boolean random = rand.nextInt(4) == 0;
              if (!hazmatSuit && aboveLowPoint && belowHighPoint && random) {
                radiationManager.addPlayerRadiation(
                    p,
                    (p.getWorld().getEnvironment() == Environment.NETHER ? getConfig().getWorld(w)
                        .getDouble("radiationRate") * 2 : getConfig().getWorld(w).getDouble(
                        "radiationRate"))
                        * (Math.round(p.getLevel() / 10) + 1));
              }
            }
          }
        }
      }
    }, 20 * ((long) 10), 20 * ((long) 10));
  }

  @Override
  public void onDisable() {
    if (!db.open()) {
      log.severe(getMessages().getCaption("errNotOpenDatabase"));
      return;
    }
    try {
      for (World w : Bukkit.getWorlds()) {
        for (Player p : w.getPlayers()) {
          radiationManager.saveRadiation(p);
        }
      }
      db.close();
    } catch (SQLException ex) {
      Logger.getLogger(Apocalyptic.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public ChunkGenerator getDefaultWorldGenerator(String worldName, String genID) {
    return new RavagedChunkGenerator(this, genID);
  }

  /**
   * 
   * @param name of the world
   * @return whether the named world has fallout enabled
   */
  public boolean worldEnabledFallout(String name) {
    return getConfig().getConfigurationSection("worlds").getKeys(false).contains(name) && getConfig().getBoolean("worlds." + name + ".fallout"); //$NON-NLS-3$
  }

  /**
   * 
   * @param name of the world
   * @return whether the named world has zombie apocalypse enabled
   */
  public boolean worldEnabledZombie(String name) {
    return getConfig().getConfigurationSection("worlds").getKeys(false).contains(name) && getConfig().getBoolean("worlds." + name + ".zombie"); //$NON-NLS-3$
  }

  /**
   * 
   * @param p The player
   * @return whether the player has a full hazmat suit
   */
  public boolean playerWearingHazmatSuit(Player p) {
    EntityEquipment e = p.getEquipment();
    boolean helmet =
        e.getHelmet() != null
            && e.getHelmet().hasItemMeta()
            && e.getHelmet().getItemMeta().hasDisplayName()
            && (e.getHelmet().getItemMeta().getDisplayName()
                .startsWith(ChatColor.RESET + getMessages().getCaption("gasMask")) || e.getHelmet()
                .getItemMeta().getDisplayName()
                .startsWith(ChatColor.RESET + getMessages().getCaption("hazmat")));
    boolean chest =
        e.getChestplate() != null
            && e.getChestplate().hasItemMeta()
            && e.getChestplate().getItemMeta().hasDisplayName()
            && e.getChestplate().getItemMeta().getDisplayName()
                .startsWith(ChatColor.RESET + getMessages().getCaption("hazmat"));
    boolean legs =
        e.getLeggings() != null
            && e.getLeggings().hasItemMeta()
            && e.getLeggings().getItemMeta().hasDisplayName()
            && e.getLeggings().getItemMeta().getDisplayName()
                .startsWith(ChatColor.RESET + getMessages().getCaption("hazmat"));
    boolean boots =
        e.getBoots() != null
            && e.getBoots().hasItemMeta()
            && e.getBoots().getItemMeta().hasDisplayName()
            && e.getBoots().getItemMeta().getDisplayName()
                .startsWith(ChatColor.RESET + getMessages().getCaption("hazmat"));
    return helmet && chest && legs && boots;
  }

  /**
   * Sends apocalyptic texture pack to a player.
   * 
   * @param p the player which to send the texture pack to
   */
  public void sendApocalypticTexturePack(Player p) {
    if (!getConfig().getBoolean("worlds." + p.getWorld().getName() + ".texturepack")) {
      return;
    }
    p.setResourcePack(texturePack);
  }

  @Override
  public ApocalypticConfiguration getConfig() {
    if (cachedConfig == null || recacheConfig) {
      recacheConfig = false;
      ApocalypticConfiguration config = new ApocalypticConfiguration();
      try {
        config.load(new File(getDataFolder().getPath() + File.separator + "config.yml"));
      } catch (IOException | InvalidConfigurationException ex) {
        ex.printStackTrace();
      }
      cachedConfig = config;
      return config;
    } else {
      return cachedConfig;
    }
  }

  /**
   * 
   * @param p a player
   * @param cmd String alias of a command
   * @return whether the specified player can perform the command
   */
  public boolean canDoCommand(CommandSender p, String cmd) {
    if (p == getServer().getConsoleSender()) {
      return true;
    }
    boolean usePerms = getConfig().getBoolean("meta.permissions");
    if (usePerms) {
      return (cmd.equals("radiation.self") && p.hasPermission("apocalyptic.radiation.self"))
          || (cmd.equals("radiation.other") && p.hasPermission("apocalyptic.radiation.other"))
          || (cmd.equals("radiation.change") && p
              .hasPermission("apocalyptic.radiation.change.self"))
          || (cmd.equals("apocalyptic.radhelp") && p.hasPermission("apocalyptic.help.radiation"))
          || (cmd.equals("apocalyptic.stop") && p.hasPermission("apocalyptic.admin.stop"))
          || (cmd.equals("apocalyptic.reload")
              && p.hasPermission("apocalyptic.admin.reload")
              || (cmd.equals("hazmatArmor.self") && p.hasPermission("apocalyptic.hazmatArmor.self")) || (cmd
              .equals("hazmatArmor.other") && p.hasPermission("apocalyptic.hazmatArmor.other")));
    } else {
      return !(cmd.equals("radiation.other") || cmd.equals("radiation.change")
          || cmd.equals("apocalyptic.stop") || cmd.equals("apocalyptic.reload")
          || cmd.equals("hazmatArmor.self") || cmd.equals("hazmatArmor.other"))
          || p.isOp();
    }
  }

  private Plugin getWorldGuard() {
    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

    // WorldGuard may not be loaded
    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
      return null; // Maybe you want throw an exception instead
    }

    return plugin;
  }

  /**
   * Get the language file.
   * 
   * @return The language file.
   */
  public Messages getMessages() {
    return messages;
  }

  /**
   * Recache the configuration file.
   */
  public void reloadConfig() {
    recacheConfig = true;
  }

  /**
   * Get the metadata key used to save radiation to players.
   * 
   * @return The metadata key.
   */
  public String getMetadataKey() {
    return METADATA_KEY;
  }

  /**
   * Get the radiation manager, used for saving, adding, and setting radiation.
   * 
   * @return The Radiation Manager object.
   */
  public RadiationManager getRadiationManager() {
    return radiationManager;
  }

  /**
   * Get the Gas Mask itemstack
   * 
   * @return an itemstack with 1 Gas Mask.
   */
  public ItemStack getHazmatHood() {
    return hazmatHood;
  }

  /**
   * Get the Hazmat Suit itemstack
   * 
   * @return an itemstack with 1 Hazmat Suit.
   */
  public ItemStack getHazmatSuit() {
    return hazmatSuit;
  }

  /**
   * Get the Hazmat Leggings itemstack
   * 
   * @return an itemstack with 1 Hazmat Leggings.
   */
  public ItemStack getHazmatPants() {
    return hazmatPants;
  }

  /**
   * Get the Hazmat Boots itemstack
   * 
   * @return an itemstack with 1 Hazmat Boots.
   */
  public ItemStack getHazmatBoots() {
    return hazmatBoots;
  }

  public Random getRandom() {
    return rand;
  }

  public String getTablePrefix() {
    return tablePrefix;
  }
}
