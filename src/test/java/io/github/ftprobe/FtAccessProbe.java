package io.github.ftprobe;

import com.mojang.logging.LogUtils;
import io.github.kosianodangoo.forbiddenthings.common.helper.UnsafeHelper;
import io.github.kosianodangoo.forbiddenthings.transformer.GenericTransformer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 外部Modを模した検証用プローブ（別JPMSモジュール ft_probe として dev 実行に載る）。
 *
 * <p>本体 Forbidden Things は {@code ForbiddenBootstrap.applyModuleSetting()} で自モジュールを
 * {@code automatic=false} 化し、各パッケージを信頼モジュールのホワイトリストにのみ {@code addExports}
 * する「外部モジュールアクセス拒否」を行う。このプローブは、非信頼モジュールから本体内部クラスへ
 * 各種手段でアクセスを試み、拒否されること（DENIED＝期待通り）を検証する。アクセスに成功した場合は
 * 隔離が漏れている（LEAKED）としてログに大きく表示する。起動自体は止めない。</p>
 */
@SuppressWarnings("removal")
@Mod(FtAccessProbe.MODID)
public class FtAccessProbe {
    public static final String MODID = "ft_probe";
    private static final Logger LOGGER = LogUtils.getLogger();

    /** gradle の runAccessTest から渡されると、プローブ完了後に合否を終了コードで返す（System.exit）。 */
    private static final boolean SELF_TEST = Boolean.getBoolean("ft.probe.selftest");

    private static final String TARGET_MODULE_NAME = "forbidden_things";
    private static final String HELPER_CLASS = "io.github.kosianodangoo.forbiddenthings.common.helper.UnsafeHelper";
    private static final String TRANSFORMER_CLASS = "io.github.kosianodangoo.forbiddenthings.transformer.GenericTransformer";

    /** applyModuleSetting() が export 対象から外している内部パッケージ群（拒否されるべき）。 */
    private static final String[] INTERNAL_PACKAGES = {
            "io.github.kosianodangoo.forbiddenthings.common.helper",
            "io.github.kosianodangoo.forbiddenthings.transformer",
            "io.github.kosianodangoo.forbiddenthings.agent",
    };

