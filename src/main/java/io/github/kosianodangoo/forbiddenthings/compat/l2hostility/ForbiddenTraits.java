package io.github.kosianodangoo.forbiddenthings.compat.l2hostility;

import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.xkmc.l2hostility.content.config.TraitConfig;
import dev.xkmc.l2hostility.content.traits.base.MobTrait;
import dev.xkmc.l2hostility.init.L2Hostility;
import dev.xkmc.l2hostility.init.registrate.LHTraits;
import dev.xkmc.l2library.base.L2Registrate;
import io.github.kosianodangoo.forbiddenthings.compat.l2hostility.trait.ForbiddenAttackTrait;
import net.minecraft.resources.ResourceLocation;

public class ForbiddenTraits {
    public static final L2Registrate.RegistryInstance<MobTrait> TRAITS;

    public static final RegistryEntry<ForbiddenAttackTrait> FORBIDDEN_ATTACK_TRAIT;

    static {
        TRAITS = LHTraits.TRAITS;

        FORBIDDEN_ATTACK_TRAIT = L2Hostility.REGISTRATE.regTrait("forbidden_attack", ForbiddenAttackTrait::new, (ResourceLocation id) -> new TraitConfig(
                        id, 50, 40, 1, 10)).desc("")
                .lang("forbidden_attack").register();
    }

    public static void register() {
    }
}
