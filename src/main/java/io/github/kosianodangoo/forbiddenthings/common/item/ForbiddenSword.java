package io.github.kosianodangoo.forbiddenthings.common.item;

import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import io.github.kosianodangoo.forbiddenthings.common.item.tier.ForbiddenTier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class ForbiddenSword extends SwordItem {
    public ForbiddenSword(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        entity.hurt(entity.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        ForceKillHelper.forcekill(entity);
        if (entity instanceof LivingEntity livingEntity) {
            ForceKillHelper.dropAllForce(livingEntity);
        }
        return false;
    }

    public ForbiddenSword() {
        this(ForbiddenTier.INSTANCE, Integer.MAX_VALUE, Float.MAX_VALUE, new Item.Properties().stacksTo(1));
    }

    @Override
    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
