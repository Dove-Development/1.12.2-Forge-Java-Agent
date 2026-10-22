package dev.dove.agent.hooks;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minecraft 1.12.2 checks the case of a resource's path with {@code File.getCanonicalPath()}
 * (FolderResourcePack.validatePath, also used by the default pack), every time it looks one up.
 * For resources inside the game jar the path it builds ({@code file:/C:/...jar!/assets/...}) is not
 * a valid Windows path: each check walks the folders to the game directory, fails, and Minecraft
 * falls back to reading the resource from the jar. About 9000 such checks for 2900 resources at
 * startup, near 2 ms each - on Java 8 too.
 * <p>
 * Answers - a path or the failure - are kept for 30 seconds, as Java 8's canonicalization cache
 * kept paths, so each resource is checked once instead of three times.
 */
public final class ResourceHooks {

    public static final String OWNER = "dev/dove/agent/hooks/ResourceHooks";

    /** How long an answer is kept, as Java 8's canonicalization cache did. */
    static final long KEEP_NANOS = 30_000_000_000L;

    private static final Map<String, Answer> CACHE = new ConcurrentHashMap<>();

    private ResourceHooks() {
    }

    /** Replaces {@code file.getCanonicalPath()}. */
    public static String canonicalPath(File file) throws IOException {
        String key = file.getPath();
        long now = System.nanoTime();
        Answer answer = CACHE.get(key);
        if (answer == null || now - answer.at >= KEEP_NANOS) {
            try {
                answer = new Answer(file.getCanonicalPath(), null, now);
            } catch (IOException e) {
                answer = new Answer(null, e.getMessage(), now);
            }
            CACHE.put(key, answer);
        }
        if (answer.path == null) {
            throw new IOException(answer.failure);
        }
        return answer.path;
    }

    private record Answer(String path, String failure, long at) {
    }
}
