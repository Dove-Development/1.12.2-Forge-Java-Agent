package dev.dove.agent.patches.minecraft;

import dev.dove.agent.hooks.ResourceHooks;
import dev.dove.agent.transform.Bytecode;
import dev.dove.agent.transform.ClassPatch;
import org.objectweb.asm.tree.ClassNode;

import java.util.Set;

/**
 * FolderResourcePack's path case check calls {@code File.getCanonicalPath()} for every resource
 * the game loads, which on Java 25 goes to the file system every time; it now goes through
 * {@link ResourceHooks#canonicalPath}, Java 8's canonicalization cache for these calls.
 */
public final class CanonicalPathCachePatch implements ClassPatch {

    @Override
    public String name() {
        return "CanonicalPathCache";
    }

    @Override
    public Set<String> targets() {
        return Set.of("net/minecraft/client/resources/FolderResourcePack");
    }

    @Override
    public boolean apply(ClassNode node) {
        return Bytecode.redirectCall(node, "java/io/File", "getCanonicalPath", "()Ljava/lang/String;",
                ResourceHooks.OWNER, "canonicalPath", "(Ljava/io/File;)Ljava/lang/String;") > 0;
    }
}
