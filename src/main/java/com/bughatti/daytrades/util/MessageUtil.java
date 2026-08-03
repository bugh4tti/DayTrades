package com.bughatti.daytrades.util;

import com.bughatti.daytrades.DayTrades;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void sendTitle(Player player, String key, String placeholder) {
        var cfg = DayTrades.getInstance().getConfig();
        String pluginName = color(cfg.getString("plugin-name", "&6DayTrades"));

        String title = cfg.getString("messages." + key + ".title", "{plugin-name}")
                .replace("{plugin-name}", pluginName);
        String subtitle = cfg.getString("messages." + key + ".subtitle", "")
                .replace("{player}", placeholder);

        player.sendTitle(color(title), color(subtitle), 10, 60, 10);
    }

    public static void sendChat(Player player, String key, String placeholder) {
        var cfg = DayTrades.getInstance().getConfig();
        String msg = cfg.getString("chat-messages." + key, "")
                .replace("{player}", placeholder == null ? "" : placeholder);
        player.sendMessage(color(msg));
    }
      }
