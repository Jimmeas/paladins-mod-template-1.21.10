package com.jimmeas.paladinsmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import java.util.List;

public class GrenadeEntity extends ThrowableItemProjectile {
    private static final int FUSE_TIME = 60; // 3 seconds (20 ticks = 1 second)
    private static final float EXPLOSION_POWER = 3.0F;
    private static final double DAMAGE_RADIUS = 6.0;

    private int fuseTicks = FUSE_TIME;

    public GrenadeEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public GrenadeEntity(Level world, LivingEntity owner) {
        super(EntityType.SNOWBALL, world);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE; // Visual representation
    }

    @Override
    public void tick() {
        super.tick();

        // Countdown fuse
        fuseTicks--;

        // Spawn particles
        if (this.level().isClientSide) {
            this.level().addParticle(
                    ParticleTypes.SMOKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0, 0.05, 0
            );
        }

        // Explode when fuse runs out
        if (fuseTicks <= 0 && !this.level().isClientSide) {
            explode();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);

        // Bounce off walls instead of exploding immediately
        // The grenade will explode after fuse time regardless
    }

    private void explode() {
        if (this.level().isClientSide) return;

        // Create explosion effect (no terrain damage)
        // Create explosion effect (no terrain damage)
        this.level().explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                EXPLOSION_POWER,
                false, // Don't break blocks
                Level.ExplosionInteraction.NONE
        );

// Add extra explosion particles for visual effect
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Large explosion particle burst
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    3, // Multiple explosion particles
                    0, 0, 0,
                    0
            );

            // Smoke cloud
            for (int i = 0; i < 50; i++) {
                double offsetX = (Math.random() - 0.5) * 4;
                double offsetY = (Math.random() - 0.5) * 4;
                double offsetZ = (Math.random() - 0.5) * 4;

                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1,
                        0, 0.1, 0,
                        0.05
                );
            }

            // Fire particles
            for (int i = 0; i < 30; i++) {
                double offsetX = (Math.random() - 0.5) * 3;
                double offsetY = (Math.random() - 0.5) * 3;
                double offsetZ = (Math.random() - 0.5) * 3;

                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        1,
                        0, 0.1, 0,
                        0.05
                );
            }
        }


        // Play explosion sound
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );

        // Damage nearby entities
        AABB damageBox = new AABB(
                this.getX() - DAMAGE_RADIUS,
                this.getY() - DAMAGE_RADIUS,
                this.getZ() - DAMAGE_RADIUS,
                this.getX() + DAMAGE_RADIUS,
                this.getY() + DAMAGE_RADIUS,
                this.getZ() + DAMAGE_RADIUS
        );

        List<LivingEntity> nearbyEntities = this.level()
                .getEntitiesOfClass(LivingEntity.class, damageBox, entity -> {
                    return entity.isAlive() && entity.distanceTo(this) <= DAMAGE_RADIUS;
                });

        // Apply damage based on distance
        for (LivingEntity entity : nearbyEntities) {
            double distance = entity.distanceTo(this);
            float damage = (float) (10.0 * (1.0 - distance / DAMAGE_RADIUS)); // Max 10 damage at center

            if (damage > 0) {
                entity.hurt(this.damageSources().explosion(this, this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null), damage);
            }
        }

        // Remove grenade entity
        this.discard();
    }
}