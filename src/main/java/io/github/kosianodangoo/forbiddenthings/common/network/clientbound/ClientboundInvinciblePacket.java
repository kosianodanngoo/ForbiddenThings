package io.github.kosianodangoo.forbiddenthings.common.network.clientbound;

import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class ClientboundInvinciblePacket {
    public final UUID uuid;
    public final boolean remove;

    public ClientboundInvinciblePacket(UUID uuid, boolean remove) {
        this.uuid = uuid;
        this.remove = remove;
    }

    public static void encode(ClientboundInvinciblePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.uuid);
        buf.writeBoolean(msg.remove);
    }

    public static ClientboundInvinciblePacket decode(FriendlyByteBuf buf) {
        UUID uuid = buf.readUUID();
        boolean remove = buf.readBoolean();
        return new ClientboundInvinciblePacket(uuid, remove);
    }

    public static void handle(ClientboundInvinciblePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                msg.handle();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    public void handle() {
        if (this.remove) {
            InvincibleHelper.CLIENT_INVINCIBLE.remove(this.uuid);
        } else {
            InvincibleHelper.CLIENT_INVINCIBLE.add(this.uuid);
        }
    }
}
