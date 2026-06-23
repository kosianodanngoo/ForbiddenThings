package io.github.kosianodangoo.forbiddenthings.client.handler;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import io.github.kosianodangoo.forbiddenthings.client.model.ForbiddenMaskGeometry;
import io.github.kosianodangoo.forbiddenthings.client.render.ForbiddenRenderTypes;
import io.github.kosianodangoo.forbiddenthings.common.helper.ResourceLocationHelper;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = ForbiddenThings.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ForbiddenClientModBusHandler {
    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocationHelper.getResourceLocation("forbidden_mask"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> ForbiddenRenderTypes.MASK_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocationHelper.getResourceLocation("forbidden_glitch"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> ForbiddenRenderTypes.GLITCH_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocationHelper.getResourceLocation("forbidden_shard"),
                        DefaultVertexFormat.NEW_ENTITY
                ),
                shader -> ForbiddenRenderTypes.SHARD_SHADER = shader
        );
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        // モデル JSON の "loader": "forbidden_things:forbidden_mask" から参照される汎用ローダー。
        event.register("forbidden_mask", ForbiddenMaskGeometry.Loader.INSTANCE);
    }
}
