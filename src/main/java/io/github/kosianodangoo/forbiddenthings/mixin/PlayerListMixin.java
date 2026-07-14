package io.github.kosianodangoo.forbiddenthings.mixin;

import io.github.kosianodangoo.forbiddenthings.common.helper.InvincibleHelper;
import io.github.kosianodangoo.forbiddenthings.common.helper.MixinEntityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(method = "respawn", at= @At("HEAD"), cancellable = true)
    public void respawnMixin(ServerPlayer player, boolean won, CallbackInfoReturnable<ServerPlayer> cir) {
        if (InvincibleHelper.isInvincible(player) && !InvincibleHelper.isRemoveBypass(player) && !MixinEntityHelper.isDeadOrDying(player)) {
            cir.setReturnValue(player);
            cir.cancel();
        }
    }
}
