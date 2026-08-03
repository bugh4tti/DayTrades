package com.bughatti.daytrades.listeners;

import com.bughatti.daytrades.DayTrades;
import com.bughatti.daytrades.gui.TradeGUI;
import com.bughatti.daytrades.trade.Trade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class TradeGUIListener implements Listener {

    private final DayTrades plugin;

    public TradeGUIListener(DayTrades plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeGUI.TradeGUIHolder holder)) return;

        Trade trade = holder.getTrade();
        Player player = (Player) event.getWhoClicked();

        var cfg = plugin.getConfig();
        int acceptSlot = cfg.getInt("gui.accept-button.slot", 49);
        int cancelSlot = cfg.getInt("gui.cancel-button.slot", 45);

        if (trade.getState() == Trade.State.COUNTDOWN && event.getRawSlot() != cancelSlot) {
            event.setCancelled(true);
            return;
        }

        if (event.getRawSlot() == acceptSlot) {
            event.setCancelled(true);
            plugin.getTradeManager().toggleAccept(player, trade);
            return;
        }

        if (event.getRawSlot() == cancelSlot) {
            event.setCancelled(true);
            plugin.getTradeManager().cancelTrade(trade, "trade-cancelled-manual", player.getName());
            return;
        }

        // Si se mueve un ítem durante countdown -> cancelar
        if (trade.getState() == Trade.State.COUNTDOWN) {
            plugin.getTradeManager().cancelTrade(trade, "trade-cancelled-item-move", player.getName());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeGUI.TradeGUIHolder holder)) return;
        Trade trade = holder.getTrade();
        if (trade.getState() != Trade.State.COMPLETED && trade.getState() != Trade.State.CANCELLED) {
            plugin.getTradeManager().cancelTrade(trade, "trade-cancelled-manual",
                    ((Player) event.getPlayer()).getName());
        }
    }
    }
