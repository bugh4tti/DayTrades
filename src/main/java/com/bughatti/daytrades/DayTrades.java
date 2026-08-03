package com.bughatti.daytrades;

import com.bughatti.daytrades.commands.TradeCommand;
import com.bughatti.daytrades.listeners.TradeGUIListener;
import com.bughatti.daytrades.trade.TradeManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DayTrades extends JavaPlugin {

    private static DayTrades instance;
    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.tradeManager = new TradeManager(this);

        getCommand("dt").setExecutor(new TradeCommand(this));
        getServer().getPluginManager().registerEvents(new TradeGUIListener(this), this);

        getLogger().info("DayTrades habilitado correctamente.");
    }

    @Override
    public void onDisable() {
        getLogger().info("DayTrades deshabilitado.");
    }

    public static DayTrades getInstance() {
        return instance;
    }

    public TradeManager getTradeManager() {
        return tradeManager;
    }
}
