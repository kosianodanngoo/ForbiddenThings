package io.github.kosianodangoo.forbiddenthings.common.helper;


import io.github.kosianodangoo.forbiddenthings.mixin.EntityInvoker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public class ForceRemoveHelper {
    public static void tpRemove(Entity victim) {
        victim.setPosRaw(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        forceSetPosition(victim, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        if (victim instanceof EntityInvoker entityInvoker) {
            victim.setBoundingBox(entityInvoker.forbidden_things$makeBoundingBox());
        }
    }

    public static void forceSetPosition(Entity entity, double x, double y, double z) {
        entity.position = new Vec3(x, y, z);
        int X = Mth.floor(x);
        int Y = Mth.floor(y);
        int Z = Mth.floor(z);
        if (X != entity.blockPosition.getX() || Y != entity.blockPosition.getY() || Z != entity.blockPosition.getZ()) {
            entity.blockPosition = new BlockPos(X, Y, Z);
            entity.feetBlockState = null;
            if (SectionPos.blockToSectionCoord(X) != entity.chunkPosition.x || SectionPos.blockToSectionCoord(Z) != entity.chunkPosition.z) {
                entity.chunkPosition = new ChunkPos(entity.blockPosition);
            }
        }
    }

    public static void removeFromMemory(Entity victim) {
        Level level = victim.level();
        if (level instanceof ServerLevel serverLevel) {
            PersistentEntitySectionManager<Entity> entityManager = serverLevel.entityManager;


            victim.levelCallback.onRemove(Entity.RemovalReason.KILLED);

            victim.setLevelCallback(EntityInLevelCallback.NULL);

            removeFromPersistentEntityManager(entityManager, victim);

            removeFromEntityTickList(serverLevel.entityTickList, victim);

            serverLevel.getChunkSource().removeEntity(victim);
        }
    }

    public static void removeFromEntityTickList(EntityTickList entityTickList, Entity entity) {
        entityTickList.remove(entity);
        int id = entity.getId();
        entityTickList.active.remove(id);
        var iterated = entityTickList.iterated;
        if (iterated != null) {
            iterated.remove(id);
        }
        entityTickList.passive.remove(id);
    }

    public static void removeFromPersistentEntityManager(PersistentEntitySectionManager persistentEntitySectionManager, Entity victim) {
        EntitySectionStorage<Entity> sectionStorage = persistentEntitySectionManager.sectionStorage;
        if (persistentEntitySectionManager.isLoaded(victim.getUUID())) {
            long index = SectionPos.of(victim.blockPosition()).asLong();
            EntitySection<Entity> tSection = sectionStorage.getSection(index);
            if (Objects.nonNull(tSection)) {
                EntitySection<Entity> newSection = new EntitySection(Entity.class, tSection.getStatus());
                List<Entity> entities = tSection.getEntities()
                        .filter(entity -> victim != entity)
                        .toList();
                for (Entity entity : entities) {
                    newSection.add(entity);
                }
                sectionStorage.sections.replace(index, newSection);
            }
            persistentEntitySectionManager.knownUuids.remove(victim.getUUID());
        }

        EntityLookup<Entity> entityLookup = persistentEntitySectionManager.visibleEntityStorage;
        entityLookup.remove(victim);
        if (entityLookup.getEntity(victim.getId()) != null) {
            EntityLookup<Entity> newEntityLookup = new EntityLookup<Entity>();
            for (Entity entity : entityLookup.getAllEntities()) {
                if (entity != victim) {
                    newEntityLookup.add(entity);
                }
            }
            persistentEntitySectionManager.visibleEntityStorage = newEntityLookup;
            persistentEntitySectionManager.entityGetter = new LevelEntityGetterAdapter<>(newEntityLookup, sectionStorage);
        }
    }
}
