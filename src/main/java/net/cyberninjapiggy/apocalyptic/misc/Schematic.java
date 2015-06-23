/*
 *
 *    This class is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This class is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this class.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.cyberninjapiggy.apocalyptic.misc;

import com.sk89q.jnbt.*;
import net.cyberninjapiggy.apocalyptic.generator.ChestPopulator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Max
 */
public class Schematic {
	private byte[] blocks;
	private byte[] data;
	private short width;
	private short lenght;
	private short height;

	public Schematic(byte[] blocks, byte[] data, short width, short lenght, short height) {
		this.blocks = blocks;
		this.data = data;
		this.width = width;
		this.lenght = lenght;
		this.height = height;
	}

	/**
	 * @return the blocks
	 */
	public byte[] getBlocks() {
		return blocks;
	}

	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return the width
	 */
	public short getWidth() {
		return width;
	}

	/**
	 * @return the lenght
	 */
	public short getLenght() {
		return lenght;
	}

	/**
	 * @return the height
	 */
	public short getHeight() {
		return height;
	}@SuppressWarnings("deprecation")
	public static void pasteSchematic(World world, Location loc, Schematic schematic, ChestPopulator chestPopulator, Random rand) {
		byte[] blocks = schematic.getBlocks();
		byte[] blockData = schematic.getData();

		short length = schematic.getLenght();
		short width = schematic.getWidth();
		short height = schematic.getHeight();

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				for (int z = 0; z < length; ++z) {
					int index = y * width * length + z * width + x;
					Block block = new Location(world, x + loc.getX(), y + loc.getY(), z + loc.getZ()).getBlock();

					if (block != null) {
						block.setTypeIdAndData(blocks[index], blockData[index], true);

						if (block.getType() == Material.CHEST) {
							chestPopulator.populateChest(block.getLocation(), rand);
						}
					}


				}
			}
		}
	}

	public static Schematic loadSchematic(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(stream));

		CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
		if (!schematicTag.getName().equals("Schematic")) {
			throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
		}

		Map < String, Tag > schematic = schematicTag.getValue();
		if (!schematic.containsKey("Blocks")) {
			throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
		}

		short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
		short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
		short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

		String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
		if (!materials.equals("Alpha")) {
			throw new IllegalArgumentException("Schematic file is not an Alpha schematic");
		}

		byte[] blocks = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
		byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
		return new Schematic(blocks, blockData, width, length, height);
	}

	/**
	 * Get child tag of a NBT structure.
	 *
	 * @param items The parent tag map
	 * @param key The name of the tag to get
	 * @param expected The expected type of the tag
	 * @return child tag casted to the expected type
	 * @throws com.sk89q.worldedit.data.DataException if the tag does not exist or the tag is not of the
	 * expected type
	 */
	private static < T extends Tag > T getChildTag(Map < String, Tag > items, String key, Class < T > expected) throws IllegalArgumentException {
		if (!items.containsKey(key)) {
			throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
		}
		Tag tag = items.get(key);
		if (!expected.isInstance(tag)) {
			throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
		}
		return expected.cast(tag);
	}
}