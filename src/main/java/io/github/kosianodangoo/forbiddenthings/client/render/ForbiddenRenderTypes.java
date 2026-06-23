package io.github.kosianodangoo.forbiddenthings.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;


public class ForbiddenRenderTypes extends RenderType {
    public static ShaderInstance MASK_SHADER;
    public static ShaderInstance GLITCH_SHADER;
    public static ShaderInstance SHARD_SHADER;

    private static final RenderStateShard.ShaderStateShard MASK_SHADER_STATE =
            new RenderStateShard.ShaderStateShard(() -> MASK_SHADER);
    private static final RenderStateShard.ShaderStateShard GLITCH_SHADER_STATE =
            new RenderStateShard.ShaderStateShard(() -> GLITCH_SHADER);
    private static final RenderStateShard.ShaderStateShard SHARD_SHADER_STATE =
            new RenderStateShard.ShaderStateShard(() -> SHARD_SHADER);

    private static final Function<ResourceLocation, RenderType> MASK =
            Util.memoize(sprite -> create("forbidden_mask", sprite, MASK_SHADER_STATE, () -> MASK_SHADER));
    private static final Function<ResourceLocation, RenderType> GLITCH =
            Util.memoize(sprite -> create("forbidden_glitch", sprite, GLITCH_SHADER_STATE, () -> GLITCH_SHADER));

    // テクスチャ非依存の浮遊シャード用 RenderType（テクスチャ無し・頂点色のみ）。
    private static final RenderType SHARD = RenderType.create(
            "forbidden_shard",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(SHARD_SHADER_STATE)
                    .setTextureState(NO_TEXTURE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    private ForbiddenRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                                 boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new IllegalStateException("ForbiddenRenderType must not be instantiated");
    }

    /** スプライト（アトラス内テクスチャ）ごとに専用の NEW_ENTITY マスク RenderType を返す。 */
    public static RenderType mask(ResourceLocation sprite) {
        return MASK.apply(sprite);
    }

    /** マスク＋グリッチノイズ版の RenderType を返す。 */
    public static RenderType glitch(ResourceLocation sprite) {
        return GLITCH.apply(sprite);
    }

    /** アイテム周囲に浮遊するグリッチシャード用の RenderType。 */
    public static RenderType shard() {
        return SHARD;
    }

    private static RenderType create(String name, ResourceLocation sprite,
                                     RenderStateShard.ShaderStateShard shaderState,
                                     Supplier<ShaderInstance> shaderGetter) {
        // Sampler0=ブロックアトラス / Sampler1=MISSING_TEXTURE を束ね、SpriteUV をシェーダーへ渡す。
        RenderStateShard.EmptyTextureStateShard textureState = new RenderStateShard.EmptyTextureStateShard(
                () -> {
                    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                    RenderSystem.setShaderTexture(1, MissingTextureAtlasSprite.getLocation());
                    ShaderInstance shader = shaderGetter.get();
                    if (shader != null) {
                        TextureAtlasSprite s = Minecraft.getInstance().getModelManager()
                                .getAtlas(TextureAtlas.LOCATION_BLOCKS).getSprite(sprite);
                        shader.safeGetUniform("SpriteUV").set(s.getU0(), s.getV0(), s.getU1(), s.getV1());
                    }
                },
                () -> {
                }
        );

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(shaderState)
                .setTextureState(textureState)
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setOverlayState(NO_OVERLAY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false);

        return RenderType.create(
                name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                state
        );
    }
}
