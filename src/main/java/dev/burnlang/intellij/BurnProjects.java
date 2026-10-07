package dev.burnlang.intellij;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import com.redhat.devtools.lsp4ij.LanguageServerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

public final class BurnProjects {
    public static final String MANIFEST = "burn.toml";
    private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

    private BurnProjects() {
    }

    public static @Nullable VirtualFile root(@NotNull Project project, @Nullable VirtualFile near) {
        VirtualFile dir = near == null ? null : (near.isDirectory() ? near : near.getParent());
        while (dir != null) {
            if (dir.findChild(MANIFEST) != null) {
                return dir;
            }
            dir = dir.getParent();
        }
        String base = project.getBasePath();
        if (base == null) {
            return null;
        }
        VirtualFile baseDir = LocalFileSystem.getInstance().findFileByPath(base);
        return baseDir != null && baseDir.findChild(MANIFEST) != null ? baseDir : null;
    }

    public static void reload(@NotNull Project project, @Nullable VirtualFile near) {
        VirtualFile root = root(project, near);
        if (root == null) {
            notify(project, "No burn.toml found, so there is no Burn project to reload.", NotificationType.WARNING);
            return;
        }
        if (!RUNNING.compareAndSet(false, true)) {
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        GeneralCommandLine cmd = new GeneralCommandLine(BurnToolchain.ash(), "sync")
                .withWorkDirectory(root.getPath())
                .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
                .withEnvironment("NO_COLOR", "1")
                .withCharset(java.nio.charset.StandardCharsets.UTF_8);
        try {
            OSProcessHandler handler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(cmd);
            ProcessTerminatedListener.attach(handler);
            handler.addProcessListener(new ProcessAdapter() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    RUNNING.set(false);
                    ApplicationManager.getApplication().invokeLater(() -> finished(project, root, event.getExitCode() == 0));
                }
            });
            new RunContentExecutor(project, handler).withTitle("ash sync").run();
        } catch (ExecutionException ex) {
            RUNNING.set(false);
            notify(project, "Could not run ash: " + ex.getMessage() + ". Install it with burnup.", NotificationType.ERROR);
        }
    }

    private static void finished(@NotNull Project project, @NotNull VirtualFile root, boolean ok) {
        if (project.isDisposed()) {
            return;
        }
        if (!ok) {
            notify(project, "ash sync failed; see the Run tool window.", NotificationType.ERROR);
            return;
        }
        VirtualFile manifest = root.findChild(MANIFEST);
        if (manifest != null) {
            BurnManifestState.getInstance(project).markLoaded(manifest);
        }
        root.refresh(true, true);
        LanguageServerManager servers = LanguageServerManager.getInstance(project);
        servers.stop("burn");
        servers.start("burn");
        EditorNotifications.getInstance(project).updateAllNotifications();
        notify(project, "Reloaded the Burn project in " + root.getName() + ".", NotificationType.INFORMATION);
    }

    private static void notify(@NotNull Project project, @NotNull String message, @NotNull NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Burn")
                .createNotification(message, type)
                .notify(project);
    }
}
