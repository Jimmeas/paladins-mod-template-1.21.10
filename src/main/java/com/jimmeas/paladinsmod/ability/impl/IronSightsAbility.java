package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class IronSightsAbility extends Ability {
    private static final ResourceLocation SLOWNESS_ID = ResourceLocation.fromNamespaceAndPath("paladinsmod", "iron_sights_slowness");
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
            var modifier = new AttributeModifier(
                    SLOWNESS_ID,
                    -0.3, // 30% slower
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            );

            player.getAttribute(Attributes.MOVEMENT_SPEED)
                    .addTransientModifier(modifier);

            // TODO: Send packet to client to reduce FOV
            // This would need client-side handling to change FOV

        } else {
            // Remove slowness
            player.getAttribute(Attributes.MOVEMENT_SPEED)
                    .removeModifier(SLOWNESS_ID);

            // TODO: Send packet to client to restore FOV
        }

        return true;
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // Make sure to remove effect when switching characters
        if (isActive) {
            player.getAttribute(Attributes.MOVEMENT_SPEED)
                    .removeModifier(SLOWNESS_ID);
            isActive = false;
        }
    }

    @Override
    public void tick(ServerPlayer player) {
        // Could add accuracy bonus logic here
        // For example, reduce arrow spread when active
    }
}