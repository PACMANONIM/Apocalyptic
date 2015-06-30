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

import java.io.File;
import java.io.IOException;
import java.util.Random;

import net.cyberninjapiggy.apocalyptic.misc.Schematic;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.Plugin;

public class SchematicPopulator extends BlockPopulator {
  private Schematic schematic;
  private final int chance;
  private ChestPopulator chestPopulator;

  public SchematicPopulator(Plugin p, String schemName, int chance, ChestPopulator chestPopulator) {
    this.chestPopulator = chestPopulator;
    try {
      this.schematic =
          Schematic.loadSchematic(new File(p.getDataFolder().getAbsolutePath() + File.separator
              + "schematics" + File.separator + schemName));
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.chance = chance;
  }

  @Override
  public void populate(World world, Random rand, Chunk chunk) {
    if (rand.nextInt(chance) != 0) {
      return;
    }
    int xPos = chunk.getX() * 16 + rand.nextInt(16 - schematic.getWidth());
    int zPos = chunk.getZ() * 16 + rand.nextInt(16 - schematic.getLenght());
    int yPos = world.getHighestBlockYAt(xPos, zPos);
    if (yPos < 63) {
      return;
    }
    Schematic.pasteSchematic(world, new Location(world, xPos, yPos, zPos), schematic,
        chestPopulator, rand);
  }
}
