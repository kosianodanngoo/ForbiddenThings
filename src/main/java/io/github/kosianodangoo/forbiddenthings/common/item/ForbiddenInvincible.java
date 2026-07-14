package io.github.kosianodangoo.forbiddenthings.common.item;

import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.TextHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class ForbiddenInvincible extends Item {
    public ForbiddenInvincible() {
        this(new Item.Properties().stacksTo(1));
    }

    public ForbiddenInvincible(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            boolean isInvincible = InvincibleHelper.isInvincible(player);
            if (isInvincible) {
                InvincibleHelper.removeInvincible(player);
            } else {
                InvincibleHelper.makeInvincible(player);
            }
            if(player instanceof ServerPlayer serverPlayer) {
                TextHelper.showOverlayMessage(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        Component.translatable("item.forbidden_things.forbidden_invincible.".concat(isInvincible ? "disabled" : "enabled"))
                );
            }
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
