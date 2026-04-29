package io.github.kosianodangoo.forbiddenthings.transformer;

import io.github.kosianodangoo.forbiddenthings.ForbiddenThings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GenericTransformer {
    static String ENTITY_METHODS = "io/github/kosianodangoo/forbiddenthings/transformer/EntityMethods";
    public static List<String> exclusivePackages = new ArrayList<>();
    public static List<String> exclusiveInstructionWrappingPackages = new ArrayList<>();
    static boolean initialized = false;
    static boolean tickInjected = false;
    static boolean availableGetBytecode = false;
    public static boolean breakMyReference = false;

    @SuppressWarnings("unused")
    public enum Phase {
        GetBytecode, ILaunchPluginServiceBefore, ITransformationService, PostMixin, ILaunchPluginService, ClassFileTransformer
    }

    static {
        initialize();
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        exclusivePackages.add("io/github/kosianodangoo/forbiddenthings/transformer");
        initialized = true;
    }

    public static boolean transform(Phase phase, ClassNode classNode) {
        //ForbiddenThings.LOGGER.debug("Transform: {}", classNode.name);
        if (exclusivePackages.stream().anyMatch(packageName -> classNode.name.startsWith(packageName)))
            return false;
        boolean modified = false;

        boolean shouldWrapInsn = ((availableGetBytecode && phase == Phase.GetBytecode) || (!availableGetBytecode && phase == Phase.ILaunchPluginServiceBefore)) && exclusiveInstructionWrappingPackages.stream().noneMatch(packageName -> classNode.name.startsWith(packageName));
        boolean shouldModifyReturn = phase == Phase.ILaunchPluginService;

        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode methodInsn) {
                    if ((insn.getOpcode() == Opcodes.INVOKEVIRTUAL || insn.getOpcode() == Opcodes.INVOKEINTERFACE) && shouldWrapInsn) {
                        if (isSameMethod(methodInsn.owner, methodInsn, "net/minecraft/world/entity/LivingEntity", "m_21223_", "getHealth", "()F", false)) {
                            method.instructions.insertBefore(methodInsn, new InsnNode(Opcodes.DUP));
                            InsnList insnList = new InsnList();
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "getHealth", "(FLnet/minecraft/world/entity/LivingEntity;)F"));
                            method.instructions.insert(methodInsn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(methodInsn.owner, methodInsn, "net/minecraft/world/entity/LivingEntity", "m_21224_", "isDeadOrDying", "()Z", false)) {
                            method.instructions.insertBefore(methodInsn, new InsnNode(Opcodes.DUP));
                            InsnList insnList = new InsnList();
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "isDeadOrDying", "(ZLnet/minecraft/world/entity/LivingEntity;)Z"));
                            method.instructions.insert(methodInsn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(methodInsn.owner, methodInsn, "net/minecraft/world/entity/Entity", "m_6084_", "isAlive", "()Z", false)) {
                            method.instructions.insertBefore(methodInsn, new InsnNode(Opcodes.DUP));
                            InsnList insnList = new InsnList();
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "isAlive", "(ZLnet/minecraft/world/entity/Entity;)Z"));
                            method.instructions.insert(methodInsn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(methodInsn.owner, methodInsn, "net/minecraft/world/entity/Entity", "m_240725_", "isRemoved", "()Z", false)) {
                            method.instructions.insertBefore(methodInsn, new InsnNode(Opcodes.DUP));
                            InsnList insnList = new InsnList();
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "isRemoved", "(ZLnet/minecraft/world/entity/Entity;)Z"));
                            method.instructions.insert(methodInsn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(methodInsn.owner, methodInsn, "net/minecraft/world/entity/Entity", "m_146911_", "getRemovalReason", "()Lnet/minecraft/world/entity/Entity$RemovalReason;", false)) {
                            method.instructions.insertBefore(methodInsn, new InsnNode(Opcodes.DUP));
                            InsnList insnList = new InsnList();
                            insnList.add(new InsnNode(Opcodes.SWAP));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "getRemovalReason", "(Lnet/minecraft/world/entity/Entity$RemovalReason;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity$RemovalReason;"));
                            method.instructions.insert(methodInsn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(classNode.name, method, "net/minecraft/world/level/entity/EntityTickList", "m_156910_", "forEach", "(Ljava/util/function/Consumer;)V", false) &&
                                isSameMethod(methodInsn.owner, methodInsn, "java/util/function/Consumer", "accept", "accept", "(Ljava/lang/Object;)V", true)) {
                            LabelNode skipLabelNode = new LabelNode(new Label());
                            LabelNode endLabelNode = new LabelNode(new Label());
                            InsnList insnListB = new InsnList();
                            insnListB.add(new InsnNode(Opcodes.DUP));
                            insnListB.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "shouldOverrideTick", "(Lnet/minecraft/world/entity/Entity;)Z"));
                            insnListB.add(new JumpInsnNode(Opcodes.IFGT, skipLabelNode));
                            InsnList insnListA = new InsnList();
                            insnListA.add(new JumpInsnNode(Opcodes.GOTO, endLabelNode));
                            insnListA.add(skipLabelNode);
                            insnListA.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "tickOverride", "(Ljava/util/function/Consumer;Lnet/minecraft/world/entity/Entity;)V"));
                            insnListA.add(endLabelNode);
                            insnListA.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
                            method.instructions.insertBefore(methodInsn, insnListB);
                            method.instructions.insert(methodInsn, insnListA);
                            method.maxStack += 1;
                            modified = true;
                        }
                    }
                } else if (shouldModifyReturn) {
                    if (insn.getOpcode() == Opcodes.FRETURN) {
                        if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/LivingEntity", "m_21223_", "getHealth", "()F", false)) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "getHealth", "(FLnet/minecraft/world/entity/LivingEntity;)F"));
                            method.instructions.insertBefore(insn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        }
                    } else if (insn.getOpcode() == Opcodes.IRETURN) {
                        if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/LivingEntity", "m_21224_", "isDeadOrDying", "()Z", false)) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "isDeadOrDying", "(ZLnet/minecraft/world/entity/LivingEntity;)Z"));
                            method.instructions.insertBefore(insn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/Entity", "m_6084_", "isAlive", "()Z", false)) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "isAlive", "(ZLnet/minecraft/world/entity/Entity;)Z"));
                            method.instructions.insertBefore(insn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        } else if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/Entity", "m_213877_", "isRemoved", "()Z", false)) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "isRemoved", "(ZLnet/minecraft/world/entity/Entity;)Z"));
                            method.instructions.insertBefore(insn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        }
                    } else if (insn.getOpcode() == Opcodes.ARETURN) {
                        if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/Entity", "m_146911_", "getRemovalReason", "()Lnet/minecraft/world/entity/Entity$RemovalReason;", false)) {
                            InsnList insnList = new InsnList();
                            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "getRemovalReason", "(Lnet/minecraft/world/entity/Entity$RemovalReason;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/entity/Entity$RemovalReason;"));
                            method.instructions.insertBefore(insn, insnList);
                            method.maxStack += 1;
                            modified = true;
                        }
                    }
                }
                if (breakMyReference) {
                    breakLdc:
                    if (insn instanceof LdcInsnNode ldcInsn) {
                        Object value = ldcInsn.cst;
                        if (value instanceof String string && string.toLowerCase().replace(".", "/").contains("io/github/kosianodangoo/forbiddenthings")) {
                            ldcInsn.cst = String.valueOf(classNode.hashCode());
                            modified = true;
                            break breakLdc;
                        }
                        if (value instanceof Type type && type.getInternalName().startsWith("io/github/kosianodangoo/forbiddenthings")) {
                            ldcInsn.cst = Type.getType(Objects.class);
                            modified = true;
                            break breakLdc;
                        }
                    }
                }
            }
            if (shouldModifyReturn) {
                if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/LivingEntity", "m_21223_", "getHealth", "()F", false)) {
                    injectHead(method,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "shouldReplaceHealthMethod", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "replaceGetHealth", "(Lnet/minecraft/world/entity/LivingEntity;)F", false),
                            new InsnNode(Opcodes.FRETURN));
                    method.maxStack += 1;
                    modified = true;
                } else if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/LivingEntity", "m_21224_", "isDeadOrDying", "()Z", false)) {
                    injectHead(method,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "shouldReplaceHealthMethod", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "replaceIsDeadOrDying", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new InsnNode(Opcodes.IRETURN));
                    method.maxStack += 1;
                    modified = true;
                } else if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/Entity", "m_6084_", "isAlive", "()Z", false)) {
                    injectHead(method,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "shouldReplaceHealthMethod", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "replaceIsAlive", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new InsnNode(Opcodes.IRETURN));
                    method.maxStack += 1;
                    modified = true;
                } else if (isSameMethod(classNode.name, method, "net/minecraft/world/entity/Entity", "m_6087_", "isPickable", "()Z", false)) {
                    injectHead(method,
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "shouldReplaceIsPickable", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "replaceIsPickable", "(Lnet/minecraft/world/entity/Entity;)Z", false),
                            new InsnNode(Opcodes.IRETURN));
                    method.maxStack += 1;
                    modified = true;
                }
            }
            if (!tickInjected && phase.ordinal() >= 2 && isSameMethod(classNode.name, method, "net/minecraft/server/level/ServerLevel", "m_8793_", "tick", "(Ljava/util/function/BooleanSupplier;)V", false)) {
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ENTITY_METHODS, "updateLastTicks", "(Lnet/minecraft/server/level/ServerLevel;)V"));
                method.instructions.insert(insnList);
                method.maxStack += 1;
                tickInjected = true;
                modified = true;
            }
        }
        return modified;
    }

    public static void injectHead(MethodNode method, MethodInsnNode judgeMethod, MethodInsnNode replaceMethod, InsnNode returnInsn) {
        LabelNode skipLabelNode = new LabelNode(new Label());
        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(judgeMethod);
        insnList.add(new JumpInsnNode(Opcodes.IFLE, skipLabelNode));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(replaceMethod);
        insnList.add(returnInsn);
        insnList.add(skipLabelNode);
        insnList.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        method.instructions.insertBefore(method.instructions.getFirst(), insnList);
    }

    public static boolean isSameMethod(String owner, MethodInsnNode methodInsn, String superClass, String obfName, String name, String desc, boolean isInterface) {
        if ((!obfName.equals(methodInsn.name) && !name.equals(methodInsn.name)) || !desc.equals(methodInsn.desc)) {
            return false;
        }

        return isSubclass(owner, superClass, isInterface);
    }

    public static boolean isSameMethod(String owner, MethodNode method, String superClass, String obfName, String name, String desc, boolean isInterface) {
        if ((!obfName.equals(method.name) && !name.equals(method.name)) || !desc.equals(method.desc)) {
            return false;
        }

        return isSubclass(owner, superClass, isInterface);
    }

    public static boolean isSubclass(String className, String superClass, boolean isInterface) {
        if (className.equals(superClass) || superClass.equals("java/lang/Object")) {
            return true;
        }

        if (className.equals("java/lang/Object")) {
            return false;
        }

        String currentName = className;

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        while (!currentName.equals("java/lang/Object")) {
            try (InputStream is = classLoader.getResourceAsStream(currentName.concat(".class"))) {
                ClassReader classReader = new ClassReader(Objects.requireNonNull(is));
                currentName = classReader.getSuperName();
                if (currentName.equals(superClass)) {
                    return true;
                }
                if (isInterface) {
                    for (String interfaceName : classReader.getInterfaces()) {
                        if (isSubclass(interfaceName, superClass, true)) {
                            return true;
                        }
                    }
                }
            } catch (Throwable e) {
                ForbiddenThings.LOGGER.error("Failed to find super Class", e);
                return false;
            }
        }

        return false;
    }
}
