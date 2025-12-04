package com.jimmeas.paladinsmod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;

import java.util.List;

public class VictorRifleItem extends Item {
    private static final int FIRE_RATE_COOLDOWN = 4; // 5 shots per second (4 ticks between shots)
    private static final int RELOAD_COOLDOWN = 32; // 1.6 seconds (32 ticks)
    private static final int MAX_AMMO = 50;
    private static final float DAMAGE = 6.0F;
    private static final double MAX_RANGE = 50.0;

    public VictorRifleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Get current ammo
        int currentAmmo = getAmmo(itemStack);

        // Check if reloading
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(itemStack);
        }

        // Check if need to reload
        if (currentAmmo <= 0) {
            // Start reload
            player.getCooldowns().addCooldown(this, RELOAD_COOLDOWN);
            setAmmo(itemStack, MAX_AMMO);

            // Play reload sound
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.2F
            );

            return InteractionResultHolder.success(itemStack);
        }

        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            // Shoot the gun
            shootBullet(serverWorld, player);

            // Consume ammo
            setAmmo(itemStack, currentAmmo - 1);

            // Play gun sound
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ARROW_SHOOT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.5F
            );

            // Set fire rate cooldown
            player.getCooldowns().addCooldown(this, FIRE_RATE_COOLDOWN);

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private void shootBullet(ServerLevel world, Player player) {
        // Get shoot direction
        Vec3 startPos = player.getEyePosition();
        Vec3 lookAngle = player.getLookAngle();
        Vec3 endPos = startPos.add(lookAngle.scale(MAX_RANGE));

        // Raycast for blocks
        BlockHitResult blockHit = world.clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        Vec3 actualEndPos = blockHit.getType() == HitResult.Type.MISS ? endPos : blockHit.getLocation();

        // Check for entity hits along the ray
        Vec3 direction = actualEndPos.subtract(startPos).normalize();
        double distance = startPos.distanceTo(actualEndPos);

        AABB searchBox = new AABB(startPos, actualEndPos).inflate(1.0);
        List<LivingEntity> possibleTargets = world.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        LivingEntity hitEntity = null;
        double closestDistance = distance;

        // Find closest entity in the line of fire
        for (LivingEntity target : possibleTargets) {
            AABB targetBox = target.getBoundingBox().inflate(0.3);
            Vec3 hitPos = targetBox.clip(startPos, actualEndPos).orElse(null);

            if (hitPos != null) {
                double entityDistance = startPos.distanceTo(hitPos);
                if (entityDistance < closestDistance) {
                    closestDistance = entityDistance;
                    hitEntity = target;
                    actualEndPos = hitPos;
                }
            }
        }

        // Damage the hit entity
        if (hitEntity != null) {
            DamageSource damageSource = world.damageSources().playerAttack(player);
            hitEntity.hurt(damageSource, DAMAGE);
        }

        // Spawn particle trail
        spawnBulletTrail(world, startPos, actualEndPos);
    }

    private void spawnBulletTrail(ServerLevel world, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 5);

        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 particlePos = start.add(direction.scale(progress));

            world.sendParticles(
                    ParticleTypes.CRIT,
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    // Ammo management using NBT
    // Ammo management using NBT
// Ammo management using NBT
    // Ammo management using data components
    private int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (!tag.contains("Ammo")) {
            return MAX_AMMO; // Start with full ammo
        }
        return tag.getInt("Ammo");
    }

    private void setAmmo(ItemStack stack, int ammo) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Ammo", ammo);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true; // Always show durability bar
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int ammo = getAmmo(stack);
        return Math.round(13.0F * ammo / MAX_AMMO); // Bar width based on ammo
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int ammo = getAmmo(stack);
        if (ammo <= 10) {
            return 0xFF0000; // Red when low
        } else if (ammo <= 25) {
            return 0xFFFF00; // Yellow when medium
        }
        return 0x00FF00; // Green when high
    }
}