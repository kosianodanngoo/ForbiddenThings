package io.github.kosianodangoo.forbiddenthings.mixin;

import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "saveWithoutId", at = @At("HEAD"))
    public void saveWithoutIdMixin(CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        try {
            Entity entity = (Entity) (Object) this;
            if (InvincibleHelper.isInvincible(entity)) {
                compoundTag.putBoolean("forbidden_things:invincible", true);
            }
            if (ForceKillHelper.isForceKilled(entity)) {
                compoundTag.putBoolean("forbidden_things:force_killed", true);
            }
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    public void loadMixin(CompoundTag compoundTag, CallbackInfo ci) {
        try {
            Entity entity = (Entity) (Object) this;
            if (compoundTag.getBoolean("forbidden_things:invincible")) {
                InvincibleHelper.makeInvincible(entity);
            }
            if (compoundTag.getBoolean("forbidden_things:force_killed")) {
                ForceKillHelper.addForceKilled(entity);
            }
        } catch (Throwable ignored) {
        }
    }
}
