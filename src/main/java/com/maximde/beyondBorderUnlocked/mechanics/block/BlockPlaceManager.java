package com.maximde.beyondBorderUnlocked.mechanics.block;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockPlaceManager {
    public static void handleBlockPlace(Player player, Block targetBlock, ItemStack handItem) {
        BlockFace blockFace = BlockUtils.getTargetBlockFace(player);
        if (blockFace == null) {
            return;
        }

        Block placeBlock = targetBlock.getRelative(blockFace);
        placeBlock.setType(handItem.getType());

        updatePlayerInventory(player, handItem);
        playPlaceSound(placeBlock);
    }

    private static void updatePlayerInventory(Player player, ItemStack handItem) {
        handItem.setAmount(handItem.getAmount() - 1);
    }

    private static void playPlaceSound(Block block) {
        block.getWorld().playSound(
                block.getLocation(),
                Sound.BLOCK_STONE_PLACE,
                1.0f,
                1.0f
        );
    }
}