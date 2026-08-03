package com.bughatti.daytrades.listeners;

import com.bughatti.daytrades.DayTrades;
import com.bughatti.daytrades.gui.TradeGUI;
import com.bughatti.daytrades.trade.Trade;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class TradeGUIListener implements Listener {

    private final DayTrades plugin;

    public TradeGUIListener(DayTrades plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TradeGUI.TradeGUIHolder holder)) return;

        Trade trade = holder.getTrade();
        TradeGUI gui = trade.getGui();
        Player player = (Player) event.getWhoClicked();

        var cfg = plugin.getConfig();
        int acceptSlot = cfg.getInt("gui.accept-button.slot", 49);
        int cancelSlot = cfg.getInt("gui.cancel-button.slot", 45);
        int rawSlot = event.getRawSlot();

        // Si el trade está en countdown, solo se permite tocar el botón de cancelar
        if (trade.getState() == Trade.State.COUNTDOWN) {
            if (rawSlot != cancelSlot) {
                event.setCancelled(true);
                return;
            }
            event.setCancelled(true);
            plugin.getTradeManager().cancelTrade(trade, "trade-cancelled-manual", player.getName());
            return;
        }

        // Botón de aceptar
        if (rawSlot == acceptSlot) {
            event.setCancelled(true);
            plugin.getTradeManager().toggleAccept(player, trade);
            gui.resetAcceptVisual(player.getUniqueId(), trade.isAccepted(player.getUniqueId()));
            return;
        }

        // Botón de cancelar
        if (rawSlot == cancelSlot) {
            event.setCancelled(true);
            plugin.getTradeManager().cancelTrade(trade, "trade-cancelled-manual", player.getName());
            return;
        }

        // Si el jugador clickea en el inventario de SU personaje (parte de abajo), lo dejamos pasar normal
        if (rawSlot >= event.getInventory().getSize()) {
            return;
        }

        // Si clickea en el lado del "otro" jugador, bloquear siempre
        if (gui.getOtherSlots().contains(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        // Si clickea en un slot que no es "your-side" (ej. borde), bloquear
        if (!gui.getYourSlots().contains(rawSlot)) {
            event.setCancelled(true);
            return;
        }

        // Click válido en "your-side": dejar que el evento se procese normalmente,
        // y sincronizar el resultado un tick después (para que el ítem ya esté puesto)
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack item = event.getInventory().getItem(rawSlot);
            gui.syncItem(player.getUniqueId(), rawSlot, item);

            // Si alguien ya había aceptado y ahora mueve un ítem, se desmarca su aceptación
            if (trade.isAccepted(player.getUniqueId())) {
                trade.setAccepted(player.getUniqueId(), false);
                gui.resetAcceptVisual(player.getUniqueId(), false);
            }
        });
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Trade trade = plugin.getTradeManager().getTrade(event.getPlayer().getUniqueId());
        if (trade != null && trade.getState() != Trade.State.COMPLETED && trade.getState() != Trade.State.CANCELLED) {
            plugin.getTradeManager().cancelTrade(trade, "trade-cancelled-manual", event.getPlayer().getName());
        }
    }
                }
