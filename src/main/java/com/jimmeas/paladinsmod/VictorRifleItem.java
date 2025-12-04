package com.jimmeas.paladinsmod;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VictorRifleItem extends Item {
    private static final int FIRE_RATE_COOLDOWN = 4; // 5 shots per second (4 ticks between shots) ****** (should be 10 shots persecond)
    private static final int RELOAD_COOLDOWN = 32; // 1.6 seconds (32 ticks)
    private static final int MAG_SIZE = 50;
    private static final float DAMAGE = 2.0F;
    private static final double MAX_RANGE = 50.0;

    // Recoil/spread system
    private static final float BASE_SPREAD = 0.5F;
    private static final float MAX_SPREAD = 8.0F;
    private static final float SPREAD_INCREASE_PER_SHOT = 1.0F;
    private static final float SPREAD_RECOVERY_RATE = 0.3F;
    private static final int RECOVERY_DELAY_TICKS = 10;

    // Track each player's state
    private static final Map<UUID, Float> playerSpread = new HashMap<>();
    private static final Map<UUID, Long> lastShotTime = new HashMap<>();
    private static final Map<UUID, Integer> playerAmmo = new HashMap<>();
    private static final Map<UUID, Boolean> isReloading = new HashMap<>();

    public VictorRifleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            UUID playerUUID = player.getUUID();

            // Initialize ammo if not set
            if (!playerAmmo.containsKey(playerUUID)) {
                playerAmmo.put(playerUUID, MAG_SIZE);
            }

            // Check if reloading
            if (isReloading.getOrDefault(playerUUID, false)) {
                return InteractionResultHolder.pass(itemStack);
            }

            int currentAmmo = playerAmmo.get(playerUUID);

            // Check if out of ammo
            if (currentAmmo <= 0) {
                // Auto-reload
                startReload(player, playerUUID);
                return InteractionResultHolder.pass(itemStack);
            }

            // Get current spread
            float currentSpread = playerSpread.getOrDefault(playerUUID, BASE_SPREAD);

            // Get shoot direction with spread
            Vec3 startPos = player.getEyePosition();
            Vec3 lookAngle = player.getLookAngle();

            // Apply random spread
            double spreadAmount = currentSpread * 0.017453292;
            double randomYaw = (Math.random() - 0.5) * spreadAmount;
            double randomPitch = (Math.random() - 0.5) * spreadAmount;

            Vec3 spreadLookAngle = rotateVector(lookAngle, randomYaw, randomPitch);
            Vec3 endPos = startPos.add(spreadLookAngle.scale(MAX_RANGE));

            // Raycast for blocks
            BlockHitResult blockHit = world.clip(new ClipContext(
                    startPos,
                    endPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player
            ));

            Vec3 actualEndPos = blockHit.getType() == HitResult.Type.MISS ? endPos : blockHit.getLocation();

            // Check for entity hits
            double distance = startPos.distanceTo(actualEndPos);

            AABB searchBox = new AABB(startPos, actualEndPos).inflate(1.0);
            List<LivingEntity> possibleTargets = world.getEntitiesOfClass(
                    LivingEntity.class,
                    searchBox,
                    entity -> entity != player && entity.isAlive()
            );

            LivingEntity hitEntity = null;
            double closestDistance = distance;

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
                // Check if headshot
                boolean isHeadshot = isHeadshot(hitEntity, actualEndPos);
                float finalDamage = isHeadshot ? DAMAGE * 1.5F : DAMAGE;

                // Store knockback to restore it after
                Vec3 originalDeltaMovement = hitEntity.getDeltaMovement();

                // Apply damage
                DamageSource damageSource = world.damageSources().playerAttack(player);
                hitEntity.hurt(damageSource, finalDamage);

                // Remove knockback by restoring original velocity
                hitEntity.setDeltaMovement(originalDeltaMovement);

                // Play headshot sound
                if (isHeadshot) {
                    world.playSound(
                            null,
                            hitEntity.getX(),
                            hitEntity.getY(),
                            hitEntity.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS,
                            1.0F,
                            2.0F // High pitch for "ping" sound
                    );

                    // Show headshot message to shooter
                    player.displayClientMessage(
                            Component.literal("§c§lHEADSHOT!"),
                            true
                    );
                }
            }

            // Spawn particle trail (skip first 2 blocks to not obscure vision)
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
                    1.5F
            );

            // Consume ammo
            playerAmmo.put(playerUUID, currentAmmo - 1);

            // Update action bar with ammo count
            player.displayClientMessage(
                    Component.literal("Ammo: " + (currentAmmo - 1) + "/" + MAG_SIZE),
                    true // Show in action bar
            );

            // Increase spread
            float newSpread = Math.min(currentSpread + SPREAD_INCREASE_PER_SHOT, MAX_SPREAD);
            playerSpread.put(playerUUID, newSpread);
            lastShotTime.put(playerUUID, world.getGameTime());

            // Set cooldown
            player.getCooldowns().addCooldown(this, FIRE_RATE_COOLDOWN);

            return InteractionResultHolder.success(itemStack);
        }

        return InteractionResultHolder.pass(itemStack);
    }

    private void startReload(Player player, UUID playerUUID) {
        isReloading.put(playerUUID, true);

        // Set reload cooldown
        player.getCooldowns().addCooldown(this, RELOAD_COOLDOWN);

        // Show reload message
        player.displayClientMessage(
                Component.literal("§eReloading..."),
                true
        );

        // Schedule reload completion (needs to be handled in tick)
        player.level().getServer().tell(new net.minecraft.server.TickTask(
                player.level().getServer().getTickCount() + RELOAD_COOLDOWN,
                () -> completeReload(player, playerUUID)
        ));
    }

    private void completeReload(Player player, UUID playerUUID) {
        playerAmmo.put(playerUUID, MAG_SIZE);
        isReloading.put(playerUUID, false);

        player.displayClientMessage(
                Component.literal("§aReloaded! Ammo: " + MAG_SIZE + "/" + MAG_SIZE),
                true
        );

        // Play reload sound
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.5F,
                1.2F
        );
    }

    // Manual reload with offhand (or you could use a keybind)
    public static void manualReload(Player player) {
        UUID playerUUID = player.getUUID();

        if (isReloading.getOrDefault(playerUUID, false)) {
            return; // Already reloading
        }

        int currentAmmo = playerAmmo.getOrDefault(playerUUID, MAG_SIZE);

        if (currentAmmo >= MAG_SIZE) {
            player.displayClientMessage(
                    Component.literal("§cMag already full!"),
                    true
            );
            return;
        }

        VictorRifleItem rifle = new VictorRifleItem(new Item.Properties());
        rifle.startReload(player, playerUUID);
    }

    private Vec3 rotateVector(Vec3 vec, double yaw, double pitch) {
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);

        double x1 = vec.x * cosYaw - vec.z * sinYaw;
        double z1 = vec.x * sinYaw + vec.z * cosYaw;

        double y2 = vec.y * cosPitch - z1 * sinPitch;
        double z2 = vec.y * sinPitch + z1 * cosPitch;

        return new Vec3(x1, y2, z2).normalize();
    }

    private void spawnBulletTrail(ServerLevel world, Vec3 start, Vec3 end) {
        Vec3 direction = end.subtract(start);
        double distance = start.distanceTo(end);
        int particleCount = (int) (distance * 5);

        // Start particles 2 blocks ahead to not obscure vision
        double skipDistance = 2.0;

        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            double actualDistance = distance * progress;

            // Skip particles too close to player
            if (actualDistance < skipDistance) {
                continue;
            }

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

    public static void tick(Level world) {
        if (world.isClientSide) return;

        long currentTime = world.getGameTime();

        playerSpread.entrySet().removeIf(entry -> {
            UUID playerUUID = entry.getKey();
            Float spread = entry.getValue();
            Long lastShot = lastShotTime.get(playerUUID);

            if (lastShot == null) return true;

            if (currentTime - lastShot > RECOVERY_DELAY_TICKS) {
                float newSpread = Math.max(spread - SPREAD_RECOVERY_RATE, BASE_SPREAD);

                if (newSpread <= BASE_SPREAD) {
                    return true;
                } else {
                    entry.setValue(newSpread);
                }
            }

            return false;
        });
    }

    private boolean isHeadshot(LivingEntity target, Vec3 hitPos) {
        // Check if hit position is in the upper portion of the entity (head area)
        double entityHeight = target.getBbHeight();
        double relativeHeight = hitPos.y - target.getY();

        // Consider top 25% of entity as "head"
        return relativeHeight >= entityHeight * 0.75;
    }
}