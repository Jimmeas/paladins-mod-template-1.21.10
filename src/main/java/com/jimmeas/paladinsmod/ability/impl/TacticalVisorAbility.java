package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class TacticalVisorAbility extends Ability {
    private static final int DURATION_TICKS = 120; // 6 seconds
    private static final double DETECTION_RANGE = 30.0;
    private int activeTicks = 0;
    private boolean isActive = false;

    public TacticalVisorAbility() {
        super("Tactical Visor", 60, AbilityType.CHANNELED); // 60 second cooldown
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canUse(player)) return false;

        isActive = true;
        activeTicks = DURATION_TICKS;

        // Play activation sound
        player.getWorld().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BLOCK_BEACON_ACTIVATE,
                SoundSource.PLAYERS,
                1.0F,
                1.5F
        );

        // Give glowing effect to see enemies through walls
        player.addStatusEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                DURATION_TICKS,
                0,
                false,
                false
        ));

        return true;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (!isActive) return;

        activeTicks--;

        if (activeTicks <= 0) {
            isActive = false;
            return;
        }

        // Mark nearby enemies with glowing effect
        AABB searchAABB = AABB.of(
                player.getPos(),
                DETECTION_RANGE * 2,
                DETECTION_RANGE * 2,
                DETECTION_RANGE * 2
        );

        List<LivingEntity> nearbyEntities = player.getWorld()
                .getEntitiesByClass(LivingEntity.class, searchAABB, entity -> {
                    return entity != player &&
                            entity.isAlive() &&
                            !entity.isTeammate(player) &&
                            entity.distanceTo(player) <= DETECTION_RANGE;
                });

        // Apply glowing effect to enemies
        for (LivingEntity entity : nearbyEntities) {
            entity.addStatusEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    40, // 2 seconds (refreshed each tick)
                    0,
                    false,
                    false
            ));
        }

        // TODO: Client-side auto-aim assistance would be implemented here
        // This would require custom projectile handling or bow mechanics
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        isActive = false;
        activeTicks = 0;
    }
}