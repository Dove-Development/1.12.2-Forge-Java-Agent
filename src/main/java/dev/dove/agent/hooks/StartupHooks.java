package dev.dove.agent.hooks;

import dev.dove.agent.util.ClassFiles;

/**
 * Shortcuts for work Forge does while the game loads that cannot change the result (see the
 * patches in {@code patches/startup}). Each answers from the class bytes alone, and says "do the
 * work" whenever it cannot be sure.
 */
public final class StartupHooks {

    public static final String OWNER = "dev/dove/agent/hooks/StartupHooks";

    private static final String SIDE_ONLY = "Lnet/minecraftforge/fml/relauncher/SideOnly;";
    private static final String SUBSCRIBE_EVENT = "Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;";

    private StartupHooks() {
    }

    /** SideTransformer only strips members annotated {@code @SideOnly}. */
    public static boolean skipSideTransformer(byte[] classFile) {
        return classFile == null || !ClassFiles.hasUtf8(classFile, SIDE_ONLY);
    }

    /** EventSubscriberTransformer only makes {@code @SubscribeEvent} methods public. */
    public static boolean skipEventSubscriberTransformer(byte[] classFile) {
        return classFile == null || !ClassFiles.hasUtf8(classFile, SUBSCRIBE_EVENT);
    }

    /** TerminalTransformer only reroutes calls to {@code System.exit}, {@code Runtime.exit} and {@code Runtime.halt}. */
    public static boolean skipTerminalTransformer(byte[] classFile) {
        return classFile == null || !ClassFiles.hasUtf8(classFile, "exit", "halt");
    }
}
