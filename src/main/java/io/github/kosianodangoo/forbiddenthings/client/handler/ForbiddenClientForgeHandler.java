package io.github.kosianodangoo.forbiddenthings.client.handler;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ForbiddenThings.MODID, value = Dist.CLIENT)
public class ForbiddenClientForgeHandler {
    @SubscribeEvent
    public static void onJoin(ClientPlayerNetworkEvent.LoggingIn event) {
        InvincibleHelper.CLIENT_INVINCIBLE.clear();
        InvincibleHelper.CLIENT_REMOVE_BYPASS.clear();
    }
}