    public FtAccessProbe() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        try {
            runProbes();
        } catch (Throwable t) {
            LOGGER.error("[FT-PROBE] probe harness crashed", t);
        }
    }

    // ---- 結果集計 -------------------------------------------------------------

    private enum Result {
        DENIED,   // アクセス拒否＝期待通り（pass）
        LEAKED,   // アクセス成功＝隔離失敗（fail）
        UNEXPECTED // 想定外の結果（fail、環境問題の可能性）
    }

    private record ProbeResult(String name, Result result, String detail) {}

    private final List<ProbeResult> results = new ArrayList<>();

    private void pass(String name, String detail) {
        results.add(new ProbeResult(name, Result.DENIED, detail));
        LOGGER.warn("[FT-PROBE] {}: DENIED (expected) -- {}", name, detail);
    }

    private void leak(String name, String detail) {
        results.add(new ProbeResult(name, Result.LEAKED, detail));
        LOGGER.error("[FT-PROBE] {}: LEAKED (DENIAL FAILED) -- {}", name, detail);
    }

    private void unexpected(String name, String detail) {
        results.add(new ProbeResult(name, Result.UNEXPECTED, detail));
        LOGGER.error("[FT-PROBE] {}: UNEXPECTED -- {}", name, detail);
    }

    /** 正のコントロール等、成功が期待されるチェック用。 */
    private void controlOk(String name, String detail) {
        results.add(new ProbeResult(name, Result.DENIED, detail)); // pass 扱い
        LOGGER.warn("[FT-PROBE] {}: OK (expected) -- {}", name, detail);
    }

    private void controlFail(String name, String detail) {
        results.add(new ProbeResult(name, Result.UNEXPECTED, detail));
        LOGGER.error("[FT-PROBE] {}: CONTROL FAILED -- {}", name, detail);
    }

    // ---- プローブ本体 ---------------------------------------------------------

    private void runProbes() {
        Module probeModule = FtAccessProbe.class.getModule();
        ModuleLayer layer = probeModule.getLayer();
        LOGGER.warn("[FT-PROBE] ===== Forbidden Things access-denial probe start =====");
        LOGGER.warn("[FT-PROBE] probe module = {} (named={}, layer={})",
                probeModule.getName(), probeModule.isNamed(), layer);

        Module target = resolveTargetModule(probeModule, layer);
        if (target == null) {
            controlFail("resolve-target-module",
                    "本体モジュール '" + TARGET_MODULE_NAME + "' を解決できず。以降の一部プローブをスキップ");
        } else {
            LOGGER.warn("[FT-PROBE] target module = {} (named={})", target.getName(), target.isNamed());
        }

        probeControl();
        if (target != null) {
            probeModuleIntrospection(target, probeModule, layer);
        }
        probeReflectiveFieldGet();
        probeDeepReflection();
        probeDirectLink();
        probeReflectiveStaticMethodInvoke();
        probeDirectLinkStaticMethod();
        probeReflectiveStaticVarGet();
        probeDirectLinkStaticVar();
        probeUnsafeOffsetRead();
        probeImplLookupAccess();

        int failures = printSummary();

        if (SELF_TEST) {
            // 結果ファイルを書く（存在＝プローブが実際に走った証拠。gradle 側はこれで合否判定し、
            // 起動時クラッシュ＝ファイル無し と、LEAK 検出＝FAIL を区別できる）。
            writeResultFile(failures);
            int code = failures == 0 ? 0 : 1;
            LOGGER.warn("[FT-PROBE] self-test mode: exiting with code {} ({} failure(s))", code, failures);
            System.exit(code);
        }
    }

    /** self-test 時、gradle が判定に使う結果ファイルを書き出す。パスは -Dft.probe.resultFile で受け取る。 */
    private void writeResultFile(int failures) {
        String path = System.getProperty("ft.probe.resultFile");
        if (path == null || path.isBlank()) {
            LOGGER.warn("[FT-PROBE] ft.probe.resultFile 未指定のため結果ファイルを書きません");
            return;
        }
        String content = (failures == 0 ? "PASS" : "FAIL") + " failures=" + failures
                + " total=" + results.size() + System.lineSeparator();
        try {
            java.nio.file.Path p = java.nio.file.Path.of(path);
            if (p.getParent() != null) {
                java.nio.file.Files.createDirectories(p.getParent());
            }
            java.nio.file.Files.writeString(p, content);
            LOGGER.warn("[FT-PROBE] wrote result file: {} ({})", path, content.trim());
        } catch (Throwable t) {
            LOGGER.error("[FT-PROBE] 結果ファイル書き込み失敗: {}", path, t);
        }
    }

    private Module resolveTargetModule(Module probeModule, ModuleLayer layer) {
        if (layer != null) {
            Optional<Module> byName = layer.findModule(TARGET_MODULE_NAME);
            if (byName.isPresent()) return byName.get();
        }
        // フォールバック: 内部クラスをロードしてそのモジュールを得る（ロード自体は拒否対象外）。
        try {
            Class<?> c = Class.forName(HELPER_CLASS, false, probeModule.getClassLoader());
            return c.getModule();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** P0: リフレクション機構そのものが動くことの確認（アクセス可能な public フィールド読み取り）。 */
    private void probeControl() {
        try {
            Object v = Integer.class.getField("MAX_VALUE").get(null);
            if (Integer.valueOf(Integer.MAX_VALUE).equals(v)) {
                controlOk("P0-control-reflection", "Integer.MAX_VALUE をリフレクション取得できた（機構は正常）");
            } else {
                controlFail("P0-control-reflection", "予期しない値: " + v);
            }
        } catch (Throwable t) {
            controlFail("P0-control-reflection", describe(t));
        }
    }

    /**
     * P1/P5: モジュール内省による決定的検証（例外に頼らない）。
     * 内部パッケージは probe に export されていない（false）べき、
     * 信頼モジュール(forge)には export されている（true）べき、descriptor.automatic は false であるべき。
     */
    private void probeModuleIntrospection(Module target, Module probeModule, ModuleLayer layer) {
        for (String pkg : INTERNAL_PACKAGES) {
            boolean exportedToProbe = target.isExported(pkg, probeModule);
            if (exportedToProbe) {
                leak("P1-isExported[" + shortPkg(pkg) + "]->probe",
                        "内部パッケージが ft_probe に export されている");
            } else {
                pass("P1-isExported[" + shortPkg(pkg) + "]->probe",
                        "ft_probe には export されていない");
            }
        }

        ModuleDescriptor desc = target.getDescriptor();
        if (desc == null) {
            unexpected("P1-descriptor", "target.getDescriptor() が null");
        } else if (desc.isAutomatic()) {
            leak("P1-automatic==false", "descriptor.automatic が true のまま（automatic 化解除に失敗）");
        } else {
            pass("P1-automatic==false", "descriptor.automatic=false（自動全 export は解除済み）");
        }

        // P5: 信頼モジュールへの export が維持されている＝全拒否ではなく選択的拒否であることの裏取り。
        Optional<Module> forge = layer == null ? Optional.empty() : layer.findModule("forge");
        if (forge.isEmpty()) {
            controlFail("P5-forge-exported", "forge モジュールが見つからず検証不能");
        } else {
            String pkg = INTERNAL_PACKAGES[0];
            boolean exportedToForge = target.isExported(pkg, forge.get());
            if (exportedToForge) {
                controlOk("P5-forge-exported", shortPkg(pkg) + " は forge に export されている（選択的拒否）");
            } else {
                controlFail("P5-forge-exported",
                        shortPkg(pkg) + " が forge にも export されていない（信頼側アクセスまで壊れている疑い）");
            }
        }
    }

    /** P2: 非 export パッケージの public static フィールドをリフレクション取得 → IllegalAccessException 期待。 */
    private void probeReflectiveFieldGet() {
        String name = "P2-reflect-get[UnsafeHelper.UNSAFE]";
        try {
            Class<?> c = Class.forName(HELPER_CLASS);
            Field f = c.getField("UNSAFE");
            Object v = f.get(null); // ここで拒否されるべき
            leak(name, "public フィールドをリフレクション取得できた: value=" + v);
        } catch (IllegalAccessException e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** P3: private メンバへの setAccessible → InaccessibleObjectException 期待（opens していないため）。 */
    private void probeDeepReflection() {
        String name = "P3-setAccessible[UnsafeHelper.GET_DECLARED_FIELDS0]";
        try {
            Class<?> c = Class.forName(HELPER_CLASS);
            Field f = c.getDeclaredField("GET_DECLARED_FIELDS0");
            f.setAccessible(true); // ここで拒否されるべき
            leak(name, "private フィールドに setAccessible(true) が通った");
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** P4: 内部クラスへの直接リンク参照 → 実行時 IllegalAccessError 期待（最も忠実な外部 Mod 挙動）。 */
    private void probeDirectLink() {
        String name = "P4-direct-link[UnsafeHelper.UNSAFE]";
        try {
            Object v = directLinkTouch(); // getstatic 解決時に拒否されるべき
            leak(name, "直接リンク参照が成功した: value=" + v);
        } catch (IllegalAccessError e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** 直接リンク参照。実行時に getstatic UnsafeHelper.UNSAFE を解決する。 */
    private static Object directLinkTouch() {
        return UnsafeHelper.UNSAFE;
    }

    // ---- public static 関数 / 変数 に対する検証 --------------------------------

    /** P8: 内部クラスの public static メソッドをリフレクション呼び出し → IllegalAccessException 期待。 */
    private void probeReflectiveStaticMethodInvoke() {
        String name = "P8-reflect-invoke[GenericTransformer.isSubclass]";
        try {
            Class<?> c = Class.forName(TRANSFORMER_CLASS);
            Method m = c.getMethod("isSubclass", String.class, String.class, boolean.class);
            Object r = m.invoke(null, "java/lang/Object", "java/lang/Object", false); // ここで拒否されるべき
            leak(name, "public static メソッドをリフレクション呼び出しできた: return=" + r);
        } catch (IllegalAccessException e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** P9: 内部クラスの public static メソッドへの直接リンク呼び出し(invokestatic) → IllegalAccessError 期待。 */
    private void probeDirectLinkStaticMethod() {
        String name = "P9-direct-link-invoke[GenericTransformer.isSubclass]";
        try {
            boolean r = directLinkStaticMethod(); // invokestatic 解決時に拒否されるべき
            leak(name, "public static メソッドへの直接呼び出しが成功: return=" + r);
        } catch (IllegalAccessError e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** P10: 内部クラスの public static 変数をリフレクション取得 → IllegalAccessException 期待。 */
    private void probeReflectiveStaticVarGet() {
        String name = "P10-reflect-get[GenericTransformer.breakMyReference]";
        try {
            Class<?> c = Class.forName(TRANSFORMER_CLASS);
            Object v = c.getField("breakMyReference").get(null); // ここで拒否されるべき
            leak(name, "public static 変数をリフレクション取得できた: value=" + v);
        } catch (IllegalAccessException e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** P11: 内部クラスの public static 変数への直接リンク参照(getstatic) → IllegalAccessError 期待。 */
    private void probeDirectLinkStaticVar() {
        String name = "P11-direct-link-get[GenericTransformer.breakMyReference]";
        try {
            boolean v = directLinkStaticVar(); // getstatic 解決時に拒否されるべき
            leak(name, "public static 変数への直接参照が成功: value=" + v);
        } catch (IllegalAccessError e) {
            pass(name, describe(e));
        } catch (Throwable t) {
            unexpected(name, describe(t));
        }
    }

    /** 直接リンク呼び出し。実行時に invokestatic GenericTransformer.isSubclass を解決する（副作用なし）。 */
    private static boolean directLinkStaticMethod() {
        return GenericTransformer.isSubclass("java/lang/Object", "java/lang/Object", false);
    }

    /** 直接リンク参照。実行時に getstatic GenericTransformer.breakMyReference を解決する。 */
    private static boolean directLinkStaticVar() {
        return GenericTransformer.breakMyReference;
    }

    /**
     * P6: sun.misc.Unsafe を外部モジュールが独自取得し、staticFieldBase/Offset/getObject で
     * モジュールアクセスチェックを完全にバイパスして本体内部フィールドを読めるか。
     * これは本体 UnsafeHelper 自身が使う「マスターキー」。外部でも通れば隔離は無効化される。
     */
    private void probeUnsafeOffsetRead() {
        String name = "P6-unsafe-offset-read[UnsafeHelper.UNSAFE]";
        Unsafe u;
        try {
            u = acquireUnsafe();
            LOGGER.warn("[FT-PROBE] P6: sun.misc.Unsafe を独自取得できた（マスターキー入手）: {}", u);
        } catch (Throwable t) {
            // Unsafe を取得できない＝この経路は塞がれている
            pass(name, "sun.misc.Unsafe 取得が拒否された: " + describe(t));
            return;
        }
        try {
            Class<?> c = Class.forName(HELPER_CLASS);
            Field target = c.getDeclaredField("UNSAFE");
            Object base = u.staticFieldBase(target);
            long off = u.staticFieldOffset(target);
            Object leaked = u.getObject(base, off); // アクセスチェックを経ない生読み取り
            if (leaked != null) {
                leak(name, "Unsafe のオフセット読み取りで内部値を取得: " + leaked);
            } else {
                unexpected(name, "Unsafe 読み取りが null を返した（未初期化?）");
            }
        } catch (Throwable t) {
            pass(name, "Unsafe 経由の内部読み取りが失敗: " + describe(t));
        }
    }

    /**
     * P7: MethodHandles.Lookup.IMPL_LOOKUP（TRUSTED な全能 Lookup）を外部モジュールが取得し、
     * findStaticVarHandle でモジュール export を無視して本体内部へアクセスできるか。
     * 本体 UnsafeHelper は Unsafe 経由でこれを読み出している。
     */
    private void probeImplLookupAccess() {
        String name = "P7-impl-lookup[UnsafeHelper.UNSAFE]";
        MethodHandles.Lookup impl;
        try {
            Unsafe u = acquireUnsafe();
            Field implField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object base = u.staticFieldBase(implField);
            long off = u.staticFieldOffset(implField);
            impl = (MethodHandles.Lookup) u.getObject(base, off);
            if (impl == null) {
                pass(name, "IMPL_LOOKUP の取得結果が null");
                return;
            }
            LOGGER.warn("[FT-PROBE] P7: IMPL_LOOKUP を入手（全能 Lookup, lookupClass={}）", impl.lookupClass());
        } catch (Throwable t) {
            pass(name, "IMPL_LOOKUP 取得が拒否された: " + describe(t));
            return;
        }
        try {
            Class<?> c = Class.forName(HELPER_CLASS);
            VarHandle vh = impl.findStaticVarHandle(c, "UNSAFE", Unsafe.class);
            Object leaked = vh.get();
            if (leaked != null) {
                leak(name, "IMPL_LOOKUP 経由で内部値へアクセス成功: " + leaked);
            } else {
                unexpected(name, "IMPL_LOOKUP アクセスが null を返した（未初期化?）");
            }
        } catch (Throwable t) {
            pass(name, "IMPL_LOOKUP 経由の内部アクセスが失敗: " + describe(t));
        }
    }

    /** 外部モジュールが sun.misc.Unsafe を独自取得する（本体 UnsafeHelper と同じ手順）。 */
    private static Unsafe acquireUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    // ---- サマリ ---------------------------------------------------------------

    /** サマリを出力し、失敗件数（LEAKED + UNEXPECTED）を返す。 */
    private int printSummary() {
        int passed = 0;
        int leaked = 0;
        int unexpected = 0;
        for (ProbeResult r : results) {
            switch (r.result()) {
                case DENIED -> passed++;
                case LEAKED -> leaked++;
                case UNEXPECTED -> unexpected++;
            }
        }
        boolean allGood = leaked == 0 && unexpected == 0;
        String verdict = allGood ? "ALL PASSED" : (leaked > 0 ? "LEAK DETECTED" : "CHECK RESULTS");

        String[] banner = {
                "",
                "============================================================",
                "  FT ACCESS-DENIAL TEST: " + verdict,
                "  passed=" + passed + "  LEAKED=" + leaked + "  unexpected=" + unexpected
                        + "  (total=" + results.size() + ")",
                "============================================================",
                ""
        };
        for (String line : banner) {
            if (allGood) {
                LOGGER.warn("[FT-PROBE] {}", line);
            } else {
                LOGGER.error("[FT-PROBE] {}", line);
            }
        }
        return leaked + unexpected;
    }

    private static String shortPkg(String pkg) {
        int i = pkg.lastIndexOf('.');
        return i < 0 ? pkg : pkg.substring(i + 1);
    }

    private static String describe(Throwable t) {
        return t.getClass().getName() + ": " + t.getMessage();
    }
}
