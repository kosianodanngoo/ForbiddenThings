package io.github.kosianodangoo.forbiddenthings;


import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;


public class ForbiddenEarlyConfig {
    private static boolean breakMyReference = true;
    private static boolean breakStringReference = true;

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

        breakMyReference = getOrElse(config, "break_my_reference", true);
        breakStringReference = getOrElse(config, "break_string_reference", true);

        config.save();
        config.close();
    }

    private static <T> T getOrElse(Config config, String path, T defaultValue) {
        if (!config.contains(path)) {
            config.set(path, defaultValue);
        }
        return config.get(path);
    }

    public static boolean shouldBreakMyReference() {
        return breakMyReference;
    }

    public static boolean shouldBreakStringReference() {
        return breakStringReference;
    }
}