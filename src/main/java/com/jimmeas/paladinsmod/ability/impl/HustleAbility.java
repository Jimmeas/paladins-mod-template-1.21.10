package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class HustleAbility extends Ability {
    private static final UUID SPEED_UUID = UUID.fromString("b9d8e3f4-7a6b-5c4d-3e2f-1a0b9c8d7e6f");
    private static final int DURATION_TICKS = 40; // 2 seconds (20 ticks = 1 second)

    public HustleAbility() {
        super("Hustle", 8, AbilityType.INSTANT); // 8 second cooldown
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canUse(player)) return false;

        // Apply speed boost
        var modifier = new EntityAttributeModifier(
                SPEED_UUID,
                "hustle_speed",
                0.5, // 50% speed increase
                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        );

        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .addTemporaryModifier(modifier);

        // Apply visual effect (Speed particles)
        player.addStatusEffect(new MobEffectInstance(
                MobEffects.SPEED,
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
                    player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                            .removeModifier(SPEED_UUID);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // Remove speed boost if character is switched
        player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .removeModifier(SPEED_UUID);
    }
}