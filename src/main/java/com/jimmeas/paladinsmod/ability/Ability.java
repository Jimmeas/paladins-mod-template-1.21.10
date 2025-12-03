package com.jimmeas.paladinsmod.ability;

import net.minecraft.server.level.ServerPlayer;

public abstract class Ability {
    protected final String name;
    protected final int cooldown; // in seconds
    protected final AbilityType type;

    public enum AbilityType {
        INSTANT,    // Activated once
        TOGGLE,     // Toggle on/off
        CHANNELED,  // Hold to use
        PASSIVE     // Always active
    }

    public Ability(String name, int cooldown, AbilityType type) {
        this.name = name;
        this.cooldown = cooldown;
        this.type = type;
    }

    /**
     * Called when ability is activated
     * @return true if activation was successful
     */
    public abstract boolean activate(ServerPlayer player);

    /**
     * Called every game tick while ability is active
     */
    public void tick(ServerPlayer player) {
        // Override if needed
    }

    /**
     * Called when character is unequipped
     */
    public void onUnequip(ServerPlayer player) {
        // Override to clean up effects
    }

    /**
     * Check if ability can be used
     */
    public boolean canUse(ServerPlayer player) {
        return !player.isSpectator() && player.isAlive();
    }

    public String getName() {
        return name;
    }

    public int getCooldown() {
        return cooldown;
    }

    public AbilityType getType() {
        return type;
    }
}