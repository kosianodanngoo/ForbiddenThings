package io.github.kosianodangoo.forbiddenthings.agent;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class BytecodeBridge {
    private static final Logger LOGGER = Logger.getLogger("BytecodeBridge");
    public static volatile BiFunction<Optional<byte[]>, String, Optional<byte[]>> transformer;
    public static volatile BiFunction<Stream<byte[]>, String, Stream<byte[]>> streamTransformer;

    private BytecodeBridge() {}

    public static Optional<byte[]> transformOptionalBytes(Optional<byte[]> bytes, String className) {
        BiFunction<Optional<byte[]>, String, Optional<byte[]>> t = transformer;
        if (t == null || bytes == null) return bytes;
        try {
            return t.apply(bytes, className);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "BytecodeBridge.transformOptionalBytes failed for " + className, e);
            return bytes;
        }
    }

    public static Stream<byte[]> transformStreamBytes(Stream<byte[]> bytes, String className) {
        BiFunction<Stream<byte[]>, String, Stream<byte[]>> t = streamTransformer;
        if (t == null || bytes == null) return bytes;
        try {
            return t.apply(bytes, className);
        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "BytecodeBridge.transformStreamBytes failed for " + className, e);
            return bytes;
        }
    }
}
