package io.github.kosianodangoo.forbiddenthings.common.helper;

import io.github.kosianodangoo.forbiddenthings.common.network.ForbiddenNetwork;
import io.github.kosianodangoo.forbiddenthings.common.network.clientbound.ClientboundInvinciblePacket;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InvincibleHelper {
    private static final Set<UUID> INVINCIBLE = new HashSet<>();
    private static final Set<UUID> REMOVE_BYPASS = new HashSet<>();
    public static final Set<UUID> CLIENT_INVINCIBLE = new HashSet<>();
    public static final Set<UUID> CLIENT_REMOVE_BYPASS = new HashSet<>();

    public static void initServer() {
        INVINCIBLE.clear();
        REMOVE_BYPASS.clear();
    }

    public static void makeInvincible(Entity entity) {
        if (entity.level().isClientSide) {
            CLIENT_INVINCIBLE.add(entity.getUUID());
            return;
        }
        ForbiddenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundInvinciblePacket(entity.getUUID(), false));
        INVINCIBLE.add(entity.getUUID());
    }

    public static boolean isInvincible(Entity entity) {
        if (entity.level().isClientSide) {
            return CLIENT_INVINCIBLE.contains(entity.getUUID());
        }
        return INVINCIBLE.contains(entity.getUUID());
    }

    public static void removeInvincible(Entity entity) {
        if (entity.level().isClientSide) {
            CLIENT_INVINCIBLE.remove(entity.getUUID());
            return;
        }
        ForbiddenNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new ClientboundInvinciblePacket(entity.getUUID(), true));
        INVINCIBLE.remove(entity.getUUID());
    }

    public static void makeRemoveBypass(Entity entity) {
        if (entity.level().isClientSide) {
            CLIENT_REMOVE_BYPASS.add(entity.getUUID());
            return;
        }
        REMOVE_BYPASS.add(entity.getUUID());
    }

    public static boolean isRemoveBypass(Entity entity) {
        if (entity.level().isClientSide) {
            return CLIENT_REMOVE_BYPASS.contains(entity.getUUID());
        }
        return REMOVE_BYPASS.contains(entity.getUUID());
    }

    public static void disableRemoveBypass(Entity entity) {
        if (entity.isRemoved()) {
            return;
        }
        if (entity.level().isClientSide) {
            CLIENT_REMOVE_BYPASS.remove(entity.getUUID());
            return;
        }
        REMOVE_BYPASS.remove(entity.getUUID());
    }
}
