package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.bplo.nodes.imgui.FontAwesomeIcons;
import net.bplo.nodes.imgui.ImGuiColors;

public abstract class EditorObject {

    public enum Type { NODE, PIN, LINK, PROP }

    // TODO(brian): (temp?) workaround for Editor usage from an EditorObject context
    public static Editor editor;

    private static long NEXT_ID = 1L;

    public final Type objectType;
    public final long id;

    public EditorObject(Type objectType) {
        this.objectType = objectType;
        this.id = NEXT_ID++;
    }

    /**
     * Limited access constructor intended for use by {@link EditorSerializer}
     * to create {@link EditorObject} instances from saved json data.
     */
    EditorObject(Type objectType, long savedId) {
        this.objectType = objectType;
        this.id = savedId;
    }

    /**
     * Limited access method to update the {@link #NEXT_ID} value
     * after loading {@link EditorSerializer.NodeList} data from json.
     * Ensures that newly created {@link EditorObject} instances aren't
     * assigned ids that are already in use from the loaded node graph.
     */
    static void updateNextIdAfterLoad(long maxExistingId) {
        NEXT_ID = maxExistingId + 1;
    }

    public String label() {
        return "%s-%d".formatted(objectType.name().toLowerCase(), id);
    }

    public abstract void render();

    public void renderAfterNode() {}
    public void renderContextMenu(Editor editor) {}

    //
    // Convenience methods for consistent rendering of context menu elements
    //

    protected void headerText(String text) {
        headerText(text, ImGuiColors.cyan);
    }

    protected void headerText(String text, ImGuiColors.Value color) {
        ImGui.pushFont(EditorUtil.Fonts.nodeHeader);
        ImGui.textColored(color.asInt(), text);
        ImGui.popFont();
    }

    protected void separatorText(String label) {
        var color = ImGuiColors.medGray;

        ImGui.pushStyleColor(ImGuiCol.Text, color.asInt());
        ImGui.pushFont(EditorUtil.Fonts.small);

        ImGui.dummy(0, 2);
        ImGui.separatorText(label);

        ImGui.popFont();
        ImGui.popStyleColor();
    }

    protected void deleteButton(Editor editor) {
        var availableWidth = ImGui.getContentRegionAvailX();
        var buttonText = "%s Delete".formatted(FontAwesomeIcons.trash);
        ImGui.pushFont(EditorUtil.Fonts.icons);
        if (ImGui.button(buttonText, availableWidth, 0)) {
            editor.remove(this);
            ImGui.closeCurrentPopup();
        }
        ImGui.popFont();
    }
}
