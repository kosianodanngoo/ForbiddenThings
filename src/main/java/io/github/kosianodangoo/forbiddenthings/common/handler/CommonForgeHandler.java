package io.github.kosianodangoo.forbiddenthings.common.handler;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import io.github.kosianodangoo.forbiddenthings.common.helper.EntityHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.EventHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import io.github.kosianodangoo.forbiddenthings.common.network.ForbiddenNetwork;
import io.github.kosianodangoo.forbiddenthings.common.network.clientbound.ClientboundForceKillPacket;
import io.github.kosianodangoo.forbiddenthings.common.network.clientbound.ClientboundInvinciblePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ForbiddenThings.MODID)
public class CommonForgeHandler {
    static {
        new EventHelper();
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.Clone event) {
        ForceKillHelper.removeFromForcekill(event.getEntity());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void deathEnforcer(LivingDeathEvent deathEvent) {
        if (!deathEvent.isCanceled() || EntityHelper.isInvincible(deathEvent.getEntity())) {
            return;
        }
        try {
            if (deathEvent.getSource().is(ForceKillHelper.FORCE_DEATH)) {
                EventHelper.forceSetCanceled(deathEvent, false);
            }
        } catch (Throwable ignored) {
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelDeath(LivingDeathEvent deathEvent) {
        if (EntityHelper.isInvincible(deathEvent.getEntity())) {
            EventHelper.forceSetCanceled(deathEvent, true);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (InvincibleHelper.isInvincible(event.getTarget())) {
            ForbiddenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientboundInvinciblePacket(serverPlayer.getUUID(), true));
        }
    }

    @SubscribeEvent
    public static void onServerWillStart(ServerAboutToStartEvent event) {
        ForceKillHelper.initServer();
        InvincibleHelper.initServer();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (InvincibleHelper.isInvincible(serverPlayer)) {
            ForbiddenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientboundInvinciblePacket(serverPlayer.getUUID(), false));
        }
        if (ForceKillHelper.isForceKilled(serverPlayer)) {
            ForbiddenNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientboundForceKillPacket(serverPlayer.getUUID()));
        }

    }
}
