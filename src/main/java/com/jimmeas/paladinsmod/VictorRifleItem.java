package com.jimmeas.paladinsmod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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

import java.util.List;

public class VictorRifleItem extends Item {
    private static final int FIRE_RATE_COOLDOWN = 4; // 5 shots per second (4 ticks between shots)
    private static final float DAMAGE = 6.0F;
    private static final double MAX_RANGE = 50.0; // 50 block range

    public VictorRifleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
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
            spawnBulletTrail(serverWorld, startPos, actualEndPos);

            // Play gun sound
            world.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ARROW_SHOOT,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.5F // Higher pitch for gun sound
            );

            // Set cooldown
            player.getCooldowns().addCooldown(this, FIRE_RATE_COOLDOWN);

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private void spawnBulletTrail(ServerLevel world, Vec3 start, Vec3 end) {
        // Create particle line from gun to hit point
        Vec3 direction = end.subtract(start);
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 5); // 5 particles per block

        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 particlePos = start.add(direction.scale(progress));

            // Spawn white smoke particles for bullet trail
            world.sendParticles(
                    ParticleTypes.CRIT, // You can change this to other particles
                    particlePos.x,
                    particlePos.y,
                    particlePos.z,
                    1, // particle count
                    0, 0, 0, // offset
                    0 // speed
            );
        }
    }
}