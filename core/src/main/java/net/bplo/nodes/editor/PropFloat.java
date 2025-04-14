package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import imgui.type.ImFloat;

public class PropFloat extends Prop {

    private static final String TAG = PropFloat.class.getSimpleName();
    private static final String DEFAULT_NAME = "Number";

    private ImFloat value;

    public float step = 0.1f;
    public float stepFast = 1f;

    public PropFloat(Node node) {
        this(node, DEFAULT_NAME);
    }

    public PropFloat(Node node, String name) {
        super(node);
        init(name, 0);
    }

    public PropFloat(Node node, String name, float initialValue) {
        super(node);
        init(name, initialValue);
    }

    PropFloat(long savedId, Node node) {
        super(savedId, node);
        init(DEFAULT_NAME, 0);
    }

    private void init(String name, float initialValue) {
        this.name = name;
        this.value = new ImFloat(initialValue);
    }

    @Override
    public Object getData() {
        return value.get();
    }

    @Override
    public void setData(Json json, JsonValue dataValue) {
        value.set(dataValue.asFloat());
    }

    @Override
    public void render() {
        var nameWidth = ImGui.calcTextSizeX(name);
        ImGui.setNextItemWidth(node.width - nameWidth);

        var label = "%s##%s_%s_%s".formatted(name, node.label(), label(), TAG.toLowerCase());
        ImGui.inputFloat(label, value, step, stepFast, "%.1f");
    }

    @Override
    public void renderInfoPane() {
        render();
    }
}
