package com.maximde.beyondBorderUnlocked.utils;

import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.maximde.beyondBorderUnlocked.BeyondBorderUnlocked;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class Command implements CommandExecutor, TabCompleter {

    private final BeyondBorderUnlocked beyondBorderUnlocked;

    private final List<String> options = List.of("reload", "set");
    private final List<String> settings = List.of(
            "damage.enabled", "damage.buffer", "damage.amount",
            "building", "breaking", "walkthrough", "hitting",
            "blockOutline.enabled", "blockOutline.size", "blockOutline.block"
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("beyondborder.commands")) {
            sender.sendMessage(ChatColor.RED + "❌ You don't have permission to use this command!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            beyondBorderUnlocked.getPluginConfig().reload();
            sender.sendMessage(ChatColor.GREEN + "✓ Configuration has been successfully reloaded!");
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            Config config = beyondBorderUnlocked.getPluginConfig();
            String setting = args[1].toLowerCase();
            String value = args[2];

            try {
                switch (setting) {
                    case "building":
                        config.setBuilding(Boolean.parseBoolean(value));
                        sender.sendMessage(ChatColor.GREEN + "✓ Building beyond border: " + value);
                        break;
                    case "breaking":
                        config.setBreaking(Boolean.parseBoolean(value));
                        sender.sendMessage(ChatColor.GREEN + "✓ Breaking beyond border: " + value);
                        break;
                    case "walkthrough":
                        config.setWalkthrough(Boolean.parseBoolean(value));
                        sender.sendMessage(ChatColor.GREEN + "✓ Walking through border: " + value);
                        break;
                    case "hitting":
                        config.setHitting(Boolean.parseBoolean(value));
                        sender.sendMessage(ChatColor.GREEN + "✓ Hitting through border: " + value);
                        break;
                    case "damage.enabled":
                        boolean damageEnabled = Boolean.parseBoolean(value);
                        config.setDamageEnabled(damageEnabled);
                        sender.sendMessage(ChatColor.GREEN + "✓ Border damage: " + value);
                        break;
                    case "damage.buffer":
                        double buffer = Double.parseDouble(value);
                        config.setDamageBuffer(buffer);
                        if(config.isDamageEnabled()) {
                            for (World world : Bukkit.getWorlds()) {
                                world.getWorldBorder().setDamageBuffer(buffer);
                                world.getWorldBorder().setDamageAmount(config.getDamageAmount());
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "✓ Border damage buffer set to: " + buffer);
                        break;
                    case "damage.amount":
                        double amount = Double.parseDouble(value);
                        config.setDamageAmount(amount);
                        if(config.isDamageEnabled()) {
                            for (World world : Bukkit.getWorlds()) {
                                world.getWorldBorder().setDamageBuffer(config.getDamageBuffer());
                                world.getWorldBorder().setDamageAmount(amount);
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "✓ Border damage amount set to: " + amount);
                        break;
                    case "blockoutline.enabled":
                        config.setBlockOutlineEnabled(Boolean.parseBoolean(value));
                        sender.sendMessage(ChatColor.GREEN + "✓ Block outline: " + value);
                        break;
                    case "blockoutline.size":
                        float size = Float.parseFloat(value);
                        config.setBlockOutlineSize(size);
                        config.setSegments();
                        sender.sendMessage(ChatColor.GREEN + "✓ Block outline size set to: " + size);
                        break;
                    case "blockoutline.block":
                        config.setBlockOutlineBlock(ItemTypes.getByName(value));
                        sender.sendMessage(ChatColor.GREEN + "✓ Block outline block type set to: " + value);
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "❌ Invalid setting! Use tab completion to see available options.");
                        return true;
                }
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "❌ Invalid number format! Please enter a valid number.");
                return true;
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "❌ Invalid value! Please check the correct format.");
                return true;
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "⚡ Usage:");
        sender.sendMessage(ChatColor.YELLOW + "/beyondborder reload " + ChatColor.GRAY + "- Reload the configuration");
        sender.sendMessage(ChatColor.YELLOW + "/beyondborder set <setting> <value> " + ChatColor.GRAY + "- Change a setting");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("beyondborder.commands")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return StringUtil.copyPartialMatches(args[1], settings, new ArrayList<>());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            String setting = args[1].toLowerCase();
            if (setting.equals("damage.buffer") || setting.equals("damage.amount")) {
                return List.of("<number>");
            } else if (setting.equals("blockoutline.size")) {
                return List.of("<float>");
            } else if (setting.equals("blockoutline.block")) {
                return List.of("<block_type>");
            } else {
                return StringUtil.copyPartialMatches(args[2], List.of("true", "false"), new ArrayList<>());
            }
        }

        return Collections.emptyList();
    }
}
