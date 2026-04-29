package io.github.kosianodangoo.forbiddenthings.common.helper;

import net.minecraft.world.entity.LivingEntity;

public class MixinEntityHelper {
    public static boolean isDeadOrDying(LivingEntity livingEntity) {
        return livingEntity.isDeadOrDying();
    }
}
