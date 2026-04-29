package io.github.kosianodangoo.forbiddenthings.common.helper;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UnsafeHelper {
    public static final Unsafe UNSAFE;
    public static final boolean AVAILABLE;

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
}