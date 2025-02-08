package com.maximde.beyondBorderUnlocked.mechanics.combat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** 
 * Damage calculation:
 * Final damage = (Base + Weapon) * Critical * (1 - ArmorReduction)
 * 
 * MAX_ARMOR_REDUCTION: 80% maximum damage reduction from armor
 * ARMOR_REDUCTION_FACTOR: Each armor point reduces damage by 4%
 * CRITICAL_MULTIPLIER: Critical hits deal 50% more damage
 */

public class DamageCalculator {
    private static final double MAX_ARMOR_REDUCTION = 0.8;
    private static final double ARMOR_REDUCTION_FACTOR = 0.04;
    private static final double CRITICAL_MULTIPLIER = 1.5;

    public static double calculateDamage(Player player, LivingEntity target, double baseDamage, boolean isCritical) {
        double weaponDamage = getWeaponDamage(player);
        double armorReduction = calculateArmorReduction(target);
        double totalBaseDamage = baseDamage + weaponDamage;

        if (isCritical) {
            totalBaseDamage *= CRITICAL_MULTIPLIER;
        }

        return totalBaseDamage * (1 - Math.min(armorReduction, MAX_ARMOR_REDUCTION));
    }

    private static double getWeaponDamage(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        return handItem.getType() == Material.AIR ? 1.0 : WeaponStats.getDamage(handItem.getType());
    }

    private static double calculateArmorReduction(LivingEntity entity) {
        return entity.getAttribute(Attribute.GENERIC_ARMOR).getValue() * ARMOR_REDUCTION_FACTOR;
    }
}