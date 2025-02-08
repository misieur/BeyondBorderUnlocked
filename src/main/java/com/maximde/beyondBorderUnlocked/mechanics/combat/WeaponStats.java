package com.maximde.beyondBorderUnlocked.mechanics.combat;

import org.bukkit.Material;
import java.util.Map;
import java.util.HashMap;

public class WeaponStats {
    private static final Map<Material, Double> WEAPON_DAMAGES = new HashMap<>();

    static {
        WEAPON_DAMAGES.put(Material.WOODEN_SWORD, 4.0);
        WEAPON_DAMAGES.put(Material.STONE_SWORD, 5.0);
        WEAPON_DAMAGES.put(Material.IRON_SWORD, 6.0);
        WEAPON_DAMAGES.put(Material.DIAMOND_SWORD, 7.0);
        WEAPON_DAMAGES.put(Material.NETHERITE_SWORD, 8.0);

        WEAPON_DAMAGES.put(Material.WOODEN_AXE, 3.0);
        WEAPON_DAMAGES.put(Material.STONE_AXE, 4.0);
        WEAPON_DAMAGES.put(Material.IRON_AXE, 5.0);
        WEAPON_DAMAGES.put(Material.DIAMOND_AXE, 6.0);
        WEAPON_DAMAGES.put(Material.NETHERITE_AXE, 7.0);
    }

    public static double getDamage(Material material) {
        return WEAPON_DAMAGES.getOrDefault(material, 1.0);
    }
}