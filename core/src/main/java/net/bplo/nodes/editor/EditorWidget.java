package net.bplo.nodes.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import net.bplo.nodes.imgui.FontAwesomeIcons;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Paths;

public class EditorWidget {

    private static final String TAG = EditorWidget.class.getSimpleName();

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

    static void renderShowIdsToggle(Editor editor) {
        ImGui.pushFont(EditorUtil.Fonts.icons);

        var icon = editor.nodePane.showIds ? FontAwesomeIcons.eye : FontAwesomeIcons.eyeSlash;
        var text = editor.nodePane.showIds ? " IDs Visible" : " IDs Hidden";
        var label = icon + text;
        if (ImGui.checkbox(label, editor.nodePane.showIds)) {
            editor.nodePane.showIds = !editor.nodePane.showIds;
        }

        ImGui.popFont();
    }

    static void renderShowThumbnailsToggle(Editor editor) {
        ImGui.pushFont(EditorUtil.Fonts.icons);

        var icon = editor.nodePane.showThumbnails ? FontAwesomeIcons.eye : FontAwesomeIcons.eyeSlash;
        var text = editor.nodePane.showThumbnails ? " Thumbnails Visible" : " Thumbnails Hidden";
        var label = icon + text;
        if (ImGui.checkbox(label, editor.nodePane.showThumbnails)) {
            editor.nodePane.showThumbnails = !editor.nodePane.showThumbnails;

            // TODO(brian): should the current global setting apply to new nodes/thumbnails too?
            editor.nodes.stream()
                .flatMap(node -> node.props.stream())
                .filter(PropThumbnail.class::isInstance)
                .map(PropThumbnail.class::cast)
                .forEach(thumbnail -> thumbnail.thumbnailVisible = editor.nodePane.showThumbnails);
        }

        ImGui.popFont();
    }

    static void renderLoadMetadataButton(Editor editor) {
        var isMetadataLoaded = (editor.metadata != null);
        var buttonColor        = isMetadataLoaded ? ImGuiColors.lime.asInt()       : ImGuiColors.orange.asInt();
        var buttonActiveColor  = isMetadataLoaded ? ImGuiColors.chartreuse.asInt() : ImGuiColors.gold.asInt();
        var buttonHoveredColor = isMetadataLoaded ? ImGuiColors.forest.asInt()     : ImGuiColors.darkYellow.asInt();
        var buttonIcon = isMetadataLoaded ? FontAwesomeIcons.fileCircleCheck : FontAwesomeIcons.fileImport;
        var buttonText = isMetadataLoaded ? "Metadata Loaded" : "Load Metadata";

        ImGui.pushFont(EditorUtil.Fonts.icons);
        ImGui.pushStyleColor(ImGuiCol.Button, buttonColor);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, buttonActiveColor);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, buttonHoveredColor);

        if (ImGui.button(buttonIcon + " " + buttonText)) {
            var fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Metadata File");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files", "json"));
            // TODO(brian): persist and restore last directory
            fileChooser.setCurrentDirectory(Paths.get(".").toAbsolutePath().toFile());

            if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
                var selectedFile = fileChooser.getSelectedFile();
                var filePath = selectedFile.toPath().toAbsolutePath().toString();
                editor.loadMetadata(filePath);
            }
        }

        ImGui.popStyleColor(3);
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

    static void renderObjectId(EditorObject object) {
        // collect extra info, based on the object type, to render the id
        record Info(boolean isNode, long nodeId, ImGuiWidgetBounds bounds, ImGuiColors.Value color) {}
        var info = switch (object) {
            case Node node -> new Info(true, node.id, node.bounds.node, ImGuiColors.cyan);
            case Prop prop -> new Info(false, prop.node.id, prop.bounds, ImGuiColors.yellow);
            case Pin  pin  -> null; // TODO(brian): need to get pin bounds
            case Link link -> null; // TODO(brian): links are a special case, need to determine how to render ids for them
            default -> null;
        };

        // NOTE: for now, skip objects that we don't have all the info for
        if (info == null || info.bounds == null) {
            return;
        }

        // the node id is used to get the draw list, regardless of the object type
        var drawList = NodeEditor.getNodeBackgroundDrawList(info.nodeId);
        var textSize = ImGui.calcTextSize(String.valueOf(object.id));
        var pad  = 5f;
        var gap  = 1f;
        var height = 20f;
        var width = Math.max(textSize.x + 2 * pad, height);

        var xMin = info.bounds.min().x - width - pad - gap;
        var yMin = info.bounds.min().y - (info.isNode ? (height + pad + gap) : 0);
        var xMax = xMin + width  + pad;
        var yMax = yMin + height + pad;

        // background, border, node id
        drawList.addRectFilled(xMin, yMin, xMax, yMax, ImGuiColors.darkerGray.asInt(), 2f);
        drawList.addRect(xMin, yMin, xMax, yMax, ImGuiColors.medGray.asInt(), 2f, 0, 1f);

        drawList.addText(
            xMin + ((xMax - xMin) - textSize.x) / 2f,
            yMin + ((yMax - yMin) - textSize.y) / 2f,
            info.color.asInt(), String.valueOf(object.id));
    }

    public record Image(int id, ImVec2 size, ImVec2 uv1, ImVec2 uv2) {
        public static Image from(TextureRegion region) {
            return new Image(
                region.getTexture().getTextureObjectHandle(),
                new ImVec2(region.getRegionWidth(), region.getRegionHeight()),
                new ImVec2(region.getU(), region.getV()),
                new ImVec2(region.getU2(), region.getV2())
            );
        }

        public static Image from(TextureRegion region, float width, float height) {
            return new Image(
                region.getTexture().getTextureObjectHandle(),
                new ImVec2(width, height),
                new ImVec2(region.getU(), region.getV()),
                new ImVec2(region.getU2(), region.getV2())
            );
        }

        public static Image from(Texture texture) {
            return new Image(
                texture.getTextureObjectHandle(),
                new ImVec2(texture.getWidth(), texture.getHeight()),
                new ImVec2(0, 0),
                new ImVec2(1, 1)
            );
        }

        public static Image from(Texture texture, float width, float height) {
            return new Image(
                texture.getTextureObjectHandle(),
                new ImVec2(width, height),
                new ImVec2(0, 0),
                new ImVec2(1, 1)
            );
        }
    }
}
