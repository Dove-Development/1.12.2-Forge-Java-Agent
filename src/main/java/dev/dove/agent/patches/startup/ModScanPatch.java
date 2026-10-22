package dev.dove.agent.patches.startup;

import dev.dove.agent.patches.asm.GameAsm;
import dev.dove.agent.transform.ClassPatch;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Set;

/**
 * FML reads every class of every mod jar to find its annotations ({@code @Mod},
 * {@code @EventHandler}, ...). ASMModParser reads each class with ASM including all of its method
 * bodies, though it only listens for annotations - which sit outside the code. It now tells ASM to
 * skip the code ({@code SKIP_CODE}), which reports every annotation just the same.
 */
public final class ModScanPatch implements ClassPatch {

    private static final String PARSER = "net/minecraftforge/fml/common/discovery/asm/ASMModParser";
    private static final int SKIP_CODE = 1;

    @Override
    public String name() {
        return "ModScan";
    }

    @Override
    public Set<String> targets() {
        return Set.of(PARSER);
    }

    @Override
    public boolean apply(ClassNode node) {
        return skipCode(node);
    }

    private static boolean skipCode(ClassNode node) {
        String accept = "(L" + GameAsm.PACKAGE + "/ClassVisitor;I)V";
        boolean changed = false;
        for (MethodNode method : node.methods) {
            if (!method.name.equals("<init>")) {
                continue;
            }
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(GameAsm.CLASS_READER)
                        && call.name.equals("accept") && call.desc.equals(accept)
                        && call.getPrevious().getOpcode() == Opcodes.ICONST_0) {
                    method.instructions.set(call.getPrevious(), new InsnNode(Opcodes.ICONST_0 + SKIP_CODE));
                    changed = true;
                }
            }
        }
        return changed;
    }
}
