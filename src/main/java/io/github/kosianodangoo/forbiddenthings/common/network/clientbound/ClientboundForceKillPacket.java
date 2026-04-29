package io.github.kosianodangoo.forbiddenthings.common.network.clientbound;

import io.github.kosianodangoo.forbiddenthings.common.helper.ForceKillHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundForceKillPacket {
    public final UUID uuid;

    public ClientboundForceKillPacket(UUID uuid) {
        this.uuid = uuid;
    }

    public static void encode(ClientboundForceKillPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.uuid);
    }

    public static ClientboundForceKillPacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        return new ClientboundForceKillPacket(uuid);
    }

    public static void handle(ClientboundForceKillPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                msg.handle();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public void handle() {
        ForceKillHelper.CLIENT_FORCE_KILLED.add(this.uuid);
    }
}
