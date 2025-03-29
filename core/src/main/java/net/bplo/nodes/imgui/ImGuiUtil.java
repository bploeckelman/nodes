package net.bplo.nodes.imgui;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;

public class ImGuiUtil {

    private static final ImVec2 vec2 = new ImVec2();

    public static void drawTextCentered(String text, int color, ImDrawList draw, ImGuiWidgetBounds bounds) {
        var textSize = ImGui.calcTextSize(text);
        var pos = vec2.set(
            bounds.min().x + (bounds.size().x - textSize.x) / 2f,
            bounds.min().y + (bounds.size().y - textSize.y) / 2f);
        draw.addText(pos, color, text);
    }
}
