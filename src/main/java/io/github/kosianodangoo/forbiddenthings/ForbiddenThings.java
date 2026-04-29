package io.github.kosianodangoo.forbiddenthings;

import com.mojang.logging.LogUtils;
import io.github.kosianodangoo.forbiddenthings.common.init.ForbiddenItems;
import io.github.kosianodangoo.forbiddenthings.common.network.ForbiddenNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(ForbiddenThings.MODID)
public class ForbiddenThings {
    public static final String MODID = "forbidden_things";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ForbiddenThings() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        LOGGER.debug("Loaded Forbidden Things Mod Class");
        LOGGER.debug("instrumentation: {}", ForbiddenBootstrap.instrumentation);

        ForbiddenNetwork.register();

        new ForbiddenItems().register(modBus);
    }
}
