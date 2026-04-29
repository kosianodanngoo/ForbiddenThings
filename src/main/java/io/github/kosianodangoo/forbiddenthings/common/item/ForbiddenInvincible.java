package io.github.kosianodangoo.forbiddenthings.common.item;

import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
            if (InvincibleHelper.isInvincible(player)) {
                InvincibleHelper.removeInvincible(player);
            } else {
                InvincibleHelper.makeInvincible(player);
            }
        }
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }
}
