package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import com.jimmeas.paladinsmod.PaladinsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IronSightsAbility extends Ability {
    // Track who is aiming
    private static final Map<UUID, Boolean> isAiming = new HashMap<>();

    public IronSightsAbility() {
        super("Iron Sights", 0, AbilityType.TOGGLE); // No cooldown, toggle ability
    }

    @Override
    public boolean activate(ServerPlayer player) {
        // This ability is passive - activated by crouching
        // The actual logic is in the tick() method
        return true;
    }

    @Override
    public void tick(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        boolean isCrouching = player.isCrouching();
        boolean wasAiming = isAiming.getOrDefault(playerUUID, false);

        // Check if player is holding Victor's rifle
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.getItem() != PaladinsMod.VICTOR_RIFLE) {
            // Not holding rifle, remove iron sights if active
            if (wasAiming) {
                isAiming.put(playerUUID, false);
            }
            return;
        }

        // Can't aim while sprinting or reloading
        boolean isReloading = player.getCooldowns().isOnCooldown(PaladinsMod.VICTOR_RIFLE);
        boolean isSprinting = player.isSprinting();

        if (isReloading || isSprinting) {
            if (wasAiming) {
                // Cancel iron sights
                isAiming.put(playerUUID, false);
            }
            return;
        }

        // Update aiming state based on crouch
        if (isCrouching && !wasAiming) {
            // Start aiming
            isAiming.put(playerUUID, true);
        } else if (!isCrouching && wasAiming) {
            // Stop aiming
            isAiming.put(playerUUID, false);
        }
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // Make sure to remove aiming state when switching characters
        isAiming.put(player.getUUID(), false);
    }

    // Public method to check if player is aiming (used by rifle and renderer)
    public static boolean isPlayerAiming(UUID playerUUID) {
        return isAiming.getOrDefault(playerUUID, false);
    }
}