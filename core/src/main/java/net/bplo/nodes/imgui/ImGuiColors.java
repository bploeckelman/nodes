package net.bplo.nodes.imgui;

import imgui.ImColor;
import imgui.ImVec4;

/**
 * LibGDX Color values converted for use with ImGui functions
 */
public class ImGuiColors {

    public static ImVec4 toImVec4(int abgrPackedColor) {
        var a = ((abgrPackedColor >> 24) & 0xff) / 255f;
        var b = ((abgrPackedColor >> 16) & 0xff) / 255f;
        var g = ((abgrPackedColor >> 8)  & 0xff) / 255f;
        var r = ((abgrPackedColor     )  & 0xff) / 255f;
        return new ImVec4(r, g, b, a);
    }

    public record Value(String hex) {

        public int asInt() {
            return ImColor.rgba(hex);
        }

        public int asInt(float alpha) {
            var alphaByte = (int) (alpha * 255);
            var alphaHex = "%s%02x".formatted(hex.substring(0, 7), alphaByte);
            return ImColor.rgba(alphaHex);
        }

        public ImVec4 asVec4() {
            return toImVec4(asInt());
        }

        public ImVec4 asVec4(float alpha) {
            return toImVec4(asInt(alpha));
        }
    }

    public static final Value white       = new Value("#ffffffff");
    public static final Value gray        = new Value("#7f7f7fff");
    public static final Value black       = new Value("#000000ff");

    public static final Value blue        = new Value("#0000ffff");
    public static final Value navy        = new Value("#00007fff");
    public static final Value royal       = new Value("#4169e1ff");
    public static final Value slate       = new Value("#708090ff");
    public static final Value sky         = new Value("#87ceebff");
    public static final Value cyan        = new Value("#00ffffff");
    public static final Value teal        = new Value("#007f7fff");
    public static final Value imBlue      = new Value("#4296faff");
    public static final Value imBlueDark  = new Value("#344662ff");

    public static final Value green       = new Value("#00ff00ff");
    public static final Value chartreuse  = new Value("#7fff00ff");
    public static final Value lime        = new Value("#32cd32ff");
    public static final Value forest      = new Value("#228b22ff");
    public static final Value olive       = new Value("#6b8e23ff");

    public static final Value yellow      = new Value("#ffff00ff");
    public static final Value gold        = new Value("#ffd700ff");
    public static final Value goldenrod   = new Value("#daa520ff");
    public static final Value orange      = new Value("#ffa500ff");

    public static final Value brown       = new Value("#8b4513ff");
    public static final Value tan         = new Value("#d2b48cff");
    public static final Value firebrick   = new Value("#b22222ff");

    public static final Value red         = new Value("#ff0000ff");
    public static final Value scarlet     = new Value("#ff341cff");
    public static final Value coral       = new Value("#ff7f50ff");
    public static final Value salmon      = new Value("#fa8072ff");
    public static final Value pink        = new Value("#ff69b4ff");
    public static final Value magenta     = new Value("#ff00ffff");

    public static final Value purple      = new Value("#a020f0ff");
    public static final Value violet      = new Value("#ee82eeff");
    public static final Value maroon      = new Value("#b03060ff");

    public static final Value lightGray   = new Value("#afafafff");
    public static final Value medGray     = new Value("#8f8f8fff");
    public static final Value darkGray    = new Value("#4f4f4fff");
    public static final Value darkerGray  = new Value("#2f2f2fff");

    public static final Value lightRed    = new Value("#ffd1d0ff");
    public static final Value medRed      = new Value("#ffa39eff");
    public static final Value darkRed     = new Value("#cf1322ff");

    public static final Value lightGreen  = new Value("#89fc00ff");
    public static final Value medGreen    = new Value("#29bf12ff");
    public static final Value darkGreen   = new Value("#054a29ff");

    public static final Value lightBlue   = new Value("#b6c7ffff");
    public static final Value medBlue     = new Value("#91d5ffff");
    public static final Value darkBlue    = new Value("#0050b3ff");

    public static final Value lightYellow = new Value("#ffdbb6ff");
    public static final Value medYellow   = new Value("#ffe58fff");
    public static final Value darkYellow  = new Value("#d48806ff");
}
