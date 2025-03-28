package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.EditorUtil;

import java.util.ArrayList;
import java.util.List;

public class Node extends EditorObject {

    public final List<NodeProperty> props = new ArrayList<>();
    public final List<Pin> pins = new ArrayList<>();

    // TODO(brian): how to handle querying linkage?

    public Node() {
        super(Type.NODE);
    }

    public NodeProperty add(NodeProperty property) {
        property.node = this;
        props.add(property);
        return property;
    }

    public Pin add(Pin pin) {
        pins.add(pin);
        return pin;
    }

    @Override
    public void render() {
        NodeEditor.beginNode(id);
        ImGui.pushID(id);
        ImGui.beginGroup();

        pins.stream().filter(Pin::isInput).forEach(Pin::render);

        ImGui.setNextItemWidth(100);
        EditorUtil.beginColumn();
        {
            ImGui.pushFont(EditorUtil.Fonts.nodeHeader);
            ImGui.textColored(EditorUtil.Colors.white, "Node");
            ImGui.popFont();

            ImGui.pushFont(EditorUtil.Fonts.nodeContent);
            props.forEach(NodeProperty::render);
            ImGui.popFont();
        }
        EditorUtil.endColumn();

        pins.stream().filter(Pin::isOutput).forEach(Pin::render);

        ImGui.endGroup();
        ImGui.popID();
        NodeEditor.endNode();
    }
}
