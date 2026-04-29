package io.github.kosianodangoo.forbiddenthings.compat.l2hostility;

import io.github.kosianodangoo.forbiddenthings.compat.l2hostility.trait.ForbiddenAttackTrait;
import net.minecraftforge.fml.Bindings;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForbiddenL2Hostility {
    public static void register(FMLJavaModLoadingContext context) {
        ForbiddenTraits.register();
        Bindings.getForgeBus().get().addListener(ForbiddenAttackTrait::onForbiddenLivingHurtEvent);
    }
}
