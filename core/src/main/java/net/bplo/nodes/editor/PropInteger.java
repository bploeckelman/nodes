package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import imgui.type.ImInt;

public class PropInteger extends Prop {

    private static final String TAG = PropInteger.class.getSimpleName();
    private static final String DEFAULT_NAME = "Number";

    private ImInt value;

    public PropInteger(Node node) {
        this(node, DEFAULT_NAME);
    }

    public PropInteger(Node node, String name) {
        super(node);
        init(name, 0);
    }

    public PropInteger(Node node, String name, int initialValue) {
        super(node);
        init(name, initialValue);
    }

    PropInteger(long savedId, Node node) {
        super(savedId, node);
        init(DEFAULT_NAME, 0);
    }

    private void init(String name, int initialValue) {
        this.name = name;
        this.value = new ImInt(initialValue);
    }

    @Override
    public Object getData() {
        return value.get();
    }

    @Override
    public void setData(Json json, JsonValue dataValue) {
        value.set(dataValue.asInt());
    }

    @Override
    public void render() {
        ImGui.beginGroup();
        {
            var nameWidth = ImGui.calcTextSizeX(name);
            ImGui.setNextItemWidth(node.width - nameWidth);

            var label = "%s##%s_%s_%s".formatted(name, node.label(), label(), TAG.toLowerCase());
            ImGui.inputInt(label, value);
        }
        ImGui.endGroup();
        bounds.update();
    }

    @Override
    public void renderInfoPane(Editor editor) {
        render();
    }
}
