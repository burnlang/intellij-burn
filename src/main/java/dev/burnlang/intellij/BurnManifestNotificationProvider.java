package dev.burnlang.intellij;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.util.function.Function;

public final class BurnManifestNotificationProvider implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
            @NotNull Project project, @NotNull VirtualFile file) {
        if (!BurnProjects.MANIFEST.equals(file.getName()) || !BurnManifestState.getInstance(project).changed(file)) {
            return null;
        }
        return editor -> {
            EditorNotificationPanel panel = new EditorNotificationPanel(editor, EditorNotificationPanel.Status.Info);
            panel.setText("burn.toml changed. Reload the project to install its packages.");
            panel.createActionLabel("Load Burn Changes", () -> BurnProjects.reload(project, file));
            panel.createActionLabel("Ignore", () -> {
                BurnManifestState.getInstance(project).markLoaded(file);
                EditorNotifications.getInstance(project).updateNotifications(file);
            });
            return panel;
        };
    }
}
