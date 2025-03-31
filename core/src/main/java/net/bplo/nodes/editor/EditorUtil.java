package net.bplo.nodes.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import imgui.ImFont;
import imgui.ImVec2;
import net.bplo.nodes.assets.Assets;
import net.bplo.nodes.imgui.ImGuiPlatform;

public class EditorUtil {

    public static void init(ImGuiPlatform imgui, Assets assets) {
        Fonts.init(imgui);
        Images.init(assets);
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
            nodeHeader  = imgui.getFont("noto-sans-cjk-jp-medium.otf");
            nodeContent = imgui.getFont("play-regular.ttf");
            small       = imgui.getFont("tahoma.ttf");
            icons       = imgui.getFont("fa-solid-900-v6.ttf");
        }
    }

    /**
     * Convenience structure for data used by ImGui texture rendering functions.
     */
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
