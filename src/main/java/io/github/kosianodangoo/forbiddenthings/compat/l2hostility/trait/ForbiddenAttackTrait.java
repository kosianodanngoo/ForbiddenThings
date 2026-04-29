package io.github.kosianodangoo.forbiddenthings.compat.l2hostility.trait;

import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2hostility.content.capability.mob.MobTraitCap;
import dev.xkmc.l2hostility.content.logic.TraitEffectCache;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import io.github.kosianodangoo.forbiddenthings.common.event.ForbiddenLivingHurtEvent;
import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import io.github.kosianodangoo.forbiddenthings.compat.l2hostility.ForbiddenTraits;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

public class ForbiddenAttackTrait extends MobTrait {
    public ForbiddenAttackTrait() {
        super(ChatFormatting.BLACK);
    }

    @Override
    public void onHurtTarget(int level, LivingEntity attacker, AttackCache cache, TraitEffectCache traitCache) {
        super.onHurtTarget(level, attacker, cache, traitCache);
        forbiddenAttack(cache.getAttackTarget());
    }

    public static void onForbiddenLivingHurtEvent(ForbiddenLivingHurtEvent event) {
        Entity entity = event.getDamageSource().getEntity();
        if (!(entity instanceof LivingEntity mob) || !MobTraitCap.HOLDER.isProper(mob)) {
            return;
        }
        MobTraitCap cap = MobTraitCap.HOLDER.get(mob);
        if (cap.hasTrait(ForbiddenTraits.FORBIDDEN_ATTACK_TRAIT.get())) {
            forbiddenAttack(event.getEntity());
        }
    }

    public static void forbiddenAttack(LivingEntity target) {
        ForceKillHelper.forcekill(target);
        if (!(target instanceof Player) || !target.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            ForceKillHelper.dropAllForce(target);
        }
    }
}
