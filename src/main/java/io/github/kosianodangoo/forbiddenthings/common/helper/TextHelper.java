package io.github.kosianodangoo.forbiddenthings.common.helper;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;


public class TextHelper {
    public static void showOverlayMessage(PacketDistributor.PacketTarget packetTarget, Component message) {
        packetTarget.send(new ClientboundSetActionBarTextPacket(message));
    }
}
