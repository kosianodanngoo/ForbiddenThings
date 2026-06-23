package io.github.kosianodangoo.forbiddenthings.transformer;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Optional;
import java.util.stream.Stream;

public class BytecodeGetterTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"cpw/mods/cl/ModuleClassLoader".equals(className)) return null;
        ForbiddenThings.LOGGER.info("Found Transform class " + className);

        try {
            byte[] result = transformClass(classfileBuffer, loader);
            if (result != null && result != classfileBuffer) {
                return result;
            }
        } catch (Exception e) {
            ForbiddenThings.LOGGER.error("Transform failed", e);
            return null;
        }
        return null;
    }

    private byte[] transformClass(byte[] bytes, ClassLoader loader) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }

        try {
            ClassReader cr = new ClassReader(bytes);

            ClassNode cn = new ClassNode(Opcodes.ASM9);
            cr.accept(cn, ClassReader.EXPAND_FRAMES);
            boolean modified = false;

            if (cn.methods != null) {
                for (MethodNode mn : cn.methods) {
                    ForbiddenThings.LOGGER.info("method name, {}, desc= {}", mn.name, mn.desc);
                    if ("getClassBytes".equals(mn.name) &&
                            "(Ljava/lang/module/ModuleReader;Ljava/lang/module/ModuleReference;Ljava/lang/String;)[B".equals(mn.desc)) {
                        ForbiddenThings.LOGGER.info("Found target method, injecting...");
                        for (AbstractInsnNode insnNode : mn.instructions) {
                            if (insnNode instanceof MethodInsnNode methodInsn && methodInsn.name.equals("findFirst")) {
                                InsnList insnList = new InsnList();
                                insnList.add(new VarInsnNode(Opcodes.ALOAD, 3));
                                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "io/github/kosianodangoo/forbiddenthings/agent/BytecodeBridge", "transformStreamBytes", "(Ljava/util/stream/Stream;Ljava/lang/String;)Ljava/util/stream/Stream;"));
                                mn.instructions.insertBefore(insnNode, insnList);
                                mn.maxStack += 1;
                                GenericTransformer.availableGetBytecode = true;
                                break;
                            }
                        }
                        modified = true;
                        break;
                    }
                }
            }

            if (!modified) {
                return bytes;
            }

            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(cw);
            return cw.toByteArray();

        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("Transform failed: " + t.getClass().getName() + " - " + t.getMessage(), t);
            return bytes;
        }
    }

    public static Stream<byte[]> transformStreamBytes(Stream<byte[]> stream, String className) {
        if (stream == null || GenericTransformer.exclusivePackages.stream().anyMatch(className::startsWith)) {
            return stream;
        }
        return stream.map(bytes -> transformBytes(bytes, className));
    }

    public static Optional<byte[]> transformOptionalBytes(Optional<byte[]> optionalBytes, String className) {
        if (optionalBytes.isEmpty() || GenericTransformer.exclusivePackages.stream().anyMatch(className::startsWith)) {
            return optionalBytes;
        }
        return Optional.of(transformBytes(optionalBytes.orElse(new byte[0]), className));
    }

    private static byte[] transformBytes(byte[] bytes, String className) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }

        ClassNode classNode;
        boolean modified;
        try {
            ClassReader classReader = new ClassReader(bytes);
            classNode = new ClassNode(Opcodes.ASM9);
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
            modified = GenericTransformer.transform(GenericTransformer.Phase.GetBytecode, classNode);
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("transformBytes: read/transform failed for {} ({}): {}",
                    className, t.getClass().getName(), t.getMessage(), t);
            return bytes;
        }

        if (!modified) {
            return bytes;
        }

        try {
            ClassWriter cw = new HierarchyAwareClassWriter(classNode, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(cw);
            byte[] out = cw.toByteArray();
            //ForbiddenThings.LOGGER.debug("transformBytes: wrote {} ({} -> {} bytes)", className, bytes.length, out.length);
            return out;
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("transformBytes: write failed for {} ({}): {} -- falling back to original bytes",
                    className, t.getClass().getName(), t.getMessage(), t);
            return bytes;
        }
    }
}
