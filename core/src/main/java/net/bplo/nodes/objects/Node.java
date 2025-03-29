package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.editor.EditorObject;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiLayout;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static net.bplo.nodes.editor.EditorUtil.*;

public class Node extends EditorObject {

    public static final float DEFAULT_WIDTH = 200f;

    public final List<Prop> props = new ArrayList<>();
    public final List<Pin> pins = new ArrayList<>();

    public float width = DEFAULT_WIDTH;

    private static class Bounds {
        public final ImGuiWidgetBounds header           = new ImGuiWidgetBounds();
        public final ImGuiWidgetBounds content          = new ImGuiWidgetBounds();
        public final ImGuiWidgetBounds node             = new ImGuiWidgetBounds();
        public final ImGuiWidgetBounds nodeBackground   = new ImGuiWidgetBounds();
        public final ImGuiWidgetBounds headerBackground = new ImGuiWidgetBounds();
    }

    private final Bounds bounds = new Bounds();

    public Node() {
        super(Type.NODE);
    }

    //
    // TODO(brian): thinking about whether to use these add(...) methods
    //  or just add pins/props to the node directly in their constructors
    //

    public Prop add(Prop property) {
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

        // wrap the node in group to calc node bounds
        ImGui.setNextItemWidth(width);
        ImGui.beginGroup();
        {
            renderNodeHeader();
            renderNodeContent();
        }
        ImGui.endGroup();
        bounds.node.update();

        ImGui.popID();
        NodeEditor.endNode();

        renderAfterNode();
    }

    @Override
    public void renderAfterNode() {
        var draw     = NodeEditor.getNodeBackgroundDrawList(id);
        var padding  = NodeEditor.getStyle().getNodePadding();
        var rounding = NodeEditor.getStyle().getNodeRounding();
        var border   = NodeEditor.getStyle().getNodeBorderWidth();

        // draw node background
        var img = Images.nodeHeader;
        var bg  = bounds.nodeBackground;
        var color = ImGuiColors.darkGray.asInt(0.5f);
        bg.setWithPadding(bounds.node, padding);
        draw.addRectFilled(bg.min(), bg.max(), color, rounding);

        // draw node header background
        bg = bounds.headerBackground;
        bg.setWithPadding(bounds.header, padding, -border);
        color = ImGuiColors.teal.asInt();
        draw.addImageRounded(img.id(),
            bg.min(), bg.max(),
            img.uv1(), img.uv2(),
            color, rounding);

        // NOTE: not needed at the moment because the inline text looks ok
        // draw the node header text, centered in the header rectangle
        // bounds.headerBackground.setWithPadding(bounds.header, padding, -border);
        // ImGuiUtil.drawTextCentered("Node Header", Colors.white, draw, bounds.headerBackground);

        // ensure renderAfterNode is called for all this node's 'child' objects
        props.forEach(Prop::renderAfterNode);
        pins.forEach(Pin::renderAfterNode);
    }

    private void renderNodeHeader() {
        var textColumnSize = new ImVec2(width - 2 * Pin.SIZE, Pin.SIZE);

        ImGui.beginGroup();
        {
            ImGuiLayout.beginColumn(Pin.SIZE);
            {
                inputPins().forEach(Pin::render);
            }
            ImGuiLayout.nextColumn(textColumnSize.x);
            {
                ImGui.pushFont(Fonts.nodeHeader);

                // NOTE: ImGui.text*() widgets collapse to the width of the text,
                //  so a dummy item is used to reserve space for the text, then
                //  the cursor is reset to the start of the column to draw the text
                //  theoretically the cursor could be adjusted to center the text,
                //  but there were quirks with that the first time I tried
                var cursor = ImGui.getCursorPos();
                ImGui.setNextItemAllowOverlap();
                ImGui.dummy(textColumnSize);
                ImGui.setCursorPos(cursor);
                ImGui.textColored(ImGuiColors.white.asInt(), "Node Header");

                ImGui.popFont();
            }
            ImGuiLayout.nextColumn(Pin.SIZE);
            {
                outputPins().forEach(Pin::render);
            }
            ImGuiLayout.endColumn();
        }
        ImGui.endGroup();
        bounds.header.update();
    }

    private void renderNodeContent() {
        ImGuiLayout.beginColumn(width);
        {
            ImGui.pushFont(Fonts.nodeContent);
            props.forEach(Prop::render);
            ImGui.popFont();
        }
        ImGuiLayout.endColumn();
        bounds.content.update();
    }
}
