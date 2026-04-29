package io.github.kosianodangoo.forbiddenthings.mixin.client;

import io.github.kosianodangoo.forbiddenthings.common.helper.EntityHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.MixinEntityHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow(aliases = "f_91074_")
    @Nullable
    public LocalPlayer player;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void setScreenMixin(Screen screen, CallbackInfo ci) {
        if (player == null) return;
        if (screen instanceof DeathScreen && EntityHelper.isInvincible(player) && !MixinEntityHelper.isDeadOrDying(player)) {
            ci.cancel();
        }
    }
}
