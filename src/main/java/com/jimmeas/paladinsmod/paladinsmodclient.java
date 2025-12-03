package com.jimmeas.paladinsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.FriendlyByteBuf;
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

        // Register client tick event for key handling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Check ability keys and send packets to server
            if (ability1Key.consumeClick()) {
                sendAbilityPacket(0);
            }
            if (ability2Key.consumeClick()) {
                sendAbilityPacket(1);
            }
            if (ability3Key.consumeClick()) {
                sendAbilityPacket(2);
            }
            if (ultimateKey.consumeClick()) {
                sendAbilityPacket(3);
            }
        });

        PaladinsMod.LOGGER.info("Paladins Mod Client initialized successfully!");
    }

    private static void sendAbilityPacket(int slot) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slot);
        ClientPlayNetworking.send(PaladinsMod.ABILITY_USE_PACKET, buf);
    }
}