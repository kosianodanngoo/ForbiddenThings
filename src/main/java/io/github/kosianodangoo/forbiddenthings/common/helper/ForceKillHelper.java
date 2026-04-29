package io.github.kosianodangoo.forbiddenthings.common.helper;

import io.github.kosianodangoo.forbiddenthings.common.network.ForbiddenNetwork;
import io.github.kosianodangoo.forbiddenthings.common.network.clientbound.ClientboundForceKillPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ForceKillHelper {
    public static final ResourceKey<DamageType> FORCE_DEATH = ResourceKey.create
            (Registries.DAMAGE_TYPE, ResourceLocationHelper.getResourceLocation("force_death"));
    private static final Set<UUID> FORCE_KILLED = new HashSet<>();
    public static final Set<UUID> CLIENT_FORCE_KILLED = new HashSet<>();

    public static void initServer() {
        FORCE_KILLED.clear();
    }

    public static void forcekill(Entity entity) {
        if (entity.level().isClientSide) {
            return;
        }
        addForceKilled(entity);
        if (entity instanceof LivingEntity livingEntity) {
            forceDie(livingEntity);
            entity.gameEvent(GameEvent.ENTITY_DAMAGE);
        }
        ForbiddenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundForceKillPacket(entity.getUUID()));
    }

    public static void breakBrain(LivingEntity entity) {
        entity.getBrain().clearMemories();
        if (entity instanceof Mob mob) {
            breakGoalSelector(mob.goalSelector);
            breakGoalSelector(mob.targetSelector);
            mob.setTarget(entity);
            mob.setTarget(null);
        }
    }

    public static void breakGoalSelector(GoalSelector goalSelector) {
        try {
            goalSelector.removeAllGoals(goal -> true);
            goalSelector.addGoal(0, new Goal() {
                @Override
                public boolean canUse() {
                    return false;
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void forceDie(LivingEntity entity) {
        DamageSource damageSource = getForceDeathDamage(entity.level());
        entity.die(damageSource);
        if (!entity.dead) {
            LivingEntity livingentity = entity.getKillCredit();
            if (entity.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(entity, entity.deathScore, damageSource);
            }
            entity.gameEvent(GameEvent.ENTITY_DIE);
            entity.level().broadcastEntityEvent(entity, (byte) 3);
            entity.setPose(Pose.DYING);
        }
    }

    public static void addForceKilled(Entity entity) {
        if (entity.level().isClientSide) {
            CLIENT_FORCE_KILLED.add(entity.getUUID());
            return;
        }
        FORCE_KILLED.add(entity.getUUID());
    }

    public static boolean isForceKilled(Entity entity) {
        if (entity.level().isClientSide) {
            return CLIENT_FORCE_KILLED.contains(entity.getUUID());
        }
        return FORCE_KILLED.contains(entity.getUUID());
    }

    public static void removeFromForcekill(Entity entity) {
        if (entity.level().isClientSide) {
            CLIENT_FORCE_KILLED.remove(entity.getUUID());
        }
        FORCE_KILLED.remove(entity.getUUID());
    }

    public static DamageSource getForceDeathDamage(Level level) {
        try {
            return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(FORCE_DEATH));
        } catch (Exception e) {
            return level.damageSources().genericKill();
        }
    }

    public static void dropAllForce(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            player.getInventory().compartments.forEach(itemStacks -> itemStacks.forEach(stack -> clearStackAndDrop(player, stack)));
        }
        EquipmentSlot[] equipmentSlots = EquipmentSlot.values();
        int length = equipmentSlots.length;
        for (int i = 0; i < length; i++) {
            clearStackAndDrop(livingEntity, livingEntity.getItemBySlot(equipmentSlots[i]));
        }
        dropFromLootTable(livingEntity, getForceDeathDamage(livingEntity.level()));
    }

    public static void dropFromLootTable(LivingEntity livingEntity, DamageSource damageSource) {
        ResourceLocation resourcelocation = livingEntity.getLootTable();

        Level level = livingEntity.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LootTable loottable = serverLevel.getServer().getLootData().getLootTable(resourcelocation);
        LootParams.Builder lootparams$builder = (new LootParams.Builder(serverLevel))
                .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                .withParameter(LootContextParams.ORIGIN, livingEntity.position()).withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity());
        if (livingEntity.lastHurtByPlayer != null) {
            lootparams$builder = lootparams$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, livingEntity.lastHurtByPlayer).withLuck(livingEntity.lastHurtByPlayer.getLuck());
        }

        LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
        loottable.getRandomItems(lootparams, livingEntity.getLootTableSeed(), livingEntity::spawnAtLocation);
    }

    public static void clearStackAndDrop(Entity entity, ItemStack itemStack) {
        ItemStack stack = itemStack.copyAndClear();
        entity.spawnAtLocation(stack);
    }
}
