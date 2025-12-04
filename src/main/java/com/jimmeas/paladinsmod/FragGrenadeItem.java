package com.jimmeas.paladinsmod;

import com.jimmeas.paladinsmod.entity.GrenadeEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FragGrenadeItem extends Item {
    private static final int COOLDOWN_TICKS = 0; // 8 seconds (20 ticks = 1 second) (base is 160 ticks)
    private static final double THROW_POWER = 1.5;

    public FragGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide) {
            // Get player's look direction
            Vec3 lookAngle = player.getLookAngle();

            // Spawn grenade entity
            GrenadeEntity grenade = new GrenadeEntity(world, player);

            // Set grenade velocity (throw it)
            grenade.setDeltaMovement(
                    lookAngle.x * THROW_POWER,
                    lookAngle.y * THROW_POWER + 0.2, // Add slight upward arc
                    lookAngle.z * THROW_POWER
            );

            // Spawn grenade in world
            world.addFreshEntity(grenade);

            // Play throw sound
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.SNOWBALL_THROW,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.8F
            );

            // Set cooldown (8 seconds)
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }


}