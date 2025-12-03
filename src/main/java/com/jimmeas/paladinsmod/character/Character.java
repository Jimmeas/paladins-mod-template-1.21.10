package com.jimmeas.paladinsmod.character;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public abstract class Character {
    protected final String id;
    protected final String name;
    protected final Ability[] abilities;
    protected final Map<Integer, Long> cooldowns;

    public Character(String id, String name) {
        this.id = id;
        this.name = name;
        this.abilities = new Ability[4]; // 3 abilities + 1 ultimate
        this.cooldowns = new HashMap<>();
    }

    /**
     * Initialize abilities for this character
     * Override this in character implementations
     */
    protected abstract void setupAbilities();

    /**
     * Called when player selects this character
     */
    public void onEquip(ServerPlayer player) {
        setupAbilities();
        resetCooldowns();
    }

    /**
     * Called when player switches away from this character
     */
    public void onUnequip(ServerPlayer player) {
        // Clean up any active effects
        for (Ability ability : abilities) {
            if (ability != null) {
                ability.onUnequip(player);
            }
        }
    }

    /**
     * Use ability in given slot
     */
    public void useAbility(ServerPlayer player, int slot) {
        if (slot < 0 || slot >= abilities.length) return;

        Ability ability = abilities[slot];
        if (ability == null) return;

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        Long cooldownEnd = cooldowns.get(slot);

        if (cooldownEnd != null && currentTime < cooldownEnd) {
            // Still on cooldown
            return;
        }

        // Activate ability
        if (ability.activate(player)) {
            // Set cooldown
            cooldowns.put(slot, currentTime + (ability.getCooldown() * 1000L));

            // Sync cooldown to client (implement packet sending)
            syncCooldownToClient(player, slot, ability.getCooldown());
        }
    }

    /**
     * Get remaining cooldown for ability slot (in seconds)
     */
    public float getRemainingCooldown(int slot) {
        Long cooldownEnd = cooldowns.get(slot);
        if (cooldownEnd == null) return 0;

        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000f : 0;
    }

    /**
     * Reset all cooldowns
     */
    public void resetCooldowns() {
        cooldowns.clear();
    }

    /**
     * Tick method called every game tick
     */
    public void tick(ServerPlayer player) {
        for (Ability ability : abilities) {
            if (ability != null) {
                ability.tick(player);
            }
        }
    }

    protected void setAbility(int slot, Ability ability) {
        if (slot >= 0 && slot < abilities.length) {
            abilities[slot] = ability;
        }
    }

    private void syncCooldownToClient(ServerPlayer player, int slot, int cooldown) {
        // TODO: Implement packet to sync cooldown to client
        // Use ServerPlayNetworking.send()
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Ability getAbility(int slot) {
        if (slot >= 0 && slot < abilities.length) {
            return abilities[slot];
        }
        return null;
    }
}