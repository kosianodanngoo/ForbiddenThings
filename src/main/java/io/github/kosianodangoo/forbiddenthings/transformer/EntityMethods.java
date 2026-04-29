package io.github.kosianodangoo.forbiddenthings.transformer;

import com.mojang.logging.LogUtils;
import io.github.kosianodangoo.forbiddenthings.common.helper.EntityHelper;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.server.timings.TimeTracker;

import java.util.function.Consumer;

@SuppressWarnings("unused")
public class EntityMethods {
    public static boolean shouldReplaceHealthMethod(Entity entity) {
        return EntityHelper.isForceDead(entity) || EntityHelper.isInvincible(entity);
    }

    public static float replaceGetHealth(LivingEntity livingEntity) {
        if (EntityHelper.isInvincible(livingEntity)) {
            float maxHealth = livingEntity.getMaxHealth();
            return Float.isNaN(maxHealth) || maxHealth < 20 ? 20 : maxHealth;
        }
        if (EntityHelper.isForceDead(livingEntity)) {
            return -Float.MAX_VALUE;
        }
        return 20;
    }

    public static float getHealth(float health, LivingEntity livingEntity) {
        if (EntityHelper.isInvincible(livingEntity)) {
            float maxHealth = livingEntity.getMaxHealth();
            return Float.isNaN(maxHealth) || maxHealth < 20 ? 20 : maxHealth;
        }
        if (EntityHelper.isForceDead(livingEntity)) {
            return -Float.MAX_VALUE;
        }
        return health;
    }

    public static boolean replaceIsDeadOrDying(Entity entity) {
        return !EntityHelper.isInvincible(entity) && EntityHelper.isForceDead(entity);
    }

    public static boolean isDeadOrDying(boolean deadOrDying, LivingEntity livingEntity) {
        return !EntityHelper.isInvincible(livingEntity) && (deadOrDying || EntityHelper.isForceDead(livingEntity));
    }

    public static boolean replaceIsAlive(Entity entity) {
        return !replaceIsDeadOrDying(entity);
    }

    public static boolean isAlive(boolean alive, Entity entity) {
        return EntityHelper.isInvincible(entity) || (alive && !EntityHelper.isForceDead(entity));
    }

    public static Entity.RemovalReason getRemovalReason(Entity.RemovalReason removalReason, Entity entity) {
        if (EntityHelper.isInvincible(entity) && !EntityHelper.shouldRemoveBypassInvincible(entity)) {
            return null;
        }
        if (EntityHelper.isForceRemove(entity)) {
            return Entity.RemovalReason.KILLED;
        }
        return removalReason;
    }

    public static boolean isRemoved(boolean removed, Entity entity) {
        if (EntityHelper.isInvincible(entity) && !EntityHelper.shouldRemoveBypassInvincible(entity)) {
            return false;
        }
        if (EntityHelper.isForceRemove(entity)) {
            return true;
        }
        return removed;
    }

    public static boolean shouldReplaceIsPickable(Entity entity) {
        return EntityHelper.isInvincible(entity);
    }

    public static boolean replaceIsPickable(Entity entity) {
        return false;
    }

    public static boolean shouldOverrideTick(Entity entity) {
        return EntityHelper.isInvincible(entity);
    }

    public static void updateLastTicks(ServerLevel serverLevel) {
        /*
        serverLevel.entityTickList.forEach(entity -> {
            if (entity instanceof ITickTracker tickTracker) {
                if (!entity.isPassenger() && shouldOverrideTick(entity) && shouldForceTick(entity)) {
                    newGuardEntityTick(serverLevel::tickNonPassenger, entity);
                }
                tickTracker.the_trial_monolith$markUpdating(true);
                tickTracker.the_trial_monolith$updateLastTickCount();
            }
        });*/
    }

    public static boolean shouldForceTick(Entity entity) {
        return false; //entity instanceof ITickTracker tickTracker && tickTracker.the_trial_monolith$getLastTickCount() == entity.tickCount && !tickTracker.the_trial_monolith$isUpdating();
    }

    public static void tickOverride(Consumer<Entity> consumer, Entity entity) {
        consumer.accept(entity);
        /*if (!entity.isPassenger() && shouldForceTick(entity)) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                newGuardEntityTick(serverLevel::tickNonPassenger, entity);
            } else if (entity.level() instanceof ClientLevel clientLevel) {
                newGuardEntityTick(clientLevel::tickNonPassenger, entity);
            }
        }*/
        //if (entity instanceof ITickTracker tickTracker) {
        //tickTracker.the_trial_monolith$markUpdating(false);
        //}
    }

    public static void newGuardEntityTick(Consumer<Entity> consumer, Entity entity) {
        try {
            TimeTracker.ENTITY_UPDATE.trackStart(entity);
            consumer.accept(entity);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
            entity.fillCrashReportCategory(crashreportcategory);
            if (!(Boolean) ForgeConfig.SERVER.removeErroringEntities.get()) {
                throw new ReportedException(crashreport);
            }

            LogUtils.getLogger().error("{}", crashreport.getFriendlyReport());
            //EntityHelper.setBypassProtection(entity, true);
            entity.discard();
        } finally {
            TimeTracker.ENTITY_UPDATE.trackEnd(entity);
        }
    }
}
