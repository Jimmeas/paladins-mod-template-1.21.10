package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class IronSightsAbility extends Ability {
    private static final UUID SLOWNESS_UUID = UUID.fromString("a8c79f2e-8f3a-4b2d-9c1e-5a6b7c8d9e0f");
    private boolean isActive = false;

    public IronSightsAbility() {
        super("Iron Sights", 0, AbilityType.TOGGLE); // No cooldown, toggle ability
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canUse(player)) return false;

        isActive = !isActive;

        if (isActive) {
            // Apply slowness while aiming
            var modifier = new EntityAttributeModifier(
                    SLOWNESS_UUID,
                    "iron_sights_slowness",
                    -0.3, // 30% slower
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL
            );

            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    .addTemporaryModifier(modifier);

            // TODO: Send packet to client to reduce FOV
            // This would need client-side handling to change FOV

        } else {
            // Remove slowness
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    .removeModifier(SLOWNESS_UUID);

            // TODO: Send packet to client to restore FOV
        }

        return true;
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // Make sure to remove effect when switching characters
        if (isActive) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                    .removeModifier(SLOWNESS_UUID);
            isActive = false;
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        // Could add accuracy bonus logic here
        // For example, reduce arrow spread when active
    }
}