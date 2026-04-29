package io.github.kosianodangoo.forbiddenthings.transformer;

import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;

public class ForbiddenLaunchPlugin implements ILaunchPluginService {

    @Override
    public String name() {
        return "forbidden_plugin";
    }

    @Override
    public EnumSet<Phase> handlesClass(Type type, boolean b) {
        if (type.getClassName().startsWith("io.github.kosianodangoo.forbiddenthings.transformer"))
            return EnumSet.noneOf(Phase.class);
        return EnumSet.of(Phase.AFTER, Phase.BEFORE);
    }

    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType, String reason) {
        if (classNode.name.startsWith("io/github/kosianodangoo/forbiddenthings/transformer"))
            return false;
        if (reason.equals(ITransformerActivity.CLASSLOADING_REASON)) {
            return GenericTransformer.transform(phase == Phase.AFTER ? GenericTransformer.Phase.ILaunchPluginService : GenericTransformer.Phase.ILaunchPluginServiceBefore, classNode);
        }
        return false;
    }

}
