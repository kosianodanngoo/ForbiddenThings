package io.github.kosianodangoo.forbiddenthings.common.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

public class ForbiddenLivingHurtEvent extends LivingEvent {
    private final DamageSource damageSource;
    private final float amount;

    public ForbiddenLivingHurtEvent(LivingEntity entity, DamageSource damageSource, float amount) {
        super(entity);
        this.damageSource = damageSource;
        this.amount = amount;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public float getAmount() {
        return amount;
    }
}
