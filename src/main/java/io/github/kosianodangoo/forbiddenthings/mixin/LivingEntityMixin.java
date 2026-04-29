package io.github.kosianodangoo.forbiddenthings.mixin;

import io.github.kosianodangoo.forbiddenthings.common.event.ForbiddenLivingHurtEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 0)
public class LivingEntityMixin {
    @Inject(method = "hurt", at = @At("HEAD"))
    public void onHurt(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        MinecraftForge.EVENT_BUS.post(new ForbiddenLivingHurtEvent((LivingEntity) (Object) this, damageSource, amount));
    }
}
