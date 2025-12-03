package com.jimmeas.paladinsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class paladinsmodclient implements ClientModInitializer {

    public static KeyMapping ability1Key;
    public static KeyMapping ability2Key;
    public static KeyMapping ability3Key;
    public static KeyMapping ultimateKey;
    public static KeyMapping characterMenuKey;

    @Override
    public void onInitializeClient() {
        PaladinsMod.LOGGER.info("Initializing Paladins Mod Client");

        // Register entity renderer
        EntityRendererRegistry.register(
                PaladinsMod.GRENADE_ENTITY,
                (context) -> new ThrownItemRenderer<>(context)
        );

        // Register key bindings
        ability1Key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.paladinsmod.ability1",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_Q,
                "category.paladinsmod.abilities"
        ));

        ability2Key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.paladinsmod.ability2",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_E,
                "category.paladinsmod.abilities"
        ));

        ability3Key = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.paladinsmod.ability3",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_SHIFT,
                "category.paladinsmod.abilities"
        ));

        ultimateKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.paladinsmod.ultimate",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.paladinsmod.abilities"
        ));

        characterMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.paladinsmod.menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.paladinsmod.abilities"
        ));

        // Note: Key handling removed for now - packet system needs to be reimplemented
        // for 1.21.1's new CustomPacketPayload system

        PaladinsMod.LOGGER.info("Paladins Mod Client initialized successfully!");
        PaladinsMod.LOGGER.info("Note: Ability keybinds registered but not functional yet - packet system needs update");
    }
}