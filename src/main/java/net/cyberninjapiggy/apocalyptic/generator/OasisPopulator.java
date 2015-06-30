/*
 * Copyright (C) 2015 Kaisar Arkhan Copyright (C) 2014 Nick Schatz
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

import org.bukkit.*;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class OasisPopulator extends BlockPopulator {
  private int frequency;
  private final int maxSize;
  private final int minSize;

  public OasisPopulator(int frequency, int maxSize, int minSize) {
    this.frequency = frequency;

    this.maxSize = maxSize;
    this.minSize = minSize;
  }

  @Override
  public void populate(World world, Random rand, Chunk chunk) {
    if (rand.nextInt(frequency) == 0) {
      int realX = 0;
      int realZ = 0;
      boolean flag = false;
      outer_loop: for (int i = 0; i < 16; i++) {
        realX = i + chunk.getX() * 16;
        for (int o = 0; o < 16; o++) {
          realZ = o + chunk.getZ() * 16;
          if (world.getBlockAt(realX, world.getHighestBlockYAt(realX, realZ) - 1, realZ).getType() == Material.MYCEL) {
            flag = true;
            break outer_loop;
          }
        }
      }
      if (!flag)
        return;
      int size = rand.nextInt(maxSize - minSize) + minSize;
      if (size % 2 == 0) {
        size += 1;
      }
      int[][] matrix = new int[size][];
      double midPoint = (matrix.length - 1) / 2.0;
      for (int col = 0; col < matrix.length; col++) {
        int[] row = new int[matrix.length];
        double yy = col - midPoint;
        for (int x = 0; x < row.length; x++) {
          double xx = x - midPoint;
          if (Math.sqrt(xx * xx + yy * yy) <= midPoint)
            row[x] = 1;
        }
        matrix[col] = row;
      }
      Location tree =
          new Location(world, realX + (size - 1) / 2, world.getHighestBlockYAt(realX + (size - 1)
              / 2, realZ + (size - 1) / 2) - 1, realZ + (size - 1) / 2);

      for (int x = 0; x < size; x++) {
        for (int y = 0; y < size; y++) {
          if (matrix[x][y] == 1) {
            int pondSize = (size - 1) / 4;
            if (x < ((size - 1) / 2) + pondSize && x > ((size - 1) / 2) - pondSize
                && y < ((size - 1) / 2) + pondSize && y > ((size - 1) / 2) - pondSize) {
              world.getBlockAt(realX + x, world.getHighestBlockYAt(realX + x, realZ + y) + 30,
                  realZ + y).setType(Material.WATER);
            } else if (x == ((size - 1) / 2) + pondSize + 1 && x == ((size - 1) / 2) - pondSize - 1
                && y == ((size - 1) / 2) + pondSize + 1 && y == ((size - 1) / 2) - pondSize - 1) {
              for (int i = 0; i < rand.nextInt(2 + 1); i++) {
                world.getBlockAt(realX + x,
                    world.getHighestBlockYAt(realX + x, realZ + y) + i + 30, realZ + y).setType(
                    Material.WATER);
              }

            } else {
              world.getBlockAt(realX + x, world.getHighestBlockYAt(realX + x, realZ + y) + 30,
                  realZ + y).setType(Material.GRASS);
              if (rand.nextInt(5) == 0) {
                tree =
                    new Location(world, realX + x, world.getHighestBlockYAt(realX + x, realZ + y),
                        realZ + y);
              }
              if (rand.nextInt(2) == 0) {
                // noinspection deprecation,deprecation
                world.getBlockAt(realX + x, world.getHighestBlockYAt(realX + x, realZ + y),
                    realZ + y).setTypeIdAndData(Material.LONG_GRASS.getId(), (byte) 1, true);
              }
            }
          }
        }
      }

      TreeType tt;

      switch (rand.nextInt(9)) {
        case 0:
          tt = TreeType.BIG_TREE;
          break;
        case 1:
          tt = TreeType.TREE;
          break;
        case 2:
          tt = TreeType.BIRCH;
          break;
        case 3:
          tt = TreeType.SMALL_JUNGLE;
          break;
        case 4:
          tt = TreeType.JUNGLE;
          break;
        case 5:
          tt = TreeType.ACACIA;
          break;
        case 6:
          tt = TreeType.DARK_OAK;
          break;
        case 7:
          tt = TreeType.REDWOOD;
          break;
        case 8:
          tt = TreeType.TALL_REDWOOD;
          break;
        default:
          tt = TreeType.TREE;
      }


      for (int i = 1; i < 3; i++)
        genDirtCircle(world, rand, chunk, size - i, i, realX, realZ);

      world.generateTree(tree, tt);
    }
  }

  private void genDirtCircle(World world, Random rand, Chunk chunk, int size, int level, int realX,
      int realZ) {
    int mysize = size - 1;
    int[][] matrix = new int[mysize][mysize];
    double midPoint = (matrix.length - 1) / 2.0;

    for (int col = 0; col < matrix.length; col++) {
      int[] row = new int[matrix.length];
      double yy = col - midPoint;
      for (int x = 0; x < row.length; x++) {
        double xx = x - midPoint;
        if (Math.sqrt(xx * xx + yy * yy) <= midPoint)
          row[x] = 1;
      }
      matrix[col] = row;
    }

    for (int x = 0; x < mysize - 1; x++)
      for (int y = 0; y < mysize - 1; y++)
        if (matrix[x][y] == 1)
          world.getBlockAt(realX + x + 1,
              world.getHighestBlockYAt(realX + x + 1, realZ + y + 1) - (level + 1), realZ + y + 1)
              .setType(Material.DIRT);

  }
}
