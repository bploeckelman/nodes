package net.bplo.nodes.imgui;

import imgui.ImVec4;

public class ImGuiUtil {

    public static ImVec4 toImVec4(int abgrPackedColor) {
        int a = (abgrPackedColor >> 24) & 0xff;
        int b = (abgrPackedColor >> 16) & 0xff;
        int g = (abgrPackedColor >> 8)  & 0xff;
        int r = (abgrPackedColor     )  & 0xff;
        return new ImVec4(r, g, b, a);
    }
}
