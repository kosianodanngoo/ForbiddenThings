package io.github.kosianodangoo.forbiddenthings.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import io.github.kosianodangoo.forbiddenthings.client.render.ForbiddenRenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * 汎用ジオメトリローダー。item/generated と同じ板状ジオメトリを生成しつつ、
 * RenderType を独自のマスクシェーダー({@link ForbiddenRenderTypes#mask})に差し替える。
 * "glitch": true のときはアイテム周囲に浮遊するグリッチシャード層も組み込む。
 * シャード配置はモデルJSONの "shards" で設定でき、ドリフトはシェーダー側で時間アニメする。
 */
public class ForbiddenMaskGeometry implements IUnbakedGeometry<ForbiddenMaskGeometry> {
    private final boolean glitch;
    private final ShardConfig shards;

    public ForbiddenMaskGeometry(boolean glitch, ShardConfig shards) {
        this.glitch = glitch;
        this.shards = shards;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext ctx, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState state, ItemOverrides overrides, ResourceLocation modelLocation) {
        Material mat = ctx.getMaterial("layer0");
        TextureAtlasSprite sprite = spriteGetter.apply(mat);
        // block 側は未使用だが BLOCK・chunk 型が必須。entity 側に独自 RenderType を割り当てる。
        RenderType entity = glitch
                ? ForbiddenRenderTypes.glitch(mat.texture())
                : ForbiddenRenderTypes.mask(mat.texture());
        RenderTypeGroup group = new RenderTypeGroup(RenderType.cutout(), entity);

        List<BlockElement> elements = UnbakedGeometryHelper.createUnbakedItemElements(0, sprite.contents());
        List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(elements, m -> sprite, state, modelLocation);

        CompositeModel.Baked.Builder builder =
                CompositeModel.Baked.builder(ctx, sprite, overrides, ctx.getTransforms());
        builder.addQuads(group, quads);

        // glitch 時はアイテム周囲に浮遊するグリッチシャードを別レイヤーとして組み込む
        // （描画ジオメトリなので GUI/手持ち/落下すべてで描画とズレなく整列する）。
        if (glitch) {
            RenderTypeGroup shardGroup = new RenderTypeGroup(RenderType.cutout(), ForbiddenRenderTypes.shard());
            builder.addQuads(shardGroup, buildShards(sprite, shards));
        }
        return builder.build();
    }

    private static final int COLOR_MAGENTA = 0xFFF700F7; // ABGR: a,b,g,r
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_BLACK = 0xFF000000;

    /** モデル空間に散らした平面 quad のグリッチ片群を生成する（配置は JSON 設定・決定論的）。 */
    private static List<BakedQuad> buildShards(TextureAtlasSprite sprite, ShardConfig cfg) {
        List<BakedQuad> shards = new ArrayList<>();
        Random rand = new Random(cfg.seed());
        for (int i = 0; i < cfg.count(); i++) {
            float s = lerp(rand, cfg.sizeMin(), cfg.sizeMax());
            float cx = lerp(rand, cfg.areaMin(), cfg.areaMax());
            float cy = lerp(rand, cfg.areaMin(), cfg.areaMax());
            float cz = lerp(rand, cfg.depthMin(), cfg.depthMax()); // 奥行きを散らして立体感
            int color = switch (rand.nextInt(3)) {
                case 0 -> COLOR_MAGENTA;
                case 1 -> COLOR_WHITE;
                default -> COLOR_BLACK;
            };
            float seed = rand.nextFloat();
            shards.add(shardQuad(cx, cy, cz, s, color, seed, cfg.drift(), sprite));
        }
        return shards;
    }

    private static float lerp(Random rand, float min, float max) {
        return min + rand.nextFloat() * (max - min);
    }

    private static BakedQuad shardQuad(float cx, float cy, float cz, float s, int color, float seed, float drift,
                                       TextureAtlasSprite sprite) {
        int[] v = new int[32];
        putShardVertex(v, 0, cx, cy, cz, color, seed, drift);
        putShardVertex(v, 1, cx + s, cy, cz, color, seed, drift);
        putShardVertex(v, 2, cx + s, cy + s, cz, color, seed, drift);
        putShardVertex(v, 3, cx, cy + s, cz, color, seed, drift);
        return new BakedQuad(v, -1, Direction.SOUTH, sprite, false);
    }

    // BLOCK 頂点フォーマット（8 int/頂点）: pos3, color, uv2, lightmap, normal
    private static void putShardVertex(int[] v, int index, float x, float y, float z, int color, float seed, float drift) {
        int o = index * 8;
        v[o] = Float.floatToRawIntBits(x);
        v[o + 1] = Float.floatToRawIntBits(y);
        v[o + 2] = Float.floatToRawIntBits(z);
        v[o + 3] = color;
        v[o + 4] = Float.floatToRawIntBits(seed);  // UV0.x = シャード固有シード
        v[o + 5] = Float.floatToRawIntBits(drift); // UV0.y = ドリフト量（vsh で使用）
        v[o + 6] = 0x00F000F0;                      // フルブライト lightmap
        v[o + 7] = 0x007F0000;                      // +Z 法線
    }

    /** モデル JSON の "shards" で指定するシャード配置設定。 */
    public record ShardConfig(int count, float sizeMin, float sizeMax, float areaMin, float areaMax,
                              float depthMin, float depthMax, long seed, float drift) {
        public static final ShardConfig DEFAULT =
                new ShardConfig(16, 0.04F, 0.10F, -0.15F, 1.10F, 0.12F, 0.88F, 0xF0B1DDL, 0.05F);

        public static ShardConfig fromJson(JsonObject o) {
            if (o == null) {
                return DEFAULT;
            }
            float[] size = readRange(o, "size", DEFAULT.sizeMin, DEFAULT.sizeMax);
            float[] area = readRange(o, "area", DEFAULT.areaMin, DEFAULT.areaMax);
            float[] depth = readRange(o, "depth", DEFAULT.depthMin, DEFAULT.depthMax);
            int count = GsonHelper.getAsInt(o, "count", DEFAULT.count);
            long seed = GsonHelper.getAsInt(o, "seed", (int) DEFAULT.seed);
            float drift = GsonHelper.getAsFloat(o, "drift", DEFAULT.drift);
            return new ShardConfig(count, size[0], size[1], area[0], area[1], depth[0], depth[1], seed, drift);
        }

        private static float[] readRange(JsonObject o, String key, float defMin, float defMax) {
            if (o.has(key)) {
                JsonArray a = GsonHelper.getAsJsonArray(o, key);
                return new float[]{a.get(0).getAsFloat(), a.get(1).getAsFloat()};
            }
            return new float[]{defMin, defMax};
        }
    }

    public static final class Loader implements IGeometryLoader<ForbiddenMaskGeometry> {
        public static final Loader INSTANCE = new Loader();

        @Override
        public ForbiddenMaskGeometry read(JsonObject json, JsonDeserializationContext ctx) {
            boolean glitch = GsonHelper.getAsBoolean(json, "glitch", false);
            JsonObject shardsObj = json.has("shards") ? GsonHelper.getAsJsonObject(json, "shards") : null;
            return new ForbiddenMaskGeometry(glitch, ShardConfig.fromJson(shardsObj));
        }
    }
}
