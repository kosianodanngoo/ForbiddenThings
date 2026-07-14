package io.github.kosianodangoo.forbiddenthings.common.init;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ForbiddenCreativeTabs implements ForbiddenRegistry<CreativeModeTab> {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForbiddenThings.MODID);
    public static List<RegistryObject<? extends Item>> ITEMS = new ArrayList<>();

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = TABS.register("forbidden_things", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.tab.forbidden_things"))
            .icon(() -> ForbiddenItems.FORBIDDEN_SWORD.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (var item : ITEMS) {
                    output.accept(item.get());
                }
            })
            .build());


    @Override
    public DeferredRegister<CreativeModeTab> getDeferredRegister() {
        return TABS;
    }
}
