package io.github.kosianodangoo.forbiddenthings.transformer;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class GenericClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classfileBuffer == null || classfileBuffer.length == 0) {
            return null;
        }
        if (className == null || GenericTransformer.exclusivePackages.stream().anyMatch(className::startsWith)) {
            return null;
        }

        ClassNode classNode;
        boolean modified;
        try {
            ClassReader classReader = new ClassReader(classfileBuffer);
            classNode = new ClassNode(Opcodes.ASM9);
            classReader.accept(classNode, ClassReader.EXPAND_FRAMES);
            modified = GenericTransformer.transform(GenericTransformer.Phase.ClassFileTransformer, classNode);
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("GenericClassFileTransformer: read/transform failed for {} ({}): {}",
                    className, t.getClass().getName(), t.getMessage(), t);
            return null;
        }

        if (!modified) {
            return null;
        }

        try {
            ClassWriter cw = new HierarchyAwareClassWriter(classNode, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classNode.accept(cw);
            return cw.toByteArray();
        } catch (Throwable t) {
            ForbiddenThings.LOGGER.error("GenericClassFileTransformer: write failed for {} ({}): {} -- keeping original bytes",
                    className, t.getClass().getName(), t.getMessage(), t);
            return null;
        }
    }
}
