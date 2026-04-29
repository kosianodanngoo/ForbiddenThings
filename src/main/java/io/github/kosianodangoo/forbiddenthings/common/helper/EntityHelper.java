package io.github.kosianodangoo.forbiddenthings.common.helper;


import net.minecraft.world.entity.Entity;

public class EntityHelper {
    public static boolean isInvincible(Entity entity) {
        return InvincibleHelper.isInvincible(entity);
    }

    public static boolean isForceDead(Entity entity) {
        return ForceKillHelper.isForceKilled(entity);
    }

    public static boolean isForceRemove(Entity entity) {
        return false;
    }

    public static boolean shouldRemoveBypassInvincible(Entity entity) {
        return InvincibleHelper.isRemoveBypass(entity);
    }
}
