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

package net.cyberninjapiggy.apocalyptic.generator;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class LavaPopulator extends BlockPopulator {
	//Taken from Nordic bukkit plugin
	public void populate(World world, Random random, Chunk source) {
		if (random.nextInt(50) >= 2) {
			return;
		}
		ChunkSnapshot snapshot = source.getChunkSnapshot();

		int rx16 = random.nextInt(16);
		int rx = (source.getX() << 4) + rx16;
		int rz16 = random.nextInt(16);
		int rz = (source.getZ() << 4) + rz16;
		if (snapshot.getHighestBlockYAt(rx16, rz16) < 4) return;
		int ry = random.nextInt(20) + 20;
		int radius = 2 + random.nextInt(4);

		Material solidMaterial = Material.STATIONARY_LAVA;

		ArrayList < Block > lakeBlocks = new ArrayList < > ();
		Vector center;
		for (int i = -1; i < 4; i++) {
			center = new BlockVector(rx, ry - i, rz);
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					Vector position = center.clone().add(new Vector(x, 0, z));
					if (center.distance(position) <= radius + 0.5D - i) {
						lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
					}
				}
			}
		}

		for (Block block: lakeBlocks) {
			if ((!block.isEmpty()) && (!block.isLiquid())) if (block.getY() >= ry) {
				block.setType(Material.AIR);
			} else block.setType(solidMaterial);
		}
	}

}