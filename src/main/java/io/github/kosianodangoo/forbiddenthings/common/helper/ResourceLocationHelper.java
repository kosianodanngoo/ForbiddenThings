package io.github.kosianodangoo.forbiddenthings.common.helper;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("removal")
public class ResourceLocationHelper {
    public static ResourceLocation getResourceLocation(String path) {
        return new ResourceLocation(ForbiddenThings.MODID, path);
    }
}
