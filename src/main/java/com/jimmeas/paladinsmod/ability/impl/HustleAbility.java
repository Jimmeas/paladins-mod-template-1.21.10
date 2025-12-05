package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.server.level.ServerPlayer;

/**
 * Victor's passive mobility ability
 * Victor can use native Minecraft sprint (Ctrl/Double-tap W)
 * Other characters cannot sprint at all
 */
public class HustleAbility extends Ability {

    public HustleAbility() {
        super("Hustle", 0, AbilityType.PASSIVE); // No cooldown, passive ability
    }

    @Override
    public boolean activate(ServerPlayer player) {
        // Passive ability - sprint is always available for Victor
        // No activation needed, just having this ability allows sprinting
        return true;
    }

    @Override
    public void tick(ServerPlayer player) {
        // No tick logic needed - native Minecraft sprint handles everything
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // When Victor is unequipped, sprint should be disabled
        // This will be handled by a global sprint restriction system
    }
}