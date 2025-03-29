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

    public static void beginColumn() {
        ImGui.beginGroup();
    }

    public static void nextColumn() {
        nextColumn(null);
    }

    public static void nextColumn(Float width) {
        ImGui.endGroup();
        ImGui.sameLine();
        if (width != null) ImGui.setNextItemWidth(width);
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

        public static final int white       = ImColor.rgb("#ffffff");
        public static final int lightGray   = ImColor.rgb("#bfbfbf");
        public static final int gray        = ImColor.rgb("#7f7f7f");
        public static final int darkGray    = ImColor.rgb("#2f2f2f");
        public static final int black       = ImColor.rgb("#000000");

        public static final int blue        = ImColor.rgb("#0000ff");
        public static final int navy        = ImColor.rgb("#00007f");
        public static final int royal       = ImColor.rgb("#4169e1");
        public static final int slate       = ImColor.rgb("#708090");
        public static final int sky         = ImColor.rgb("#87ceeb");
        public static final int cyan        = ImColor.rgb("#00ffff");
        public static final int teal        = ImColor.rgb("#007f7f");

        public static final int green       = ImColor.rgb("#00ff00");
        public static final int chartreuse  = ImColor.rgb("#7fff00");
        public static final int lime        = ImColor.rgb("#32cd32");
        public static final int forest      = ImColor.rgb("#228b22");
        public static final int olive       = ImColor.rgb("#6b8e23");

        public static final int yellow      = ImColor.rgb("#ffff00");
        public static final int gold        = ImColor.rgb("#ffd700");
        public static final int goldenrod   = ImColor.rgb("#daa520");
        public static final int orange      = ImColor.rgb("#ffa500");

        public static final int brown       = ImColor.rgb("#8b4513");
        public static final int tan         = ImColor.rgb("#d2b48c");
        public static final int firebrick   = ImColor.rgb("#b22222");

        public static final int red         = ImColor.rgb("#ff0000");
        public static final int scarlet     = ImColor.rgb("#ff341c");
        public static final int coral       = ImColor.rgb("#ff7f50");
        public static final int salmon      = ImColor.rgb("#fa8072");
        public static final int pink        = ImColor.rgb("#ff69b4");
        public static final int magenta     = ImColor.rgb("#ff00ff");

        public static final int purple      = ImColor.rgb("#a020f0");
        public static final int violet      = ImColor.rgb("#ee82ee");
        public static final int maroon      = ImColor.rgb("#b03060");

        public static final int lightGrey   = ImColor.rgb("#f5f5f5");
        public static final int medGrey     = ImColor.rgb("#d9d9d9");
        public static final int darkGrey    = ImColor.rgb("#595959");

        public static final int lightBlue   = ImColor.rgb("#e6f7ff");
        public static final int medBlue     = ImColor.rgb("#91d5ff");
        public static final int darkBlue    = ImColor.rgb("#0050b3");

        public static final int lightYellow = ImColor.rgb("#fffbe6");
        public static final int medYellow   = ImColor.rgb("#ffe58f");
        public static final int darkYellow  = ImColor.rgb("#d48806");

        public static final int lightRed    = ImColor.rgb("#fff1f0");
        public static final int medRed      = ImColor.rgb("#ffa39e");
        public static final int darkRed     = ImColor.rgb("#cf1322");


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
