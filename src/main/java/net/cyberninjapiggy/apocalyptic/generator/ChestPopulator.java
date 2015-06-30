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

package net.cyberninjapiggy.apocalyptic.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.cyberninjapiggy.apocalyptic.Apocalyptic;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChestPopulator {
  private Apocalyptic plugin;
  private Map<LootTableEntry, Integer> lootTable;

  public ChestPopulator(FileConfiguration worldConfig, Apocalyptic plugin) {
    this.plugin = plugin;
    lootTable = new HashMap<>();
    genLootTable(worldConfig);
  }

  public void populateChest(Location location, Random rand) {
    Chest c = (Chest) location.getBlock().getState();
    Inventory inv = c.getBlockInventory();

    for (Map.Entry<LootTableEntry, Integer> e : lootTable.entrySet()) {
      if (rand.nextInt(e.getValue()) == 0) {
        ItemStack stack = null;
        if (e.getKey().isSpecialItem()) {
          switch (e.getKey().getSpecialItem()) {
            case HAZMAT_HOOD:
              stack = new ItemStack(plugin.getHazmatHood());
              break;
            case HAZMAT_SUIT:
              stack = new ItemStack(plugin.getHazmatSuit());
              break;
            case HAZMAT_PANTS:
              stack = new ItemStack(plugin.getHazmatPants());
              break;
            case HAZMAT_BOOTS:
              stack = new ItemStack(plugin.getHazmatBoots());
              break;
          }
        } else {
          stack = new ItemStack(e.getKey().getMaterial());
        }
        stack.setAmount(rand.nextInt(e.getKey().getMax() - e.getKey().getMin() + 1)
            + e.getKey().getMin());
        inv.setItem(rand.nextInt(inv.getSize() - 1), stack);
      }
    }
  }

  public void genLootTable(FileConfiguration worldConfig) {

    ConfigurationSection loot = worldConfig.getConfigurationSection("houses.loot");
    Set<String> keys = loot.getKeys(false);
    for (String key : keys) {
      LootTableEntry entry;
      ConfigurationSection item = worldConfig.getConfigurationSection("houses.loot." + key);
      SpecialItem special = SpecialItem.match(item.getName());
      if (special != null) {
        entry = new LootTableEntry(special, item.getInt("min"), item.getInt("max"));
      } else {
        entry =
            new LootTableEntry(Material.matchMaterial(item.getName()), item.getInt("min"),
                item.getInt("max"));
      }
      lootTable.put(entry, item.getInt("frequency"));
    }
  }

  public enum SpecialItem {
    HAZMAT_HOOD, HAZMAT_SUIT, HAZMAT_PANTS, HAZMAT_BOOTS;

    public static SpecialItem match(String s) {
      SpecialItem item;
      try {
        item = SpecialItem.valueOf(s.toUpperCase());
      } catch (IllegalArgumentException e) {
        return null;
      }
      return item;
    }
  }

  public static class LootTableEntry {
    private final Material mat;
    private final int min;
    private final int max;
    private final SpecialItem item;
    private boolean isSpecialItem = false;

    public LootTableEntry(Material mat, int min, int max) {

      this.mat = mat;
      this.min = min;
      this.max = max;
      item = null;
    }

    public LootTableEntry(SpecialItem item, int min, int max) {
      isSpecialItem = true;
      this.item = item;
      this.min = min;
      this.max = max;
      mat = null;
    }

    public Material getMaterial() {
      return mat;
    }

    public int getMin() {
      return min;
    }

    public int getMax() {
      return max;
    }

    public boolean isSpecialItem() {
      return isSpecialItem;
    }

    public SpecialItem getSpecialItem() {
      return item;
    }
  }
}
