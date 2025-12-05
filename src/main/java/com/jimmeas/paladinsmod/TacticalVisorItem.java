package com.jimmeas.paladinsmod;

import com.jimmeas.paladinsmod.ability.impl.TacticalVisorAbility;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class TacticalVisorItem extends Item {

    public TacticalVisorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Check if ult is charged
            if (!TacticalVisorAbility.isUltReady(player.getUUID())) {
                float chargePercent = TacticalVisorAbility.getUltChargePercent(player.getUUID());
                player.displayClientMessage(
                        Component.literal("§c§lUltimate not ready! " +
                                String.format("%.1f", chargePercent) + "%"),
                        true
                );
                return InteractionResultHolder.fail(itemStack);
            }

            // Activate ultimate
            if (TacticalVisorAbility.activateUltimate(serverPlayer)) {
                // Switch back to slot 1 (gun) immediately
                player.getInventory().selected = 0; // Slot 1 (index 0)

                return InteractionResultHolder.success(itemStack);
            }
        }

        return InteractionResultHolder.pass(itemStack);
    }
}