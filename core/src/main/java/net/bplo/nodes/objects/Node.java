package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.extension.nodeditor.NodeEditor;

import java.util.ArrayList;
import java.util.List;

public class Node extends EditorObject {

    public final List<NodeProperty> props = new ArrayList<>();

    // TODO(brian): how to handle querying linkage?

    public Node() {
        super(Type.NODE);
    }

    @Override
    public void render() {
        NodeEditor.beginNode(id);
        ImGui.pushID(id);
        ImGui.beginGroup();

        ImGui.setNextItemWidth(100);
        beginColumn();
        {
            ImGui.text("This is a node");
            ImGui.text("content under construction");
        }
        endColumn();

        ImGui.endGroup();
        ImGui.popID();
        NodeEditor.endNode();
    }

    // NOTE(brian): these 'begin/next/endColumn()' methods are a workaround for tables...
    //  table api uses 'begin/endChild()' which node-editor doesn't play nice with
    //  see: https://github.com/ocornut/imgui/blob/master/imgui_tables.cpp#L39
    //  interestingly, it achieves the same thing with simpler layout code ¯\_(ツ)_/¯

    public static void beginColumn() {
        ImGui.beginGroup();
    }

    public static void nextColumn() {
        ImGui.endGroup();
        ImGui.sameLine();
        ImGui.beginGroup();
    }

    public static void endColumn() {
        ImGui.endGroup();
    }
}
