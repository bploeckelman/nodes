package net.bplo.nodes.imgui;

import imgui.ImGui;

/**
 * Utility methods used as a table layout workaround because the table api
 * uses {@code begin/endChild()} which node-editor doesn't play nice with
 * <br>
 * see: <a href="https://github.com/ocornut/imgui/blob/master/imgui_tables.cpp#L39">imgui_tables.cpp:39</a>
 */
public class ImGuiLayout {

    public static void beginColumn() {
        beginColumn(null);
    }

    public static void beginColumn(Float width) {
        if (width != null) {
            ImGui.setNextItemWidth(width);
        }
        ImGui.beginGroup();
    }

    public static void nextColumn() {
        nextColumn(null);
    }

    public static void nextColumn(Float width) {
        ImGui.endGroup();
        ImGui.sameLine();
        beginColumn(width);
    }

    public static void endColumn() {
        ImGui.endGroup();
    }
}
