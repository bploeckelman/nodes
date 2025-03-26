package net.bplo.nodes.imgui;

import imgui.ImFont;

public interface ImGuiPlatform {
    void init();
    void startFrame();
    void endFrame();
    void dispose();
    ImFont getFont(String name);
}
