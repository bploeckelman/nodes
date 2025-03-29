package net.bplo.nodes.imgui;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;

public record ImGuiWidgetBounds(
    ImVec2 min,
    ImVec2 max,
    ImVec2 size,
    ImVec2 center
) {
    public ImGuiWidgetBounds() {
        this(new ImVec2(), new ImVec2(), new ImVec2(), new ImVec2());
    }

    public void update() {
        ImGui.getItemRectMin(min);
        ImGui.getItemRectMax(max);
        updateInternal();
    }

    public void setWithPadding(ImGuiWidgetBounds bounds, ImVec4 pad) {
        setWithPadding(bounds, pad, 0f);
    }

    public void setWithPadding(ImGuiWidgetBounds bounds, ImVec4 pad, float adjust) {
        // TODO(brian): verify that x,y,z,w are left,top,right,bottom (I think)
        min.set(bounds.min.x - pad.x - adjust, bounds.min.y - pad.y - adjust);
        max.set(bounds.max.x + pad.z + adjust, bounds.max.y + pad.w + adjust);
        updateInternal();
    }

    private void updateInternal() {
        size.set(
            max.x - min.x,
            max.y - min.y);
        center.set(
            min.x + size.x / 2f,
            min.y + size.y / 2f);
    }
}
