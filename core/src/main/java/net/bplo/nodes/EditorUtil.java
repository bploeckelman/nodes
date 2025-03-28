package net.bplo.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImColor;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import net.bplo.nodes.imgui.ImGuiPlatform;

public class EditorUtil {

    public static ImGuiPlatform imgui;

    public static void init(ImGuiPlatform imgui, Assets assets) {
        EditorUtil.imgui = imgui;
        Fonts.init(imgui);
        Images.init(assets);
    }

    // NOTE(brian): these 'begin/next/endColumn()' methods are a workaround for tables...
    //  table api uses 'begin/endChild()' which node-editor doesn't play nice with
    //  see: https://github.com/ocornut/imgui/blob/master/imgui_tables.cpp#L39
    //  interestingly, it achieves the same thing with simpler layout code ¯\_(ツ)_/¯

    public static void beginColumn() {
        ImGui.beginGroup();
    }

    public static void nextColumn() {
        ImGui.endGroup();
        ImGui.sameLine();
        ImGui.beginGroup();
    }

    public static void endColumn() {
        ImGui.endGroup();
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

    /**
     * Show some text with a colored background behind it, used for right-click context menu labels
     * for displaying temporary messages to the user about whether the current action is valid
     * or invalid, and why.
     */
    public static void showMessage(String text, int backgroundColor) {
        ImGui.setCursorPosY(ImGui.getCursorPosY() - ImGui.getTextLineHeight());

        var scale = 1.5f;
        var textSize = ImGui.calcTextSize(text);
        var size = new ImVec2(textSize.x * scale, textSize.y * scale);
        var padding = ImGui.getStyle().getFramePadding();
        var spacing = ImGui.getStyle().getItemSpacing();
        ImGui.setCursorPos(
            ImGui.getCursorPosX() + spacing.x,
            ImGui.getCursorPosY() - spacing.y);

        var rectMin = new ImVec2(
            ImGui.getCursorScreenPosX() - padding.x,
            ImGui.getCursorScreenPosY() - padding.y);
        var rectMax = new ImVec2(
            ImGui.getCursorScreenPosX() + size.x + padding.x,
            ImGui.getCursorScreenPosY() + size.y + padding.y);

        var font = Fonts.small;
        var drawList = ImGui.getWindowDrawList();
        var fontSize = (int) Math.floor(font.getFontSize() * scale);
        drawList.addRectFilled(rectMin, rectMax, backgroundColor, size.y * 0.25f);
        drawList.addText(
            font, fontSize,
            rectMin.x + padding.x,
            rectMin.y + padding.y,
            Colors.white, text);
    }

    public static class Colors {
        public static final ImVec4 dim = new ImVec4(0.5f, 0.5f, 0.5f, 1f);
        public static final ImVec4 linkPath = new ImVec4(0f, 1f, 0f, 1f);
        public static final ImVec4 createLink = new ImVec4(0.24f, 0.6f, 1f, 0.8f);
        public static final ImVec4 rejectLink = new ImVec4(1f, 0f, 0f, 0.8f);
        public static final ImVec4 acceptLink = new ImVec4(0f, 1f, 0f, 0.8f);
        public static final ImVec4 newNodeHeader = new ImVec4(0, 1f, 1f, 1f);
        public static final int rejectLabelBackground = ImColor.rgba(45, 32, 32, 255);
        public static final int acceptLabelBackground = ImColor.rgba(32, 45, 32, 255);
        public static final int white = ImColor.rgb(255, 255, 255);
    }

    public static class Fonts {
        public static ImFont nodeHeader;
        public static ImFont nodeContent;
        public static ImFont small;
        public static ImFont icons;

        public static void init(ImGuiPlatform imgui) {
            nodeHeader = imgui.getFont("noto-sans-cjk-jp-medium.otf");
            nodeContent = imgui.getFont("play-regular.ttf");
            small = imgui.getFont("tahoma.ttf");
            icons = imgui.getFont("fa-solid-900-v6.ttf");
        }
    }

    public static class Images {
        public static Image nodeHeader;

        public static void init(Assets assets) {
            var region = assets.atlas().findRegion("noise-bg");
            nodeHeader = Image.from(region);
        }
    }
}
