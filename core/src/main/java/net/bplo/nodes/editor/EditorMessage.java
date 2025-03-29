package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec2;
import net.bplo.nodes.imgui.ImGuiColors;

import java.util.Map;

public class EditorMessage {

    public enum Type { INFO, WARNING, ERROR, ACCEPT }

    private record MessageColors(int background, int border, int text) {
        public MessageColors(ImGuiColors.Value background, ImGuiColors.Value border, ImGuiColors.Value text) {
            this(background.asInt(), border.asInt(), text.asInt());
        }
    }

    private static final Map<Type, MessageColors> colorsByType = Map.of(
        Type.INFO,    new MessageColors(ImGuiColors.medBlue,    ImGuiColors.darkBlue,   ImGuiColors.darkBlue),
        Type.WARNING, new MessageColors(ImGuiColors.medYellow,  ImGuiColors.darkYellow, ImGuiColors.darkYellow),
        Type.ERROR,   new MessageColors(ImGuiColors.medRed,     ImGuiColors.darkRed,    ImGuiColors.darkRed),
        Type.ACCEPT,  new MessageColors(ImGuiColors.lightGreen, ImGuiColors.medGreen,   ImGuiColors.darkGreen)
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

        var font = EditorUtil.Fonts.icons;
        ImGui.pushFont(font);
        {
            var msgColors = colorsByType.get(type);
            var textSize = ImGui.calcTextSize(text);
            var rounding = 10f;
            var padding = new ImVec2(15, 10);
            var spacing = new ImVec2(4, 4);
            var rectMin = new ImVec2(
                cursorPos.x - padding.x,
                cursorPos.y - padding.y);
            var rectMax = new ImVec2(
                cursorPos.x + textSize.x + padding.x,
                cursorPos.y + textSize.y + padding.y);

            // draw the message background
            ImGui.setCursorPos(
                cursorPos.x - spacing.x,
                cursorPos.y - spacing.y);
            draw.addRectFilled(rectMin, rectMax, msgColors.background, rounding);
            draw.addRect      (rectMin, rectMax, msgColors.border,     rounding, 4f);

            // draw the message text
            ImGui.setCursorPos(cursorPos);
            ImGui.textColored(msgColors.text, text);
        }
        ImGui.popFont();
    }
}
