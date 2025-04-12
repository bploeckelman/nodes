package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCond;

public class EditorInfoPane extends EditorPane {

    Node selectedNode;
    Prop focusedProp;

    private final ImVec2 undockedSize = new ImVec2();
    private boolean wasDocked = false;

    public EditorInfoPane(Editor editor) {
        super(editor);
        this.selectedNode = null;
        this.focusedProp = null;
    }

    @Override
    public void render() {
        setInitialPositionAndSize();

        if (ImGui.begin("Properties")) {
            if (selectedNode == null) {
                ImGui.textDisabled("No node selected");
            } else {
                // Show node info
                ImGui.text("Selected Node: #" + selectedNode.id);

                // render node properties in the info pane
                selectedNode.props.forEach(Prop::renderInfoPane);
            }

            if (ImGui.isWindowDocked()) {
                wasDocked = true;
            } else {
                if (wasDocked) {
                    wasDocked = false;
                    ImGui.setWindowSize(undockedSize);
                }
                undockedSize.set(ImGui.getWindowSize());
            }
        }

        ImGui.end();
    }

    private void setInitialPositionAndSize() {
        var viewport = ImGui.getMainViewport();

        var padding = 10f;
        var width = viewport.getSizeX() * 0.3f;
        var height = viewport.getSizeY() * 0.7f;
        var x = viewport.getPosX() + padding;
        var y = viewport.getPosY() + (viewport.getSizeY() - height) / 2f;

        ImGui.setNextWindowPos(x, y, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(width, height, ImGuiCond.FirstUseEver);
    }
}
