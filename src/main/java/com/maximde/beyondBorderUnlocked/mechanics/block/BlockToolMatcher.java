package com.maximde.beyondBorderUnlocked.mechanics.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BlockToolMatcher {
    private static final String PICKAXE_MATERIALS = ".*(STONE|ORE|BRICK|CONCRETE|METAL|IRON|GOLD|DIAMOND|" +
            "EMERALD|COPPER|ANCIENT_DEBRIS|NETHERITE|OBSIDIAN|DEEPSLATE|BLACKSTONE|" +
            "AMETHYST|PRISMARINE|PURPUR|QUARTZ|TERRACOTTA|SANDSTONE|BASALT).*";

    private static final String AXE_MATERIALS = ".*(WOOD|LOG|PLANK|FENCE|DOOR|SIGN|BARREL|CHEST|" +
            "CRAFTING_TABLE|BOOKSHELF|LADDER|SCAFFOLDING|CAMPFIRE|STEM|HYPHAE).*";

    private static final String SHOVEL_MATERIALS = ".*(DIRT|GRASS|SAND|GRAVEL|CLAY|MYCELIUM|PATH|" +
            "SOUL_SAND|SOUL_SOIL|SNOW|POWDER_SNOW|PODZOL|FARMLAND).*";

    private static final String HOE_MATERIALS = ".*(LEAVES|HAY|SPONGE|MOSS|SCULK|TARGET|SHROOMLIGHT|" +
            "DRIED_KELP|WART|CROPS|ROOTS|NETHER_WART).*";

    public static boolean isCorrectToolForBlock(ItemStack tool, Block block) {
        if (tool == null) return false;

        Material toolType = tool.getType();
        String toolName = toolType.name();
        String blockName = block.getType().name();

        if (toolName.contains("PICKAXE")) {
            return blockName.matches(PICKAXE_MATERIALS);
        }
        if (toolName.contains("AXE")) {
            return blockName.matches(AXE_MATERIALS)
                    && !blockName.contains("IRON")
                    && !blockName.contains("METAL");
        }
        if (toolName.contains("SHOVEL")) {
            return blockName.matches(SHOVEL_MATERIALS);
        }
        if (toolName.contains("HOE")) {
            return blockName.matches(HOE_MATERIALS);
        }

        return false;
    }
}