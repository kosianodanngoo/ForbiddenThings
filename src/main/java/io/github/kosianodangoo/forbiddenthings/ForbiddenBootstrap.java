package io.github.kosianodangoo.forbiddenthings;

import com.sun.tools.attach.VirtualMachine;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.modlauncher.LaunchPluginHandler;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import io.github.kosianodangoo.forbiddenthings.agent.ForbiddenAgent;
import io.github.kosianodangoo.forbiddenthings.transformer.BytecodeGetterTransformer;
import io.github.kosianodangoo.forbiddenthings.common.helper.UnsafeHelper;
import io.github.kosianodangoo.forbiddenthings.transformer.EntityMethods;
import io.github.kosianodangoo.forbiddenthings.transformer.ForbiddenLaunchPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class ForbiddenBootstrap {
    private static final String AGENT_CLASS = "io.github.kosianodangoo.forbiddenthings.agent.ForbiddenAgent";
    private static final String AGENT_RESOURCE = "io/github/kosianodangoo/forbiddenthings/agent/ForbiddenAgent.class";
    private static final String BRIDGE_CLASS = "io.github.kosianodangoo.forbiddenthings.agent.BytecodeBridge";
    private static final String BRIDGE_RESOURCE = "io/github/kosianodangoo/forbiddenthings/agent/BytecodeBridge.class";

    public static volatile Instrumentation instrumentation = null;
    public static volatile boolean LAUNCH_PLUGIN_AVAILABLE = false;
    private static volatile boolean STARTED = false;

    private ForbiddenBootstrap() {}

    public static void start() {
        if (STARTED) return;
        synchronized (ForbiddenBootstrap.class) {
            if (STARTED) return;
            STARTED = true;
        }
        EntityMethods.class.getClass();
        try {
            ForbiddenThings.LOGGER.debug("Initialize Start");
            if (!LAUNCH_PLUGIN_AVAILABLE) {
                LAUNCH_PLUGIN_AVAILABLE = initLaunchPlugin();
            }
            if (instrumentation == null) {
                if (!initAgent()) return;
                instrumentation = fetchInstrumentation();
                if (instrumentation == null) return;
                Class.forName("io.github.kosianodangoo.forbiddenthings.transformer.BytecodeGetterTransformer");
                if (!registerBridge()) return;
                instrumentation.addTransformer(new BytecodeGetterTransformer(), true);
                instrumentation.retransformClasses(ModuleClassLoader.class);
            }
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("ForbiddenBootstrap.start failed", t);
        }
    }

    private static boolean initLaunchPlugin() {
        try {
            ILaunchPluginService plugin = new ForbiddenLaunchPlugin();

            Field field = Launcher.class.getDeclaredField("launchPlugins");
            field.setAccessible(true);
            LaunchPluginHandler pluginHandler = (LaunchPluginHandler) field.get(Launcher.INSTANCE);
            field = LaunchPluginHandler.class.getDeclaredField("plugins");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ILaunchPluginService> map = (Map<String, ILaunchPluginService>) field.get(pluginHandler);
            map.put(plugin.name(), plugin);
            return true;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ForbiddenThings.LOGGER.error(e.toString());
            return false;
        }
    }

    private static boolean initAgent() {
        try {
            if (!UnsafeHelper.allowAttachSelf()) {
                ForbiddenThings.LOGGER.debug("Could not force attach-self via Unsafe; relying on -Djdk.attach.allowAttachSelf");
            }
            File agentJar = buildAgentJar();
            ForbiddenThings.LOGGER.debug("Agent jar: {}", agentJar.getAbsolutePath());
            String pid = String.valueOf(ProcessHandle.current().pid());
            VirtualMachine vm = VirtualMachine.attach(pid);
            try {
                vm.loadAgent(agentJar.getAbsolutePath());
            } finally {
                vm.detach();
            }
            return true;
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("Agent load failed", t);
            return false;
        }
    }

    private static Instrumentation fetchInstrumentation() {
        try {
            Class<?> agentSys = Class.forName(AGENT_CLASS, true, ClassLoader.getSystemClassLoader());
            return (Instrumentation) agentSys.getField("INSTRUMENTATION").get(null);
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("Instrumentation handle unavailable", t);
            return null;
        }
    }

    private static boolean registerBridge() {
        try {
            Class<?> bridgeCls = Class.forName(BRIDGE_CLASS, true, ClassLoader.getSystemClassLoader());
            Field f = bridgeCls.getField("transformer");
            BiFunction<Optional<byte[]>, String, Optional<byte[]>> fn = BytecodeGetterTransformer::transformOptionalBytes;
            f.set(null, fn);
            return true;
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("BytecodeBridge registration failed", t);
            return false;
        }
    }

    private static File buildAgentJar() throws IOException {
        byte[] agentBytes = readResource(AGENT_RESOURCE);
        byte[] bridgeBytes = readResource(BRIDGE_RESOURCE);
        Manifest mf = new Manifest();
        Attributes a = mf.getMainAttributes();
        a.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        a.putValue("Agent-Class", AGENT_CLASS);
        a.putValue("Premain-Class", AGENT_CLASS);
        a.putValue("Can-Retransform-Classes", "true");
        a.putValue("Can-Redefine-Classes", "true");
        File jar = File.createTempFile("forbidden/forbidden-agent-", ".jar");
        jar.deleteOnExit();
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar), mf)) {
            jos.putNextEntry(new JarEntry(AGENT_RESOURCE));
            jos.write(agentBytes);
            jos.closeEntry();
            jos.putNextEntry(new JarEntry(BRIDGE_RESOURCE));
            jos.write(bridgeBytes);
            jos.closeEntry();
        }
        return jar;
    }

    private static byte[] readResource(String resource) throws IOException {
        try (InputStream in = ForbiddenAgent.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) throw new IOException("Resource not found: " + resource);
            return in.readAllBytes();
        }
    }
}
