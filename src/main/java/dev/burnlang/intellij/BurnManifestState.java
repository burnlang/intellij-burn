package dev.burnlang.intellij;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
public final class BurnManifestState {
    private final Map<String, Integer> loaded = new ConcurrentHashMap<>();

    public static BurnManifestState getInstance(@NotNull Project project) {
        return project.getService(BurnManifestState.class);
    }

    private static int hash(@NotNull VirtualFile file) {
        try {
            return Arrays.hashCode(file.contentsToByteArray());
        } catch (IOException e) {
            return 0;
        }
    }

    public boolean changed(@NotNull VirtualFile manifest) {
        int now = hash(manifest);
        Integer before = loaded.putIfAbsent(manifest.getPath(), now);
        return before != null && before != now;
    }

    public void markLoaded(@NotNull VirtualFile manifest) {
        loaded.put(manifest.getPath(), hash(manifest));
    }
}
