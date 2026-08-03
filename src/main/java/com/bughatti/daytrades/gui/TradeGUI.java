package com.bughatti.daytrades.gui;

import com.bughatti.daytrades.DayTrades;
import com.bughatti.daytrades.trade.Trade;
import com.bughatti.daytrades.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TradeGUI {

    private final DayTrades plugin;
    private final Trade trade;
    private Inventory inventory;

    public TradeGUI(DayTrades plugin, Trade trade) {
        this.plugin = plugin;
        this.trade = trade;
        build();
    }

    private void build() {
        var cfg = plugin.getConfig();
        int rows = cfg.getInt("gui.rows", 6);
        String title = MessageUtil.color(cfg.getString("gui.title", "&8Trade"));

        inventory = Bukkit.createInventory(new TradeGUIHolder(trade), rows * 9, title);

        placeBorder();
        placeButton("gui.accept-button", "material");
        placeButton("gui.cancel-button", "material");
    }

    private void placeBorder() {
        var cfg = plugin.getConfig();
        Material mat = Material.matchMaterial(cfg.getString("gui.border.material", "GRAY_STAINED_GLASS_PANE"));
        String name = MessageUtil.color(cfg.getString("gui.border.name", " "));

        ItemStack pane = new ItemStack(mat != null ? mat : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(name);
        pane.setItemMeta(meta);

        for (int slot : cfg.getIntegerList("gui.border.slots")) {
            inventory.setItem(slot, pane);
        }
    }

    private void placeButton(String path, String materialKey) {
        var cfg = plugin.getConfig();
        Material mat = Material.matchMaterial(cfg.getString(path + "." + materialKey, "BARRIER"));
        int slot = cfg.getInt(path + ".slot", 45);
        String name = MessageUtil.color(cfg.getString(path + ".name", " "));

        ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        inventory.setItem(slot, item);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Trade getTrade() {
        return trade;
    }

    public static class TradeGUIHolder implements org.bukkit.inventory.InventoryHolder {
        private final Trade trade;
        public TradeGUIHolder(Trade trade) { this.trade = trade; }
        public Trade getTrade() { return trade; }
        @Override public Inventory getInventory() { return null; }
    }
          }
