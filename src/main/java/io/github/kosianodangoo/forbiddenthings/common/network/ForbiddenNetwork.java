package io.github.kosianodangoo.forbiddenthings.common.network;

import io.github.kosianodangoo.forbiddenthings.common.helper.ResourceLocationHelper;
import io.github.kosianodangoo.forbiddenthings.common.network.clientbound.ClientboundForceKillPacket;
import io.github.kosianodangoo.forbiddenthings.common.network.clientbound.ClientboundInvinciblePacket;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ForbiddenNetwork {
    public static final String VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.ChannelBuilder
            .named(ResourceLocationHelper.getResourceLocation("main"))
            .clientAcceptedVersions(VERSION::equals)
            .serverAcceptedVersions(VERSION::equals)
            .networkProtocolVersion(() -> VERSION)
            .simpleChannel();

    private static int id = 0;

    public static void register() {
        INSTANCE.messageBuilder(ClientboundForceKillPacket.class, id++)
                .encoder(ClientboundForceKillPacket::encode)
                .decoder(ClientboundForceKillPacket::decode)
                .consumerMainThread(ClientboundForceKillPacket::handle)
                .add();

        INSTANCE.messageBuilder(ClientboundInvinciblePacket.class, id++)
                .encoder(ClientboundInvinciblePacket::encode)
                .decoder(ClientboundInvinciblePacket::decode)
                .consumerMainThread(ClientboundInvinciblePacket::handle)
                .add();
    }
}
