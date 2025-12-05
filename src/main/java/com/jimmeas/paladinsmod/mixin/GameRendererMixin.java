package com.jimmeas.paladinsmod.mixin;

import com.jimmeas.paladinsmod.ability.impl.IronSightsAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void modifyFov(net.minecraft.client.Camera camera, float partialTicks, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        Player player = minecraft.player;

        if (player != null && IronSightsAbility.isPlayerAiming(player.getUUID())) {
            // Reduce FOV for zoom effect
            // Lower multiplier = more zoom (0.5 = 2x zoom, 0.33 = 3x zoom)
            double currentFov = cir.getReturnValue();
            cir.setReturnValue(currentFov * 0.33); // 3x zoom (stronger zoom)
        }
    }
}