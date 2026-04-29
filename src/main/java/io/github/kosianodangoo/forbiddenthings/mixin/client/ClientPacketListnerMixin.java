package io.github.kosianodangoo.forbiddenthings.mixin.client;

import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListnerMixin implements TickablePacketListener, ClientGamePacketListener {
    @Shadow(aliases = "f_104889_")
    private ClientLevel level;

    @Shadow(aliases = "f_104888_")
    @Final
    private Minecraft minecraft;

    @Inject(method = "handleRemoveEntities", at = @At("HEAD"))
    public void handleRemoveEntitiesBeforeMixin(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        packet.getEntityIds().forEach((id) -> {
            Entity entity = this.level.getEntity(id);
            if (entity == null) {
                return;
            }
            InvincibleHelper.makeRemoveBypass(entity);
        });
    }

    @Inject(method = "handleRemoveEntities", at = @At("RETURN"))
    public void handleRemoveEntitiesAfterMixin(ClientboundRemoveEntitiesPacket packet, CallbackInfo ci) {
        packet.getEntityIds().forEach((id) -> {
            Entity entity = this.level.getEntity(id);
            if (entity == null) {
                return;
            }
            InvincibleHelper.disableRemoveBypass(entity);
        });
    }

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    public void handleRespawnMixin(ClientboundRespawnPacket packet, CallbackInfo ci) {
        if (minecraft.player == null) return;
        ForceKillHelper.CLIENT_FORCE_KILLED.remove(minecraft.player.getUUID());
    }
}
