package com.maximde.beyondBorderUnlocked.utils;

import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.maximde.beyondBorderUnlocked.mechanics.outline.OutlineSegment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Config {
    private boolean building = false;
    private boolean breaking = true;
    private boolean walkthrough = true;
    private boolean hitting = true;

    private boolean damageEnabled = true;
    private double damageBuffer = 5.0;
    private double damageAmount = 0.2;

    private boolean blockOutlineEnabled = true;
    private float blockOutlineSize = 0.009F;
    private ItemType blockOutlineBlock = ItemTypes.BLACK_STAINED_GLASS;
    
    private final File configFile;
    private FileConfiguration config;

    @Setter(AccessLevel.NONE)
    private List<OutlineSegment> segments;

    public Config(JavaPlugin plugin) {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        reload();
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
        building = config.getBoolean("building", true);
        breaking = config.getBoolean("breaking", true);
        walkthrough = config.getBoolean("walkthrough", true);
        hitting = config.getBoolean("hitting", true);
        damageEnabled = config.getBoolean("damage.enabled", true);
        damageBuffer = config.getDouble("damage.buffer", 0.2);
        damageAmount = config.getDouble("damage.amount", 1.0);
        blockOutlineEnabled = config.getBoolean("blockOutline.enabled", true);
        blockOutlineSize = (float) config.getDouble("blockOutline.size", 0.008F);
        blockOutlineBlock = ItemTypes.getByName(config.getString("blockOutline.block", ItemTypes.BLACK_STAINED_GLASS.getName().getKey()));
        setSegments();
    }

    public void setSegments() {
        this.segments = generateSegments();
    }

    public void save() throws IOException {
        config.set("building", building);
        config.set("breaking", breaking);
        config.set("walkthrough", walkthrough);
        config.set("hitting", hitting);
        config.set("damage.enabled", damageEnabled);
        config.set("damage.buffer", damageBuffer);
        config.set("damage.amount", damageAmount);
        config.set("blockOutline.enabled", blockOutlineEnabled);
        config.set("blockOutline.size", blockOutlineSize);
        config.set("blockOutline.block", blockOutlineBlock.getName().getKey());

        config.save(configFile);
    }

    public List<OutlineSegment> generateSegments() {
        List<OutlineSegment> segments = new ArrayList<>();
        float size = this.getBlockOutlineSize();

        segments.add(new OutlineSegment(new Vector3f(1, size, size), new Vector3f(0.5F, 1, 0)));
        segments.add(new OutlineSegment(new Vector3f(1, size, size), new Vector3f(0.5F, 1, 1)));
        segments.add(new OutlineSegment(new Vector3f(size, size, 1), new Vector3f(0, 1, 0.5F)));
        segments.add(new OutlineSegment(new Vector3f(size, size, 1), new Vector3f(1, 1, 0.5F)));

        segments.add(new OutlineSegment(new Vector3f(1, size, size), new Vector3f(0.5F, 0, 0)));
        segments.add(new OutlineSegment(new Vector3f(1, size, size), new Vector3f(0.5F, 0, 1)));
        segments.add(new OutlineSegment(new Vector3f(size, size, 1), new Vector3f(0, 0, 0.5F)));
        segments.add(new OutlineSegment(new Vector3f(size, size, 1), new Vector3f(1, 0, 0.5F)));

        segments.add(new OutlineSegment(new Vector3f(size, 1, size), new Vector3f(0, 0.5F, 0)));
        segments.add(new OutlineSegment(new Vector3f(size, 1, size), new Vector3f(1, 0.5F, 0)));
        segments.add(new OutlineSegment(new Vector3f(size, 1, size), new Vector3f(0, 0.5F, 1)));
        segments.add(new OutlineSegment(new Vector3f(size, 1, size), new Vector3f(1, 0.5F, 1)));

        return segments;
    }
}
