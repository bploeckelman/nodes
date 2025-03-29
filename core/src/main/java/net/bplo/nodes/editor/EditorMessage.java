package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec2;

import java.util.Map;

import static net.bplo.nodes.editor.EditorUtil.Colors.*;

public class EditorMessage {

    public enum Type { INFO, WARNING, ERROR }

    private record MessageColors(int background, int border, int text) {}

    private static final Map<Type, MessageColors> colorsByType = Map.of(
        Type.INFO,    new MessageColors(lightBlue,   medBlue,   darkBlue),
        Type.WARNING, new MessageColors(lightYellow, medYellow, darkYellow),
        Type.ERROR,   new MessageColors(lightRed,    medRed,    darkRed)
    );

    public static void show(String text) {
        show(Type.INFO, text);
    }

    /**
     * Show informational messages in a small ui element, used for right-click context menu labels
     * to display temporary messages about the the current user action and it's validity
     */
    public static void show(Type type, String text) {
        ImGui.setCursorPosY(ImGui.getCursorPosY() - ImGui.getTextLineHeight());
        var cursorPos = ImGui.getCursorPos();
        var draw = ImGui.getWindowDrawList();

        var font = EditorUtil.Fonts.small;
        ImGui.pushFont(font);
        {
            var msgColors = colorsByType.get(type);
            var textSize = ImGui.calcTextSize(text);
            var rounding = 10f;
            var padding = new ImVec2(4, 4);
            var spacing = new ImVec2(4, 4);
            var rectMin = new ImVec2(
                cursorPos.x - padding.x,
                cursorPos.y - padding.y);
            var rectMax = new ImVec2(
                cursorPos.x + textSize.x + padding.x,
                cursorPos.y + textSize.y + padding.y);

            // draw the message background
            ImGui.setCursorPos(cursorPos.x - spacing.x, cursorPos.y - spacing.y);
            draw.addRectFilled(rectMin, rectMax, msgColors.background, rounding);
            draw.addRect      (rectMin, rectMax, msgColors.border,     rounding);

            // draw the message text
            ImGui.setCursorPos(cursorPos);
            ImGui.textColored(msgColors.text, text);
        }
        ImGui.popFont();
    }
}
