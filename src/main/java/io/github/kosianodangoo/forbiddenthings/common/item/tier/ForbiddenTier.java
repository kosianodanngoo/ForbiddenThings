package io.github.kosianodangoo.forbiddenthings.common.item.tier;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class ForbiddenTier implements Tier {
    public static final ForbiddenTier INSTANCE = new ForbiddenTier();

    @Override
    public int getUses() {
        return Integer.MAX_VALUE;
    }

    @Override
    public float getSpeed() {
        return Integer.MAX_VALUE;
    }

    @Override
    public float getAttackDamageBonus() {
        return Float.MAX_VALUE;
    }

    @Override
    public int getLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getEnchantmentValue() {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
