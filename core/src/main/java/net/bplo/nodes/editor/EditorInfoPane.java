package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import net.bplo.nodes.imgui.ImGuiColors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EditorInfoPane extends EditorPane {

    private final List<Node> selectedNodes = new ArrayList<>();
    private final ImVec2 undockedSize = new ImVec2();
    private boolean wasDocked = false;


    public EditorInfoPane(Editor editor) {
        super(editor);
    }

    public void clear() {
        selectedNodes.clear();
    }

    public void select(Node node) {
        clear();
        selectedNodes.add(node);
    }

    public void select(List<Node> nodes) {
        clear();
        selectedNodes.addAll(nodes);
        selectedNodes.sort(Comparator.comparingLong(node -> node.id));
    }

    @Override
    public void render() {
        setInitialPositionAndSize();

        if (ImGui.begin("Properties")) {
            if (selectedNodes.isEmpty()) {
                ImGui.textDisabled("No nodes selected");
            } else {
                var plural = selectedNodes.size() > 1 ? "s" : "";
                ImGui.pushFont(EditorUtil.Fonts.nodeHeader);
                ImGui.text("Selected Node%s:".formatted(plural));
                ImGui.popFont();

                ImGui.pushFont(EditorUtil.Fonts.small);
                ImGui.pushStyleColor(ImGuiCol.Text, ImGuiColors.goldenrod.asInt());
                selectedNodes.forEach(node -> ImGui.bulletText(node.label()));
                ImGui.popStyleColor();
                ImGui.popFont();

                ImGui.separator();
                ImGui.dummy(0, 2);

                for (var node : selectedNodes) {
                    ImGui.pushFont(EditorUtil.Fonts.nodeHeader);
                    ImGui.pushStyleColor(ImGuiCol.Text, ImGuiColors.cyan.asInt());
                    ImGui.text("%s (%s)".formatted(node.headerText, node.label()));
                    ImGui.popStyleColor();
                    ImGui.popFont();
                    ImGui.separator();

                    // TODO(brian): when selecting multiple nodes with PropEditableText props
                    //  only the first node's editable text prop is populated for some reason
                    node.props.forEach(prop -> {
                        if (editor.nodePane.showIds) {
                            var propType = prop.getClass().getSimpleName().replaceFirst("Prop", "");
                            ImGui.textColored(ImGuiColors.teal.asInt(), "%s (%s)".formatted(propType, prop.label()));
                            ImGui.dummy(0, 1);
                        }
                        prop.renderInfoPane(editor);
                        ImGui.dummy(0, 4);
                    });
                }
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
