package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImColor;
import imgui.ImGui;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;
import net.bplo.nodes.imgui.ImGuiLayout;

public class PropTest extends Prop {

    private final int backgroundColor = ImColor.rgba("#00004f2f");

    public PropTest(Node node) {
        super(node);
        new Pin(this, PinKind.INPUT,  PinType.DATA);
        new Pin(this, PinKind.OUTPUT, PinType.DATA);
    }

    PropTest(long savedId, Node node) {
        super(savedId, node);
        // NOTE(brian): 'internally created pins' for a prop are a quirk for deserialization,
        //  they can't be created in ctor because they require the saved id on construction.
        //  so don't instantiate them here, instead deserialization instantiates/attaches them
    }

    @Override
    public Object getData() { return null; }

    @Override
    public void setData(Json json, JsonValue dataValue) {}

    @Override
    public void render() {
        ImGui.beginGroup();
        {
            var contentWidth = node.width - 2 * Pin.SIZE;

            ImGuiLayout.beginColumn(Pin.SIZE);
            {
                inputPins().forEach(Pin::render);
            }
            ImGuiLayout.nextColumn(contentWidth);
            {
                // NOTE: same approach as in Node.render() to ensure a fixed column width
                //  when the only widget in the column is text, which collapses to fit the string
                var cursorPos = ImGui.getCursorPos();
                ImGui.setNextItemAllowOverlap();
                ImGui.dummy(contentWidth, Pin.SIZE);
                ImGui.setCursorPos(cursorPos);
                ImGui.text("prop");
            }
            ImGuiLayout.nextColumn(Pin.SIZE);
            {
                outputPins().forEach(Pin::render);
            }
            ImGuiLayout.endColumn();
        }
        ImGui.endGroup();
        bounds.update();
    }

    @Override
    public void renderAfterNode() {
        var draw = NodeEditor.getNodeBackgroundDrawList(node.id);
        var rounding = NodeEditor.getStyle().getNodeRounding();
        draw.addRectFilled(bounds.min(), bounds.max(), backgroundColor, rounding);
    }
}
