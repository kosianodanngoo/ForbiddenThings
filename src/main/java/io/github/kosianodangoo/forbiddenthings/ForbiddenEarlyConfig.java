package io.github.kosianodangoo.forbiddenthings;


import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.nio.file.Path;


public class ForbiddenEarlyConfig {
    private static boolean breakMyReference = true;
    private static boolean breakStringReference = true;
    private static boolean denyReflection = true;
    private static boolean closeModule = true;

    static {
        ForbiddenEarlyConfig.loadEarlyConfig();
    }

    public static void loadEarlyConfig() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(ForbiddenThings.MODID + "-early.toml");

        CommentedFileConfig config = CommentedFileConfig.builder(configPath)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        config.load();

        breakMyReference = getOrElse(config, "break_my_reference", true, "Break other mods' reference in bytecodes to prevent other mods from accessing this mod.");
        breakStringReference = getOrElse(config, "break_string_reference", true, "Break other mod's reference which is a String.");

        denyReflection = getOrElse(config, "deny_reflection", true, "Disable reflections to prevent other mods from accessing this mod.");
        closeModule = getOrElse(config, "close_module", true, "Disable module's automatic open to prevent other mods from accessing this mod.");

        config.save();
        config.close();
    }

    private static <T> T getOrElse(CommentedFileConfig config, String path, T defaultValue, @Nullable String comment) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
        }
        if (comment != null) {
            config.setComment(path, comment);
        }
        return config.get(path);
    }

    public static boolean shouldBreakMyReference() {
        return breakMyReference;
    }

    public static boolean shouldBreakStringReference() {
        return breakStringReference;
    }

    public static boolean shouldDenyReflection() {
        return denyReflection;
    }

    public static boolean shouldCloseModule() {
        return closeModule;
    }
}