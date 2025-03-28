package net.bplo.nodes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImColor;
import imgui.ImFont;
import imgui.ImVec2;
import imgui.ImVec4;
import net.bplo.nodes.imgui.ImGuiPlatform;

public class EditorUtil {

    public static void init(ImGuiPlatform imgui, Assets assets) {
        Fonts.init(imgui);
        Images.init(assets);
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

    public static class Colors {
        public static final ImVec4 dim = new ImVec4(0.5f, 0.5f, 0.5f, 1f);
        public static final ImVec4 createLink = new ImVec4(0.24f, 0.6f, 1f, 0.8f);
        public static final ImVec4 rejectLink = new ImVec4(1f, 0f, 0f, 0.8f);
        public static final ImVec4 acceptLink = new ImVec4(0f, 1f, 0f, 0.8f);
        public static final ImVec4 newNodeHeader = new ImVec4(0, 1f, 1f, 1f);
        public static final int rejectLabelBackground = ImColor.rgba(45, 32, 32, 255);
        public static final int acceptLabelBackground = ImColor.rgba(32, 45, 32, 255);
    }

    public static class Fonts {
        public static ImFont nodeHeader;
        public static ImFont nodeContent;
        public static ImFont small;

        public static void init(ImGuiPlatform imgui) {
            nodeHeader = imgui.getFont("noto-sans-cjk-jp-medium.otf");
            nodeContent = imgui.getFont("play-regular.ttf");
            small = imgui.getFont("tahoma.ttf");
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
