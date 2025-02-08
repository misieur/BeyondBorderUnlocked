package com.maximde.beyondBorderUnlocked.mechanics.border;

import org.bukkit.Location;

public record BorderData(
        double[] borders,
        Location center,
        double distX,
        double distZ,
        double px,
        double pz
) {}