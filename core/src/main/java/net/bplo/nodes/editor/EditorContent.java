package net.bplo.nodes.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import net.bplo.nodes.Util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for working with content metadata such as
 * game asset files or config data, that needs to be surfaced
 * in the editor for use by nodes.
 */
public class EditorContent {

    private static final String TAG = EditorContent.class.getSimpleName();
    private static FileHandle rootPath = Gdx.files.absolute("../sprites");

    public static String selectedMetadataKey = rootPath.path();

    //
    // TODO(brian): add categories to filter what's shown in particular node props
    //

    public record Metadata(
        String key,
        String path,
        String filename,
        String extension
    ) {
        public Metadata(FileHandle fileHandle) {
            this(
                fileHandle.pathWithoutExtension().substring(rootPath.path().length() + 1),
                fileHandle.path(),
                fileHandle.name(),
                fileHandle.extension()
            );
        }
        private static String key(FileHandle fileHandle) {
            return fileHandle.path().substring(rootPath.path().length());
        }
    }

    public static Map<String, Metadata> metadata = new HashMap<>();

    public static List<String> metadataKeys() {
        return metadata.keySet().stream().sorted().toList();
    }

    public static FileHandle rootPath() {
        return rootPath;
    }

    public static void rootPath(Path newRootPath) {
        var path = newRootPath.toAbsolutePath().toString();
        Util.log(TAG, "Setting content root path: %s".formatted(path));

        EditorContent.rootPath = Gdx.files.absolute(path);
        refresh();
    }

    public static void refresh() {
        Util.log(TAG, "Refreshing content root path: %s".formatted(rootPath));

        if (rootPath.exists() && rootPath.isDirectory()) {
            Util.log(TAG, "Content root path exists and is a directory, scanning for files...");
        } else {
            Util.log(TAG, "Content root path does not exist: %s".formatted(rootPath));
            return;
        }

        // recursively scan for files in path and convert to Metadata
        var newMetadata = Arrays.stream(rootPath.list())
            .flatMap(EditorContent::recurseDirectories)
            .filter(EditorContent::isFile)
            .map(Metadata::new)
            .peek(EditorContent::logMetadata)
            .collect(Collectors.toMap(Metadata::key, Function.identity(), (a, b) -> a));

        metadata.clear();
        metadata.putAll(newMetadata);

        Util.log(TAG, "Found %d files in content root path".formatted(metadata.size()));

        selectedMetadataKey = metadata.keySet().stream().findFirst().orElse(rootPath.path());
    }

    private static Stream<FileHandle> recurseDirectories(FileHandle fileHandle) {
        return fileHandle.isDirectory() ? Arrays.stream(fileHandle.list()) : Stream.of(fileHandle);
    }

    private static boolean isFile(FileHandle fileHandle) {
        return !fileHandle.isDirectory();
    }

    private static void logMetadata(Metadata metadata) {
        Util.log(TAG, "- %s -> %s".formatted(metadata.key, metadata.path));
    }
}
