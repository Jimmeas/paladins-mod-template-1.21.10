package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class HustleAbility extends Ability {
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath("paladinsmod", "hustle_speed");
    private static final int DURATION_TICKS = 40; // 2 seconds (20 ticks = 1 second)

    public HustleAbility() {
        super("Hustle", 8, AbilityType.INSTANT); // 8 second cooldown
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canUse(player)) return false;

        // Apply speed boost
        var modifier = new AttributeModifier(
                SPEED_ID,
                0.5, // 50% speed increase
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );

        player.getAttribute(Attributes.MOVEMENT_SPEED)
                .addTransientModifier(modifier);

        // Apply visual effect (Speed particles)
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                DURATION_TICKS,
                0,
                false,
                true
        ));

        // Schedule removal of speed boost
        scheduleSpeedRemoval(player, DURATION_TICKS);

        return true;
    }

    private void scheduleSpeedRemoval(ServerPlayer player, int ticks) {
        // Use a simple counter-based system
        new Thread(() -> {
            try {
                Thread.sleep(ticks * 50L); // 50ms per tick
                if (player.isAlive()) {
                    player.getAttribute(Attributes.MOVEMENT_SPEED)
                            .removeModifier(SPEED_ID);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // Remove speed boost if character is switched
        player.getAttribute(Attributes.MOVEMENT_SPEED)
                .removeModifier(SPEED_ID);
    }
}