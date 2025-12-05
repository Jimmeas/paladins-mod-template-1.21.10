package com.jimmeas.paladinsmod;

import com.jimmeas.paladinsmod.entity.GrenadeEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaladinsMod implements ModInitializer {
    public static final String MOD_ID = "paladinsmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Grenade entity type
    public static EntityType<GrenadeEntity> GRENADE_ENTITY;

    // Items
    public static Item VICTOR_RIFLE;
    public static Item FRAG_GRENADE;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Paladins Mod");

        // Register grenade entity
        GRENADE_ENTITY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "grenade"),
                FabricEntityTypeBuilder.<GrenadeEntity>create(MobCategory.MISC, GrenadeEntity::new)
                        .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                        .trackRangeBlocks(64)
                        .trackedUpdateRate(10)
                        .build()
        );

        // Register items
        VICTOR_RIFLE = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "victor_rifle"),
                new VictorRifleItem(new Item.Properties().stacksTo(1))
        );

        FRAG_GRENADE = Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "frag_grenade"),
                new FragGrenadeItem(new Item.Properties().stacksTo(1))
        );




        // Register characters
        CharacterRegistry.register();

        LOGGER.info("Paladins Mod initialized successfully!");
    }
}