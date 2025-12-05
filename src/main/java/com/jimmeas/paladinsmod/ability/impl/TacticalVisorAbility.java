package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import com.jimmeas.paladinsmod.PaladinsMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Victor's Ultimate Ability - Tactical Visor
 * Charges by dealing damage with gun and grenades
 * When activated: marks enemies with glowing effect
 */
public class TacticalVisorAbility extends Ability {

    private static final int DURATION_TICKS = 120; // 6 seconds
    private static final double DETECTION_RANGE = 30.0;
    private static final float ULT_CHARGE_REQUIRED = 1500.0F; // Ult charge
    private static final float FIRE_RATE_MULTIPLIER = 3.0F; // 3x fire rate during ult

    // Track active ults and charge per player
    private static final Map<UUID, Integer> activeUltTicks = new HashMap<>();
    private static final Map<UUID, Float> ultCharge = new HashMap<>();
    private static final Map<UUID, Boolean> ultActive = new HashMap<>();

    public TacticalVisorAbility() {
        super("Tactical Visor", 0, AbilityType.CHANNELED); // No traditional cooldown, charge-based
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canUse(player)) return false;

        UUID playerUUID = player.getUUID();
        float currentCharge = ultCharge.getOrDefault(playerUUID, 0.0F);

        // Check if ult is charged
        if (currentCharge < ULT_CHARGE_REQUIRED) {
            player.displayClientMessage(
                    Component.literal("§c§lUltimate not ready! " +
                            (int)currentCharge + "/" + (int)ULT_CHARGE_REQUIRED),
                    true
            );
            return false;
        }

        // Activate ultimate
        activeUltTicks.put(playerUUID, DURATION_TICKS);
        ultCharge.put(playerUUID, 0.0F); // Reset charge
        ultActive.put(playerUUID, true); // Mark as active

        // Update XP bar to show it's empty
        updateXPBar(player);

        // Play activation sound
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS,
                1.0F,
                1.5F
        );

        // Give night vision to see better
        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                DURATION_TICKS,
                0,
                false,
                false
        ));

        // Display activation message
        player.displayClientMessage(
                Component.literal("§6§l⚡ TACTICAL VISOR ACTIVATED ⚡"),
                true
        );

        return true;
    }

    @Override
    public void tick(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        Integer ticksRemaining = activeUltTicks.get(playerUUID);

        // If ult is not active, just return
        if (ticksRemaining == null || ticksRemaining <= 0) {
            activeUltTicks.remove(playerUUID);
            return;
        }

        // Countdown
        ticksRemaining--;
        activeUltTicks.put(playerUUID, ticksRemaining);

        // Update XP bar to show remaining duration
        updateUltDurationXPBar(player, ticksRemaining);

        // Mark nearby enemies with glowing effect
        AABB searchBox = new AABB(
                player.getX() - DETECTION_RANGE,
                player.getY() - DETECTION_RANGE,
                player.getZ() - DETECTION_RANGE,
                player.getX() + DETECTION_RANGE,
                player.getY() + DETECTION_RANGE,
                player.getZ() + DETECTION_RANGE
        );

        List<LivingEntity> nearbyEntities = player.level()
                .getEntitiesOfClass(LivingEntity.class, searchBox, entity -> {
                    return entity != player &&
                            entity.isAlive() &&
                            !entity.isAlliedTo(player) &&
                            entity.distanceTo(player) <= DETECTION_RANGE;
                });

        // Apply glowing effect to enemies
        for (LivingEntity entity : nearbyEntities) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    40, // 2 seconds (refreshed each tick)
                    0,
                    false,
                    false
            ));
        }

        // Show remaining duration every second
        if (ticksRemaining % 20 == 0) {
            int secondsLeft = ticksRemaining / 20;
            player.displayClientMessage(
                    Component.literal("§6Tactical Visor: " + secondsLeft + "s"),
                    true
            );
        }

        // Deactivation message
        if (ticksRemaining == 0) {
            player.displayClientMessage(
                    Component.literal("§7Tactical Visor ended"),
                    true
            );
            ultActive.put(playerUUID, false);
            updateXPBar(player); // Reset XP bar to show charge
        }
    }

    @Override
    public void onUnequip(ServerPlayer player) {
        // Remove active ult if character is switched
        UUID playerUUID = player.getUUID();
        activeUltTicks.remove(playerUUID);
    }

    /**
     * Check if player's ult is currently active
     */
    public static boolean isUltActive(UUID playerUUID) {
        return ultActive.getOrDefault(playerUUID, false);
    }

    /**
     * Get fire rate multiplier (used by rifle)
     */
    public static float getFireRateMultiplier(UUID playerUUID) {
        return isUltActive(playerUUID) ? FIRE_RATE_MULTIPLIER : 1.0F;
    }

    /**
     * Update player's XP bar to show ult charge
     */
    private static void updateXPBar(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        float currentCharge = ultCharge.getOrDefault(playerUUID, 0.0F);
        float chargePercent = currentCharge / ULT_CHARGE_REQUIRED;

        // Set XP bar (0.0 to 1.0)
        player.experienceProgress = chargePercent;

        // Set XP level to show percentage
        player.experienceLevel = (int)(chargePercent * 100);
    }

    /**
     * Update XP bar to show remaining ult duration
     */
    private static void updateUltDurationXPBar(ServerPlayer player, int ticksRemaining) {
        float durationPercent = (float)ticksRemaining / DURATION_TICKS;

        // Set XP bar to show remaining duration
        player.experienceProgress = durationPercent;
        player.experienceLevel = ticksRemaining / 20; // Show seconds remaining
    }

    /**
     * Public method to activate ult (called by item)
     */
    public static boolean activateUltimate(ServerPlayer player) {
        TacticalVisorAbility ability = new TacticalVisorAbility();
        return ability.activate(player);
    }

    /**
     * Add ult charge and update XP bar
     */
    public static void addUltCharge(ServerPlayer player, float damageDealt) {
        UUID playerUUID = player.getUUID();

        // Don't add charge if ult is active
        if (isUltActive(playerUUID)) {
            return;
        }

        float currentCharge = ultCharge.getOrDefault(playerUUID, 0.0F);
        float newCharge = Math.min(currentCharge + damageDealt, ULT_CHARGE_REQUIRED);

        ultCharge.put(playerUUID, newCharge);

        // Update XP bar
        updateXPBar(player);

        // Notify when ult is ready
        if (currentCharge < ULT_CHARGE_REQUIRED && newCharge >= ULT_CHARGE_REQUIRED) {
            player.displayClientMessage(
                    Component.literal("§6§l⚡ ULTIMATE READY! ⚡"),
                    false
            );

            player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS,
                    0.5F,
                    2.0F
            );
        }
    }

    /**
     * Get current ult charge percentage
     */
    public static float getUltChargePercent(UUID playerUUID) {
        float currentCharge = ultCharge.getOrDefault(playerUUID, 0.0F);
        return (currentCharge / ULT_CHARGE_REQUIRED) * 100.0F;
    }

    /**
     * Check if ult is ready
     */
    public static boolean isUltReady(UUID playerUUID) {
        return ultCharge.getOrDefault(playerUUID, 0.0F) >= ULT_CHARGE_REQUIRED;
    }

    /**
     * Update XP bar if needed
     */
    public static void updateXPBarIfNeeded(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        if (!isUltActive(playerUUID)) {
            updateXPBar(player);
        }
    }
}