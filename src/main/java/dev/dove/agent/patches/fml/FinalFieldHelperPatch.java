package dev.dove.agent.patches.fml;

import dev.dove.agent.hooks.ReflectionHooks;
import dev.dove.agent.transform.Bytecode;
import dev.dove.agent.transform.ClassPatch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Set;

/**
 * {@code @ObjectHolder} injection on 1.12.2: ObjectHolderRef fills mods' static final fields through
 * its FinalFieldHelper, which strips {@code FINAL} through {@code Field.modifiers} and writes through
 * {@code sun.reflect.ReflectionFactory} field accessors - both gone on Java 25, so loading a world
 * with any mod using {@code @ObjectHolder} (Forge itself does) fails.
 * <p>
 * Its two methods are replaced: {@code makeWritable} only makes the field accessible, and
 * {@code setField} writes through {@link ReflectionHooks#setField}.
 */
public final class FinalFieldHelperPatch implements ClassPatch {

    private static final String FIELD = "java/lang/reflect/Field";

    @Override
    public String name() {
        return "FinalFieldHelper";
    }

    @Override
    public Set<String> targets() {
        return Set.of("net/minecraftforge/registries/ObjectHolderRef$FinalFieldHelper");
    }

    @Override
    public boolean apply(ClassNode node) {
        MethodNode makeWritable = Bytecode.method(node, "makeWritable", "(L" + FIELD + ";)L" + FIELD + ";");
        MethodNode setField = Bytecode.method(node, "setField", "(L" + FIELD + ";Ljava/lang/Object;Ljava/lang/Object;)V");
        if (makeWritable == null || setField == null) {
            return false;
        }

        InsnList writable = new InsnList();
        writable.add(new VarInsnNode(Opcodes.ALOAD, 0));
        writable.add(new InsnNode(Opcodes.ICONST_1));
        writable.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, FIELD, "setAccessible", "(Z)V", false));
        writable.add(new VarInsnNode(Opcodes.ALOAD, 0));
        writable.add(new InsnNode(Opcodes.ARETURN));
        Bytecode.replaceBody(makeWritable, writable);

        InsnList set = new InsnList();
        set.add(new VarInsnNode(Opcodes.ALOAD, 0));
        set.add(new VarInsnNode(Opcodes.ALOAD, 1));
        set.add(new VarInsnNode(Opcodes.ALOAD, 2));
        set.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ReflectionHooks.OWNER, "setField",
                "(L" + FIELD + ";Ljava/lang/Object;Ljava/lang/Object;)V", false));
        set.add(new InsnNode(Opcodes.RETURN));
        Bytecode.replaceBody(setField, set);
        return true;
    }
}
