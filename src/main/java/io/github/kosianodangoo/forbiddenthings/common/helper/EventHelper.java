package io.github.kosianodangoo.forbiddenthings.common.helper;

import net.minecraftforge.eventbus.api.Event;

import java.lang.reflect.Field;

public class EventHelper {
    private static final boolean IS_CANCELED_AVAILABLE;
    private static final Field EVENT_IS_CANCELED_FIELD;
    private static final long EVENT_IS_CANCELED_OFFSET;

    static {
        Field eventIsCanceledField = null;
        boolean isCanceledAvailable = false;
        long isCanceledOffset = -1;
        try {
            eventIsCanceledField = Event.class.getDeclaredField("isCanceled");
            UnsafeHelper.forceSetAccessible(eventIsCanceledField);
            isCanceledAvailable = true;
            isCanceledOffset = UnsafeHelper.getFieldOffset(eventIsCanceledField);
        } catch (Throwable ignored) {
        }
        EVENT_IS_CANCELED_FIELD = eventIsCanceledField;
        IS_CANCELED_AVAILABLE = isCanceledAvailable;
        EVENT_IS_CANCELED_OFFSET = isCanceledOffset;
    }

    public static boolean forceSetCanceled(Event event, boolean isCanceled) {
        try {
            if (event.isCancelable()) {
                event.setCanceled(isCanceled);
            }
            if (event.isCanceled() == isCanceled) {
                return true;
            }
            if (IS_CANCELED_AVAILABLE) {
                EVENT_IS_CANCELED_FIELD.setBoolean(event, isCanceled);
                if (event.isCanceled() == isCanceled) {
                    return true;
                }
            }
            if (UnsafeHelper.putObject(event, EVENT_IS_CANCELED_OFFSET, isCanceled)) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }
}
