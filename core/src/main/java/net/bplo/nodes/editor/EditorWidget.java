package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;
import net.bplo.nodes.imgui.FontAwesomeIcons;

import javax.swing.*;

public class EditorWidget {

    static void renderSaveButton(Editor editor) {
        ImGui.pushFont(EditorUtil.Fonts.icons);

        if (ImGui.button(FontAwesomeIcons.floppyDisk + " Save")) {
            editor.save();
        }

        ImGui.popFont();
    }

    static void renderLoadButton(Editor editor) {
        ImGui.pushFont(EditorUtil.Fonts.icons);

        if (ImGui.button(FontAwesomeIcons.folderOpen + " Load")) {
            editor.load();
        }

        ImGui.popFont();
    }

    static void renderContentCombo(Editor editor) {
        ImGui.pushFont(EditorUtil.Fonts.icons);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 4f);
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, 20f, 3f);
        ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarSize, 20f);

        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        if (ImGui.beginCombo("##content-combo", EditorContent.selectedMetadataKey)) {
            for (var key : EditorContent.metadataKeys()) {
                var label = FontAwesomeIcons.key + " " + key;
                if (ImGui.selectable(label)) {
                    EditorContent.selectedMetadataKey = key;
                }
            }
            ImGui.endCombo();
        }

        ImGui.popStyleVar(3);
        ImGui.popFont();
    }

    static void renderSetContentPathButton(Editor editor) {
        ImGui.pushFont(EditorUtil.Fonts.icons);

        if (ImGui.button(FontAwesomeIcons.folderPlus + " Set Content Path")) {
            var fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select New Content Path");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
                var selectedFile = fileChooser.getSelectedFile();
                EditorContent.rootPath(selectedFile.toPath());
            }
        }

        ImGui.popFont();
    }
}
