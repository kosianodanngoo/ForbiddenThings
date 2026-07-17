package io.github.kosianodangoo.forbiddenthings.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityInvoker  {
    @Invoker(value = "makeBoundingBox")
    AABB forbidden_things$makeBoundingBox();
}
