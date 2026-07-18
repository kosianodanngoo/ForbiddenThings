package io.github.kosianodangoo.forbiddenthings.common.helper;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class UnsafeHelper {
    public static final Unsafe UNSAFE;
    public static final boolean AVAILABLE;
    public static final MethodHandles.Lookup IMPL_LOOKUP;

    public static final long OVERRIDE_OFFSET = 12;
    private static final Method GET_DECLARED_FIELDS0;

    static {
        Unsafe unsafe = null;
        boolean available = false;
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            available = true;
        } catch (Exception ignored) {
        }
        UNSAFE = unsafe;
        AVAILABLE = available;
        Method getDeclaredFields0 = null;
        try {
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            forceSetAccessible(getDeclaredFields0);
        } catch (Throwable ignored) {
        }
        GET_DECLARED_FIELDS0 = getDeclaredFields0;
        MethodHandles.Lookup implLookup = null;
        try {
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object base = unsafe.staticFieldBase(implLookupField);
            long offset = unsafe.staticFieldOffset(implLookupField);
            implLookup = (MethodHandles.Lookup) unsafe.getObject(base, offset);
        } catch (Throwable ignored) {
        }
        IMPL_LOOKUP = implLookup;
    }

    public static boolean allowAttachSelf() {
        if (!AVAILABLE) return false;

        try {
            Class<?> clazz = Class.forName("sun.tools.attach.HotSpotVirtualMachine");
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType() == boolean.class && f.getName().equals("ALLOW_ATTACH_SELF")) {
                    long offset = UNSAFE.staticFieldOffset(f);
                    Object base = UNSAFE.staticFieldBase(f);
                    UNSAFE.putBoolean(base, offset, true);
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static long getFieldOffset(@NotNull Field field) {
        if (!AVAILABLE) {
            return -1;
        }
        try {
            return UNSAFE.objectFieldOffset(field);
        } catch (Throwable e) {
            return -1;
        }
    }

    public static boolean putObject(Object object, long offset, Object value) {
        if (!AVAILABLE || offset == -1) {
            return false;
        }
        try {
            UNSAFE.putObject(object, offset, value);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean putBoolean(Object object, long offset, boolean value) {
        if (!AVAILABLE || offset == -1) {
            return false;
        }
        try {
            UNSAFE.putBoolean(object, offset, value);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean forceSetAccessible(AccessibleObject accessibleObject) {
        if (accessibleObject.trySetAccessible()) {
            return true;
        }
        if (!AVAILABLE) {
            return false;
        }
        try {
            UNSAFE.putBoolean(accessibleObject, OVERRIDE_OFFSET, true);
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }

    public static Field[] getDeclaredFieldsForce(Class<?> clazz) {
        try {
            return (Field[]) GET_DECLARED_FIELDS0.invoke(clazz);
        } catch (Throwable throwable) {
            return new Field[0];
        }
    }


    public static @Nullable MethodHandle safeFindStatic(Class<?> clazz, String name, MethodType methodType) {
        try {
            return UnsafeHelper.IMPL_LOOKUP.findStatic(clazz, name, methodType);
        } catch (Throwable e) {
            return null;
        }
    }

    public static @Nullable MethodHandle safeFindVirtual(Class<?> clazz, String name, MethodType methodType) {
        try {
            return UnsafeHelper.IMPL_LOOKUP.findVirtual(clazz, name, methodType);
        } catch (Throwable e) {
            return null;
        }
    }

    public static @Nullable VarHandle safeFindVarHandle(Class<?> clazz, String name, Class<?> type) {
        try {
            return UnsafeHelper.IMPL_LOOKUP.findVarHandle(clazz, name, type);
        } catch (Throwable e) {
            return null;
        }
    }

    public static @Nullable VarHandle safeFindStaticVarHandle(Class<?> clazz, String name, Class<?> type) {
        try {
            return UnsafeHelper.IMPL_LOOKUP.findStaticVarHandle(clazz, name, type);
        } catch (Throwable e) {
            return null;
        }
    }

    public static @Nullable Class<?> safeFindClass(String name) {
        try {
            return UnsafeHelper.IMPL_LOOKUP.findClass(name);
        } catch (Throwable e) {
            return null;
        }
    }


    public static void denyReflection(Collection<Class<?>> targets) {
        try {
            Class<?> clazz = safeFindClass("jdk.internal.reflect.Reflection");
            Map<Class<?>, Set<String>> targetMap = new HashMap<>();
            for (Class<?> target : targets) {
                targetMap.put(target, Set.of("*"));
            }
            VarHandle fieldVarHandle = safeFindStaticVarHandle(clazz, "fieldFilterMap", Map.class);
            Set<Map.Entry<Class<?>, Set<String>>> targetEntries = targetMap.entrySet();
            Set<Map.Entry<Class<?>, Set<String>>> oldFieldMap = ((Map<Class<?>, Set<String>>) fieldVarHandle.get()).entrySet();
            Map.Entry<String, Object>[] combinedFieldEntries = Stream.concat(
                    oldFieldMap.stream(),
                    targetEntries.stream()
            ).toArray(Map.Entry[]::new);
            fieldVarHandle.set(Map.ofEntries(combinedFieldEntries));
            VarHandle methodVarHandle = safeFindStaticVarHandle(clazz, "methodFilterMap", Map.class);
            Set<Map.Entry<Class<?>, Set<String>>> oldMethodMap = ((Map<Class<?>, Set<String>>) methodVarHandle.get()).entrySet();
            Map.Entry<String, Object>[] combinedMethodEntries = Stream.concat(
                    oldMethodMap.stream(),
                    targetEntries.stream()
            ).toArray(Map.Entry[]::new);
            fieldVarHandle.set(Map.ofEntries(combinedMethodEntries));
        } catch (Throwable ignored) {
        }
    }
}