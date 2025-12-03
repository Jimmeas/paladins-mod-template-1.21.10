package com.jimmeas.paladinsmod.ability.impl;

import com.jimmeas.paladinsmod.ability.Ability;
import com.jimmeas.paladinsmod.entity.GrenadeEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class FragGrenadeAbility extends Ability {

    public FragGrenadeAbility() {
        super("Frag Grenade", 8, AbilityType.INSTANT); // 8 second cooldown
    }

    @Override
    public boolean activate(ServerPlayer player) {
        if (!canUse(player)) return false;

        // Get player's look direction
        Vec3 lookVec = player.getLookAngle();

        // Spawn grenade entity
        GrenadeEntity grenade = new GrenadeEntity(player.level(), player);

        // Set grenade velocity (throw speed)
        double throwPower = 1.5;
        grenade.setDeltaMovement(
                lookVec.x * throwPower,
                lookVec.y * throwPower + 0.2, // Add slight upward arc
                lookVec.z * throwPower
        );

        // Spawn grenade in world
        player.level().addFreshEntity(grenade);

        // Play throw sound
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS,
                1.0F,
                0.8F
        );

        return true;
    }
}