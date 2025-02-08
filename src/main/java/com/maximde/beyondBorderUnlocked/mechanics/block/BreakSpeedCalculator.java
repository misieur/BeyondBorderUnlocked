package com.maximde.beyondBorderUnlocked.mechanics.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class BreakSpeedCalculator {
    private static final float BASE_SPEED = 6.0f;
    private static final float NETHERITE_MULTIPLIER = 9.0f;
    private static final float DIAMOND_MULTIPLIER = 8.0f;
    private static final float IRON_MULTIPLIER = 6.0f;
    private static final float STONE_MULTIPLIER = 4.0f;
    private static final float WOODEN_MULTIPLIER = 2.0f;
    private static final float INSTANT_BREAK_MULTIPLIER = 1000f;

    /** Tool multipliers for break speed:
     * NETHERITE: 9x base speed
     * DIAMOND: 8x base speed  
     * IRON: 6x base speed
     * STONE: 4x base speed
     * WOODEN: 2x base speed
     * 
     * Efficiency enchantment bonus:
     * speed += (level^2 + 1)
     */
    public static float calculateBreakSpeed(ItemStack tool, Block block) {
        float speedMultiplier = BASE_SPEED;
        Material blockType = block.getType();

        if (BlockToolMatcher.isCorrectToolForBlock(tool, block)) {
            speedMultiplier *= getToolTypeMultiplier(tool);
            speedMultiplier *= getEfficiencyMultiplier(tool);
        }

        speedMultiplier = applyBlockHardness(speedMultiplier, blockType);

        if (isInstantBreakMaterial(blockType)) {
            speedMultiplier *= INSTANT_BREAK_MULTIPLIER;
        }

        return speedMultiplier;
    }

    private static float getToolTypeMultiplier(ItemStack tool) {
        String toolMaterial = tool.getType().name().split("_")[0];
        switch (toolMaterial) {
            case "NETHERITE": return NETHERITE_MULTIPLIER;
            case "DIAMOND": return DIAMOND_MULTIPLIER;
            case "IRON": return IRON_MULTIPLIER;
            case "STONE": return STONE_MULTIPLIER;
            case "WOODEN": return WOODEN_MULTIPLIER;
            default: return 1.0f;
        }
    }

    private static float getEfficiencyMultiplier(ItemStack tool) {
        int efficiencyLevel = tool.getEnchantmentLevel(Enchantment.DIG_SPEED);
        return efficiencyLevel > 0 ? 1.0f + (efficiencyLevel * efficiencyLevel + 1) : 1.0f;
    }

    private static float applyBlockHardness(float speed, Material blockType) {
        float hardness = blockType.getHardness();
        return hardness > 0 ? speed / hardness : speed;
    }

    private static boolean isInstantBreakMaterial(Material blockType) {
        String name = blockType.name();
        return blockType == Material.GRASS
                || blockType == Material.TALL_GRASS
                || name.contains("BUSH")
                || name.contains("TULIP")
                || name.contains("DANDELION")
                || name.contains("POPPY")
                || name.contains("BLUE_ORCHID")
                || name.contains("CACTUS")
                || name.contains("SUGAR_CANE")
                || name.contains("VINE");
    }
}