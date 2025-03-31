package net.bplo.nodes.editor;

import net.bplo.nodes.Main;
import net.bplo.nodes.assets.Assets;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Paths;

public class EditorFileDialog {

    private static final String PREFS_KEY = "editor.last-directory";
    private static final FileFilter JSON_FILTER = new FileNameExtensionFilter("JSON Files", "json");

    private enum Type { SAVE, LOAD }

    public static File openSaveFile() {
        return open(Type.SAVE);
    }

    public static File openLoadFile() {
        return open(Type.LOAD);
    }

    private static File open(Type type) {
        var title = (type == Type.SAVE) ? "Save File" : "Load File";
        var prefs = Main.app.assets.prefs;
        var lastDir = prefs.getString(PREFS_KEY);

        var fileChooser = new JFileChooser(lastDir);
        fileChooser.setFileFilter(JSON_FILTER);
        fileChooser.setDialogTitle(title);

        var result = (type == Type.SAVE)
            ? fileChooser.showSaveDialog(null)
            : fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            lastDir = fileChooser.getCurrentDirectory().toString();
            prefs.putString(PREFS_KEY, lastDir).flush();

            var selectedFile = fileChooser.getSelectedFile();
            return Paths.get(selectedFile.getAbsolutePath()).toFile();
        }

        return null;
    }
}
