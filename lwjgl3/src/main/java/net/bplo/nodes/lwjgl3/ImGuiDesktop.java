package net.bplo.nodes.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.GdxRuntimeException;
import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.bplo.nodes.imgui.ImGuiPlatform;

import java.util.HashMap;
import java.util.Map;

public class ImGuiDesktop implements ImGuiPlatform {

    public final Map<String, ImFont> fonts = new HashMap<>();

    public long window;
    public ImGuiImplGl3 imGuiGl3;
    public ImGuiImplGlfw imGuiGlfw;

    private InputProcessor imGuiInputProcessor;

    @Override
    public void init() {
        if (Gdx.graphics instanceof Lwjgl3Graphics lwjgl3Graphics) {
            imGuiGl3 = new ImGuiImplGl3();
            imGuiGlfw = new ImGuiImplGlfw();

            window = lwjgl3Graphics.getWindow().getWindowHandle();
            if (window == 0) {
                throw new GdxRuntimeException("Failed to create the GLFW window");
            }

            ImGui.createContext();
            ImGui.getIO().setIniFilename(null);

            initDocking();
            initFonts();

            imGuiGl3.init("#version 150");
            imGuiGlfw.init(window, true);
        } else {
            throw new GdxRuntimeException("This ImGui platform requires lwjgl3");
        }
    }

    @Override
    public void startFrame() {
        // restore the input processor after ImGui caught all inputs
        if (imGuiInputProcessor != null) {
            Gdx.input.setInputProcessor(imGuiInputProcessor);
            imGuiInputProcessor = null;
        }

        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();

        ImGui.newFrame();
    }

    @Override
    public void endFrame() {
        ImGui.render();

        imGuiGl3.renderDrawData(ImGui.getDrawData());

        // if ImGui wants to capture the input, disable libGDX's input processor
        if (ImGui.getIO().getWantCaptureKeyboard() || ImGui.getIO().getWantCaptureMouse()) {
            imGuiInputProcessor = Gdx.input.getInputProcessor();
            Gdx.input.setInputProcessor(null);
        }
    }

    @Override
    public void dispose() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        imGuiGl3 = null;
        imGuiGlfw = null;
        ImGui.destroyContext();
    }

    @Override
    public ImFont getFont(String name) {
        return fonts.get(name);
    }

    private void initDocking() {
        // TODO(brian): initialize docking support
    }

    private void initFonts() {
        var ioFonts = ImGui.getIO().getFonts();
        ioFonts.setFreeTypeRenderer(true);

        // add defaults for latin glyphs
        ioFonts.addFontDefault();

        // add custom ranges for several types of glyphs
        var rangesBuilder = new ImFontGlyphRangesBuilder();
        rangesBuilder.addRanges(ioFonts.getGlyphRangesDefault());
        rangesBuilder.addRanges(ioFonts.getGlyphRangesCyrillic());
        rangesBuilder.addRanges(ioFonts.getGlyphRangesJapanese());
        rangesBuilder.addRanges(FontAwesomeIcons._IconRange);

        var config = new ImFontConfig();
        config.setOversampleH(4);
        config.setOversampleV(4);

        // don't merge all fonts so glyphs don't get clobbered, especially for icon fonts
        config.setMergeMode(false);

        // load normal fonts
        var defaultSize = 18f;
        var glyphRanges = rangesBuilder.buildRanges();
        loadFontTTF("tahoma.ttf",                   defaultSize, config, glyphRanges);
        loadFontTTF("noto-sans-cjk-jp-medium.otf",  defaultSize, config, glyphRanges);
        loadFontTTF("cousine-regular.ttf",          defaultSize, config, glyphRanges);
        loadFontTTF("droid-sans.ttf",               defaultSize, config, glyphRanges);
        loadFontTTF("play-regular.ttf",             defaultSize, config, glyphRanges);

        // load 'icon' fonts, with a more 'monospace' look to facilitate alignment
        // see: https://github.com/ocornut/imgui/blob/master/docs/FONTS.md#using-icon-fonts
        config.setGlyphMinAdvanceX(defaultSize);
        config.setMergeMode(true); // only merge icon glyphs
        loadFontTTF("fa-solid-900-v6.ttf", defaultSize, config, glyphRanges); // font awesome - solid v6

        // finalize the fonts
        ioFonts.build();

        // cleanup
        config.destroy();
    }

    private void loadFontTTF(String fontName, float sizePixels, ImFontConfig config, short[] glyphRanges) {
        var ioFonts = ImGui.getIO().getFonts();
        var fontFile = Gdx.files.internal("fonts/%s".formatted(fontName));
        var fontData = fontFile.readBytes();
        var font = ioFonts.addFontFromMemoryTTF(fontData, sizePixels, config, glyphRanges);
        fonts.put(fontName, font);
    }
}
