package com.bughatti.daytrades.trade;

import com.bughatti.daytrades.DayTrades;
import com.bughatti.daytrades.gui.TradeGUI;
import com.bughatti.daytrades.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {

    private final DayTrades plugin;
    private final Map<UUID, Trade> activeTrades = new HashMap<>();
    private final Map<UUID, UUID> pendingRequests = new HashMap<>(); // receiver -> sender

    public TradeManager(DayTrades plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player sender, Player target) {
        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());

        MessageUtil.sendTitle(target, "trade-received", sender.getName());
        MessageUtil.sendChat(sender, "trade-request-sent", target.getName());
    }

    public boolean hasPendingRequest(Player target, Player sender) {
        UUID requester = pendingRequests.get(target.getUniqueId());
        return requester != null && requester.equals(sender.getUniqueId());
    }

    public void acceptRequest(Player target, Player sender) {
        pendingRequests.remove(target.getUniqueId());

        Trade trade = new Trade(sender.getUniqueId(), target.getUniqueId());
        trade.setState(Trade.State.ACTIVE);

        activeTrades.put(sender.getUniqueId(), trade);
        activeTrades.put(target.getUniqueId(), trade);

        TradeGUI gui = new TradeGUI(plugin, trade);
        gui.open(sender);
        gui.open(target);
    }

    public Trade getTrade(UUID uuid) {
        return activeTrades.get(uuid);
    }

    public void toggleAccept(Player player, Trade trade) {
        trade.setAccepted(player.getUniqueId(), !trade.isAccepted(player.getUniqueId()));

        if (trade.bothAccepted()) {
            startCountdown(trade);
        }
    }

    private void startCountdown(Trade trade) {
        trade.setState(Trade.State.COUNTDOWN);
        FileConfiguration cfg = plugin.getConfig();
        int seconds = cfg.getInt("cooldown.seconds", 10);
        String soundKey = cfg.getString("cooldown.sound.key", "ui.button.click");
        float volume = (float) cfg.getDouble("cooldown.sound.volume", 1.0);
        float pitch = (float) cfg.getDouble("cooldown.sound.pitch", 1.0);

        Sound sound;
        try {
            sound = Sound.valueOf(soundKey.toUpperCase().replace(".", "_"));
        } catch (IllegalArgumentException e) {
            sound = Sound.UI_BUTTON_CLICK;
        }

        Sound finalSound = sound;

        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                Player p1 = Bukkit.getPlayer(trade.getPlayer1());
                Player p2 = Bukkit.getPlayer(trade.getPlayer2());

                if (p1 == null || p2 == null || trade.getState() != Trade.State.COUNTDOWN) {
                    cancel();
                    return;
                }

                if (remaining <= 0) {
                    completeTrade(trade);
                    cancel();
                    return;
                }

                p1.playSound(p1.getLocation(), finalSound, volume, pitch);
                p2.playSound(p2.getLocation(), finalSound, volume, pitch);
                p1.sendActionBar(net.md_5.bungee.api.chat.TextComponent.fromLegacyText("&e" + remaining));
                p2.sendActionBar(net.md_5.bungee.api.chat.TextComponent.fromLegacyText("&e" + remaining));

                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void completeTrade(Trade trade) {
        Player p1 = Bukkit.getPlayer(trade.getPlayer1());
        Player p2 = Bukkit.getPlayer(trade.getPlayer2());

        if (p1 != null && p2 != null) {
            for (org.bukkit.inventory.ItemStack item : trade.getItems(trade.getPlayer1())) {
                if (item != null) p2.getInventory().addItem(item);
            }
            for (org.bukkit.inventory.ItemStack item : trade.getItems(trade.getPlayer2())) {
                if (item != null) p1.getInventory().addItem(item);
            }
            MessageUtil.sendTitle(p1, "trade-completed", p2.getName());
            MessageUtil.sendTitle(p2, "trade-completed", p1.getName());
            p1.closeInventory();
            p2.closeInventory();
        }

        trade.setState(Trade.State.COMPLETED);
        endTrade(trade);
    }

    public void cancelTrade(Trade trade, String reasonKey, String actorName) {
        Player p1 = Bukkit.getPlayer(trade.getPlayer1());
        Player p2 = Bukkit.getPlayer(trade.getPlayer2());

        if (p1 != null) {
            MessageUtil.sendTitle(p1, reasonKey, actorName);
            p1.closeInventory();
        }
        if (p2 != null) {
            MessageUtil.sendTitle(p2, reasonKey, actorName);
            p2.closeInventory();
        }

        trade.setState(Trade.State.CANCELLED);
        endTrade(trade);
    }

    private void endTrade(Trade trade) {
        activeTrades.remove(trade.getPlayer1());
        activeTrades.remove(trade.getPlayer2());
    }
                         }
