package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.editor.EditorObject;
import net.bplo.nodes.editor.EditorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Node extends EditorObject {

    public static final float DEFAULT_WIDTH = 200f;

    public final List<Prop> props = new ArrayList<>();
    public final List<Pin> pins = new ArrayList<>();

    public float width = DEFAULT_WIDTH;

    private final ImVec2 headerMin = new ImVec2();
    private final ImVec2 headerMax= new ImVec2();
    private final ImVec2 contentSize = new ImVec2();

    public Node() {
        super(Type.NODE);
    }

    public Prop add(Prop property) {
        property.node = this;
        props.add(property);
        return property;
    }

    public Pin add(Pin pin) {
        pins.add(pin);
        return pin;
    }

    public Stream<Pin> inputPins() {
        return getPins(Pin::isInput);
    }

    public Stream<Pin> outputPins() {
        return getPins(Pin::isOutput);
    }

    public Stream<Pin> getPins(Predicate<? super Pin> filter) {
        return pins.stream().filter(filter);
    }

    @Override
    public void render() {
        NodeEditor.beginNode(id);
        ImGui.pushID(id);

        ImGui.setNextItemWidth(width);
        ImGui.beginGroup();

        // node header
        ImGui.beginGroup();
        {
            ImGui.setNextItemWidth(Pin.SIZE);
            EditorUtil.beginColumn();
            {
                inputPins().forEach(Pin::render);
            }

            var headerTextSize = new ImVec2(width - 2 * Pin.SIZE, Pin.SIZE);
            EditorUtil.nextColumn(headerTextSize.x);
            {
                ImGui.pushFont(EditorUtil.Fonts.nodeHeader);

                // NOTE: normal ImGui.text*() widgets collapse to the width of the text,
                //  so in order to have a set of fixed sized columns with the text in the middle,
                //  it needs to be drawn with a draw list and a dummy item is used to reserve the space
//                var draw = ImGui.getBackgroundDrawList();
//                var draw = NodeEditor.getNodeBackgroundDrawList(id);
                ImGui.dummy(headerTextSize);

                // TODO(brian): this isn't solid yet... need to try a few things,
                //  like using textColored() + setCursorPos() to center the text
                //  instead of using a draw list and relying on getItemRectMin/max()

//                var headerText = "Node Header";
//                var textSize = ImGui.calcTextSize(headerText);
//                var textPos = new ImVec2(
//                        headerMin.x + (headerMax.x - headerMin.x - textSize.x) / 2f,
//                        headerMin.y + (headerMax.y - headerMin.y - textSize.y) / 2f);
//                draw.addText(textPos, EditorUtil.Colors.white, headerText);
//                ImGui.setCursorPos(textPos);
//                ImGui.textColored(EditorUtil.Colors.white, headerText);

                ImGui.popFont();
            }

            EditorUtil.nextColumn(Pin.SIZE);
            {
                outputPins().forEach(Pin::render);
            }
            EditorUtil.endColumn();
        }
        ImGui.endGroup();
        ImGui.getItemRectMin(headerMin);
        ImGui.getItemRectMax(headerMax);

        // node content
        ImGui.setNextItemWidth(width);
        EditorUtil.beginColumn();
        {
            ImGui.pushFont(EditorUtil.Fonts.nodeContent);
            props.forEach(Prop::render);
            ImGui.popFont();
        }
        EditorUtil.endColumn();
        ImGui.getItemRectSize(contentSize);

        ImGui.endGroup();
        ImGui.popID();
        NodeEditor.endNode();

        renderAfterNode();
    }

    @Override
    public void renderAfterNode() {
        var draw = NodeEditor.getNodeBackgroundDrawList(id);
        var rounding = NodeEditor.getStyle().getNodeRounding();
        var img = EditorUtil.Images.nodeHeader;
        var color = EditorUtil.Colors.teal;
        draw.addImageRounded(img.id(), headerMin, headerMax, img.uv1(), img.uv2(), color, rounding);

        var headerText = "Node Header";
        var textSize = ImGui.calcTextSize(headerText);
        var textPos = new ImVec2(
            headerMin.x + (headerMax.x - headerMin.x - textSize.x) / 2f,
            headerMin.y + (headerMax.y - headerMin.y - textSize.y) / 2f);
        draw.addText(textPos, EditorUtil.Colors.white, headerText);

        props.forEach(Prop::renderAfterNode);
        pins.forEach(Pin::renderAfterNode);
    }
}
