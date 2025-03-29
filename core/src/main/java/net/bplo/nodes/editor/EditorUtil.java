package net.bplo.nodes.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImColor;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import net.bplo.nodes.assets.Assets;
import net.bplo.nodes.imgui.ImGuiPlatform;
import net.bplo.nodes.imgui.ImGuiUtil;

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
    // TODO(brian): move into ImGuiUtil in a Layout inner class

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

    public static class Images {
        public static Image nodeHeader;

        public static void init(Assets assets) {
            var region = assets.atlas().findRegion("noise-bg");
            nodeHeader = Image.from(region);
        }
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

    public static class Colors {
        public static final ImVec4 dim = new ImVec4(0.5f, 0.5f, 0.5f, 1f);
        public static final ImVec4 linkPath = new ImVec4(0f, 1f, 0f, 1f);
        public static final ImVec4 createLink = new ImVec4(0.24f, 0.6f, 1f, 0.8f);
        public static final ImVec4 rejectLink = new ImVec4(1f, 0f, 0f, 0.8f);
        public static final ImVec4 acceptLink = new ImVec4(0f, 1f, 0f, 0.8f);
        public static final ImVec4 newNodeHeader = new ImVec4(0, 1f, 1f, 1f);
        public static final int rejectLabelBackground = ImColor.rgba(45, 32, 32, 255);
        public static final int acceptLabelBackground = ImColor.rgba(32, 45, 32, 255);

        //
        // LibGDX Color values converted to int for use in ImGui functions
        //
        // TODO(brian): make utility class to encapsulate the different color value types; hex-str, int, ImVec4
        //  and include a method to spit out a variant with different alpha on demand

        public static final int white       = ImColor.rgba("#ffffffff");
        public static final int lightGray   = ImColor.rgba("#bfbfbfff");
        public static final int gray        = ImColor.rgba("#7f7f7fff");
        public static final int darkGray    = ImColor.rgba("#2f2f2fff");
        public static final int black       = ImColor.rgba("#000000ff");

        public static final int blue        = ImColor.rgba("#0000ffff");
        public static final int navy        = ImColor.rgba("#00007fff");
        public static final int royal       = ImColor.rgba("#4169e1ff");
        public static final int slate       = ImColor.rgba("#708090ff");
        public static final int sky         = ImColor.rgba("#87ceebff");
        public static final int cyan        = ImColor.rgba("#00ffffff");
        public static final int teal        = ImColor.rgba("#007f7fff");

        public static final int green       = ImColor.rgba("#00ff00ff");
        public static final int chartreuse  = ImColor.rgba("#7fff00ff");
        public static final int lime        = ImColor.rgba("#32cd32ff");
        public static final int forest      = ImColor.rgba("#228b22ff");
        public static final int olive       = ImColor.rgba("#6b8e23ff");

        public static final int yellow      = ImColor.rgba("#ffff00ff");
        public static final int gold        = ImColor.rgba("#ffd700ff");
        public static final int goldenrod   = ImColor.rgba("#daa520ff");
        public static final int orange      = ImColor.rgba("#ffa500ff");

        public static final int brown       = ImColor.rgba("#8b4513ff");
        public static final int tan         = ImColor.rgba("#d2b48cff");
        public static final int firebrick   = ImColor.rgba("#b22222ff");

        public static final int red         = ImColor.rgba("#ff0000ff");
        public static final int scarlet     = ImColor.rgba("#ff341cff");
        public static final int coral       = ImColor.rgba("#ff7f50ff");
        public static final int salmon      = ImColor.rgba("#fa8072ff");
        public static final int pink        = ImColor.rgba("#ff69b4ff");
        public static final int magenta     = ImColor.rgba("#ff00ffff");

        public static final int purple      = ImColor.rgba("#a020f0ff");
        public static final int violet      = ImColor.rgba("#ee82eeff");
        public static final int maroon      = ImColor.rgba("#b03060ff");

        public static final int lightGrey   = ImColor.rgba("#f5f5f5ff");
        public static final int medGrey     = ImColor.rgba("#d9d9d9ff");
        public static final int darkGrey    = ImColor.rgba("#595959ff");

        public static final int lightBlue   = ImColor.rgba("#b6c7ffff");
        public static final int medBlue     = ImColor.rgba("#91d5ffff");
        public static final int darkBlue    = ImColor.rgba("#0050b3ff");

        public static final int lightYellow = ImColor.rgba("#ffdbb6ff");
        public static final int medYellow   = ImColor.rgba("#ffe58fff");
        public static final int darkYellow  = ImColor.rgba("#d48806ff");

        public static final int lightRed    = ImColor.rgba("#ffd1d0ff");
        public static final int medRed      = ImColor.rgba("#ffa39eff");
        public static final int darkRed     = ImColor.rgba("#cf1322ff");


        /**
         * "Convenience" class to hold the ImVec4 equivalents of the color values above.
         * mild bummer that the ImGui/NodeEditor java bindings have functions that accept
         * one or the other, but not always both
         */
        public static class Vec4 {

            public static final ImVec4 white       = ImGuiUtil.toImVec4(Colors.white       );
            public static final ImVec4 lightGray   = ImGuiUtil.toImVec4(Colors.lightGray   );
            public static final ImVec4 gray        = ImGuiUtil.toImVec4(Colors.gray        );
            public static final ImVec4 darkGray    = ImGuiUtil.toImVec4(Colors.darkGray    );
            public static final ImVec4 black       = ImGuiUtil.toImVec4(Colors.black       );

            public static final ImVec4 blue        = ImGuiUtil.toImVec4(Colors.blue        );
            public static final ImVec4 navy        = ImGuiUtil.toImVec4(Colors.navy        );
            public static final ImVec4 royal       = ImGuiUtil.toImVec4(Colors.royal       );
            public static final ImVec4 slate       = ImGuiUtil.toImVec4(Colors.slate       );
            public static final ImVec4 sky         = ImGuiUtil.toImVec4(Colors.sky         );
            public static final ImVec4 cyan        = ImGuiUtil.toImVec4(Colors.cyan        );
            public static final ImVec4 teal        = ImGuiUtil.toImVec4(Colors.teal        );

            public static final ImVec4 green       = ImGuiUtil.toImVec4(Colors.green       );
            public static final ImVec4 chartreuse  = ImGuiUtil.toImVec4(Colors.chartreuse  );
            public static final ImVec4 lime        = ImGuiUtil.toImVec4(Colors.lime        );
            public static final ImVec4 forest      = ImGuiUtil.toImVec4(Colors.forest      );
            public static final ImVec4 olive       = ImGuiUtil.toImVec4(Colors.olive       );

            public static final ImVec4 yellow      = ImGuiUtil.toImVec4(Colors.yellow      );
            public static final ImVec4 gold        = ImGuiUtil.toImVec4(Colors.gold        );
            public static final ImVec4 goldenrod   = ImGuiUtil.toImVec4(Colors.goldenrod   );
            public static final ImVec4 orange      = ImGuiUtil.toImVec4(Colors.orange      );

            public static final ImVec4 brown       = ImGuiUtil.toImVec4(Colors.brown       );
            public static final ImVec4 tan         = ImGuiUtil.toImVec4(Colors.tan         );
            public static final ImVec4 firebrick   = ImGuiUtil.toImVec4(Colors.firebrick   );

            public static final ImVec4 red         = ImGuiUtil.toImVec4(Colors.red         );
            public static final ImVec4 scarlet     = ImGuiUtil.toImVec4(Colors.scarlet     );
            public static final ImVec4 coral       = ImGuiUtil.toImVec4(Colors.coral       );
            public static final ImVec4 salmon      = ImGuiUtil.toImVec4(Colors.salmon      );
            public static final ImVec4 pink        = ImGuiUtil.toImVec4(Colors.pink        );
            public static final ImVec4 magenta     = ImGuiUtil.toImVec4(Colors.magenta     );

            public static final ImVec4 purple      = ImGuiUtil.toImVec4(Colors.purple      );
            public static final ImVec4 violet      = ImGuiUtil.toImVec4(Colors.violet      );
            public static final ImVec4 maroon      = ImGuiUtil.toImVec4(Colors.maroon      );

            public static final ImVec4 lightGrey   = ImGuiUtil.toImVec4(Colors.lightGrey   );
            public static final ImVec4 medGrey     = ImGuiUtil.toImVec4(Colors.medGrey     );
            public static final ImVec4 darkGrey    = ImGuiUtil.toImVec4(Colors.darkGrey    );

            public static final ImVec4 lightBlue   = ImGuiUtil.toImVec4(Colors.lightBlue   );
            public static final ImVec4 medBlue     = ImGuiUtil.toImVec4(Colors.medBlue     );
            public static final ImVec4 darkBlue    = ImGuiUtil.toImVec4(Colors.darkBlue    );

            public static final ImVec4 lightYellow = ImGuiUtil.toImVec4(Colors.lightYellow );
            public static final ImVec4 medYellow   = ImGuiUtil.toImVec4(Colors.medYellow   );
            public static final ImVec4 darkYellow  = ImGuiUtil.toImVec4(Colors.darkYellow  );

            public static final ImVec4 lightRed    = ImGuiUtil.toImVec4(Colors.lightRed    );
            public static final ImVec4 medRed      = ImGuiUtil.toImVec4(Colors.medRed      );
            public static final ImVec4 darkRed     = ImGuiUtil.toImVec4(Colors.darkRed     );
        }
    }
}
