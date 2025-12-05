package com.jimmeas.paladinsmod.mixin;

import com.jimmeas.paladinsmod.PaladinsMod;
import com.jimmeas.paladinsmod.VictorRifleItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttackMixin {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(net.minecraft.world.entity.Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        // If holding Victor's rifle, shoot instead of melee
        if (heldItem.getItem() == PaladinsMod.VICTOR_RIFLE) {
            if (heldItem.getItem() instanceof VictorRifleItem rifle) {
                rifle.use(player.level(), player, InteractionHand.MAIN_HAND);
            }
            ci.cancel(); // Cancel normal attack
        }
    }
}