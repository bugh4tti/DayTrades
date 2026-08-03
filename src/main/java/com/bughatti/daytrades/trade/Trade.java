package com.bughatti.daytrades.trade;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Trade {

    public enum State { PENDING, ACTIVE, COUNTDOWN, COMPLETED, CANCELLED }

    private final UUID player1;
    private final UUID player2;

    private ItemStack[] itemsPlayer1 = new ItemStack[9];
    private ItemStack[] itemsPlayer2 = new ItemStack[9];

    private boolean acceptedPlayer1 = false;
    private boolean acceptedPlayer2 = false;

    private State state = State.PENDING;
    private int countdownTaskId = -1;

    private com.bughatti.daytrades.gui.TradeGUI gui;

    public Trade(UUID player1, UUID player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }

    public UUID getOther(UUID uuid) {
        return uuid.equals(player1) ? player2 : player1;
    }

    public boolean isParticipant(UUID uuid) {
        return uuid.equals(player1) || uuid.equals(player2);
    }

    public void setAccepted(UUID uuid, boolean value) {
        if (uuid.equals(player1)) acceptedPlayer1 = value;
        else if (uuid.equals(player2)) acceptedPlayer2 = value;
    }

    public boolean isAccepted(UUID uuid) {
        return uuid.equals(player1) ? acceptedPlayer1 : acceptedPlayer2;
    }

    public boolean bothAccepted() {
        return acceptedPlayer1 && acceptedPlayer2;
    }

    public void resetAcceptance() {
        acceptedPlayer1 = false;
        acceptedPlayer2 = false;
    }

    public ItemStack[] getItems(UUID uuid) {
        return uuid.equals(player1) ? itemsPlayer1 : itemsPlayer2;
    }

    public void setItem(UUID uuid, int index, ItemStack item) {
        if (uuid.equals(player1)) itemsPlayer1[index] = item;
        else itemsPlayer2[index] = item;
    }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public int getCountdownTaskId() { return countdownTaskId; }
    public void setCountdownTaskId(int id) { this.countdownTaskId = id; }

    public com.bughatti.daytrades.gui.TradeGUI getGui() { return gui; }
    public void setGui(com.bughatti.daytrades.gui.TradeGUI gui) { this.gui = gui; }
    }
