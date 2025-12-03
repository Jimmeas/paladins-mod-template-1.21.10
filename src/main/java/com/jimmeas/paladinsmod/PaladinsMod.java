package com.jimmeas.paladinsmod;

import com.jimmeas.paladinsmod.entity.GrenadeEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaladinsMod implements ModInitializer {
    public static final String MOD_ID = "paladinsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Packet identifiers
    public static final ResourceLocation ABILITY_USE_PACKET = new ResourceLocation(MOD_ID, "ability_use");
    public static final ResourceLocation ABILITY_COOLDOWN_SYNC = new ResourceLocation(MOD_ID, "ability_cooldown");
    public static final ResourceLocation CHARACTER_SELECT_PACKET = new ResourceLocation(MOD_ID, "character_select");

    // Grenade entity type
    public static EntityType<GrenadeEntity> GRENADE_ENTITY;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Paladins Mod");

        // Register grenade entity
        GRENADE_ENTITY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                new ResourceLocation(MOD_ID, "grenade"),
                FabricEntityTypeBuilder.<GrenadeEntity>create(MobCategory.MISC, GrenadeEntity::new)
                        .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                        .trackRangeBlocks(64)
                        .trackedUpdateRate(10)
                        .build()
        );

        // Register characters
        CharacterRegistry.register();

        // Register server-side packet handlers
        registerServerPacketHandlers();

        LOGGER.info("Paladins Mod initialized successfully!");
    }

    private void registerServerPacketHandlers() {
        // Handle ability usage from client
        ServerPlayNetworking.registerGlobalReceiver(ABILITY_USE_PACKET, (server, player, handler, buf, responseSender) -> {
            int abilitySlot = buf.readInt();

            server.execute(() -> {
                CharacterData data = CharacterData.get(player);
                if (data != null && data.getCharacter() != null) {
                    data.getCharacter().useAbility(player, abilitySlot);
                }
            });
        });

        // Handle character selection
        ServerPlayNetworking.registerGlobalReceiver(CHARACTER_SELECT_PACKET, (server, player, handler, buf, responseSender) -> {
            String characterId = buf.readString();

            server.execute(() -> {
                CharacterData data = CharacterData.get(player);
                com.jimmeas.paladinsmod.character.Character character = CharacterRegistry.getCharacter(characterId);
                if (character != null) {
                    data.setCharacter(character);
                    LOGGER.info("Player {} selected character: {}", player.getName().getString(), characterId);
                }
            });
        });
    }
}