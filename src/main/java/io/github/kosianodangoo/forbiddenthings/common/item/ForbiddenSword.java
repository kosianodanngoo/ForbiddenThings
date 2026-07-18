package io.github.kosianodangoo.forbiddenthings.common.item;

import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.ForceRemoveHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.TextHelper;
import io.github.kosianodangoo.forbiddenthings.common.item.tier.ForbiddenTier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class ForbiddenSword extends SwordItem {
    public static final String REMOVE_TAG = "remove";

    public ForbiddenSword(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!entity.level().isClientSide()) {
            executeAttack(stack, entity, player);
        }
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level();
        if (!level.isClientSide() && entity.isShiftKeyDown()) {
            Vec3 offset = entity.getViewVector(1F).scale(2);
            AABB range = entity.getBoundingBox().move(offset).inflate(1,0.25,1);
            List<Entity> targets = level.getEntities(entity, range);
            for (Entity target : targets) {
                executeAttack(stack, target, entity);
            }
            if (entity instanceof Player player && level instanceof ServerLevel serverLevel) {
                Vec3 pos = entity.getEyePosition(1F).add(offset);
                level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y, pos.z, 0, offset.x, offset.y, offset.z, 0.0D);
            }
        }
        return super.onEntitySwing(stack, entity);
    }

    public ForbiddenSword() {
        this(ForbiddenTier.INSTANCE, Integer.MAX_VALUE, Float.MAX_VALUE, new Item.Properties().stacksTo(1));
    }

    public void executeAttack(ItemStack stack, Entity target, @Nullable LivingEntity source) {
        if (target instanceof LivingEntity livingEntity) {
            ForceKillHelper.breakBrain(livingEntity);
            if (target.isAlive() && (!(target instanceof Player player) || target.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY))) {
                ForceKillHelper.dropAllForce(livingEntity);
            }
        }
        target.hurt(target.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        ForceKillHelper.forcekill(target);
        if (shouldRemove(stack)) {
            //ForceRemoveHelper.tpRemove(target);
            ForceRemoveHelper.removeFromMemory(target);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            boolean shouldRemove = !shouldRemove(stack);
            setRemove(stack, shouldRemove);
            if(player instanceof ServerPlayer serverPlayer) {
                TextHelper.showOverlayMessage(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        Component.translatable("item.forbidden_things.forbidden_sword.remove.".concat(shouldRemove ? "enabled" : "disabled"))
                );
            }
        }
        return super.use(level, player, hand);
    }

    public boolean shouldRemove(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        return tag.getBoolean(REMOVE_TAG);
    }

    public void setRemove(ItemStack stack, boolean remove) {
        stack.getOrCreateTag().putBoolean(REMOVE_TAG, remove);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.translatable("item.forbidden_things.forbidden_sword.remove.".concat(shouldRemove(stack) ? "enabled" : "disabled")));
        super.appendHoverText(stack, level, tooltips, flag);
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
