package io.github.kosianodangoo.forbiddenthings.common.init;

import io.github.kosianodangoo.forbiddenthings.common.item.ForbiddenInvincible;
import io.github.kosianodangoo.forbiddenthings.common.item.ForbiddenSword;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ForbiddenItems implements ForbiddenRegistry<Item> {
    private static final DeferredRegister<Item> ITEMS = ForbiddenRegistry.makeDeferredRegister(ForgeRegistries.ITEMS);

    public static final RegistryObject<ForbiddenSword> FORBIDDEN_SWORD = register("forbidden_sword", ForbiddenSword::new);
    public static final RegistryObject<ForbiddenInvincible> FORBIDDEN_INVINCIBLE = register("forbidden_invincible", ForbiddenInvincible::new);

    @Override
    public DeferredRegister<Item> getDeferredRegister() {
        return ITEMS;
    }

    public static <V extends Item> RegistryObject<V> register(String id, Supplier<? extends V> supplier) {
        RegistryObject<V> registryObject = ITEMS.register(id, supplier);
        ForbiddenCreativeTabs.ITEMS.add(registryObject);
        return registryObject;
    }
}
