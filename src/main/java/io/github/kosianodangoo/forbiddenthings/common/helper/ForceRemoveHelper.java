package io.github.kosianodangoo.forbiddenthings.common.helper;


import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.*;

import java.util.List;
import java.util.Objects;

public class ForceRemoveHelper {
    public static void removeFromMemory(Entity victim) {
        Level level = victim.level();
        if (level instanceof ServerLevel serverLevel) {
            PersistentEntitySectionManager<Entity> entityManager = serverLevel.entityManager;


            victim.levelCallback.onRemove(Entity.RemovalReason.KILLED);

            victim.setLevelCallback(EntityInLevelCallback.NULL);

            removeFromPersistentEntityManager(entityManager, victim);

            serverLevel.getChunkSource().removeEntity(victim);
        }
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
