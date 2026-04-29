package io.github.kosianodangoo.forbiddenthings.common.init;

import io.github.kosianodangoo.forbiddenthings.api.ForbiddenRegistry;
import io.github.kosianodangoo.forbiddenthings.common.item.ForbiddenInvincible;
import io.github.kosianodangoo.forbiddenthings.common.item.ForbiddenSword;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForbiddenItems implements ForbiddenRegistry<Item> {
    private static final DeferredRegister<Item> ITEMS = ForbiddenRegistry.makeDeferredRegister(ForgeRegistries.ITEMS);

    public static final RegistryObject<ForbiddenSword> FORBIDDEN_SWORD = ITEMS.register("forbidden_sword", ForbiddenSword::new);
    public static final RegistryObject<ForbiddenInvincible> FORBIDDEN_INVINCIBLE = ITEMS.register("forbidden_invincible", ForbiddenInvincible::new);

    @Override
    public DeferredRegister<Item> getDeferredRegister() {
        return ITEMS;
    }
}
