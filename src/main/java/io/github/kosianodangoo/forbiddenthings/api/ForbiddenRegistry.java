package io.github.kosianodangoo.forbiddenthings.api;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;

public interface ForbiddenRegistry<T> {
    static <E> DeferredRegister<E> makeDeferredRegister(IForgeRegistry<E> registry) {
        return DeferredRegister.create(registry, ForbiddenThings.MODID);
    }

    DeferredRegister<T> getDeferredRegister();

    default void register(IEventBus modBus) {
        getDeferredRegister().register(modBus);
    }
}
