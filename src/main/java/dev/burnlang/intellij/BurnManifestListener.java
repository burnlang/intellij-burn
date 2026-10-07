package dev.burnlang.intellij;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BurnManifestListener implements BulkFileListener {
    private final Project project;

    public BurnManifestListener(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
        for (VFileEvent event : events) {
            if (event.getPath().endsWith("/" + BurnProjects.MANIFEST)) {
                EditorNotifications.getInstance(project).updateAllNotifications();
                return;
            }
        }
    }
}
