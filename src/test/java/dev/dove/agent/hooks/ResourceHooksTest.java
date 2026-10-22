package dev.dove.agent.hooks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceHooksTest {

    @Test
    void answersAsGetCanonicalPathDoes(@TempDir Path dir) throws Exception {
        Path file = Files.createFile(dir.resolve("Model.json"));
        File asWritten = new File(dir.toFile(), "sub/../Model.json");

        assertEquals(asWritten.getCanonicalPath(), ResourceHooks.canonicalPath(asWritten));
        // Asked again, from the cache.
        assertEquals(asWritten.getCanonicalPath(), ResourceHooks.canonicalPath(asWritten));
        assertEquals(file.toFile().getCanonicalPath(), ResourceHooks.canonicalPath(file.toFile()));
    }

    @Test
    void failuresAreRepeatedFromTheCache() {
        // What DefaultResourcePack checks for a resource in the game jar: not a valid path on Windows.
        org.junit.jupiter.api.Assumptions.assumeTrue(File.separatorChar == '\\');
        File inJar = new File("file:/C:/game.jar!/assets/minecraft/models/block/stone.json");

        IOException first = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> ResourceHooks.canonicalPath(inJar));
        IOException again = org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> ResourceHooks.canonicalPath(inJar));
        assertEquals(first.getMessage(), again.getMessage());
    }
}
