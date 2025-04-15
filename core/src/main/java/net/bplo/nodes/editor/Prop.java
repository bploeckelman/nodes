package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Prop extends EditorObject {

    public final Node node;
    public final List<Pin> pins;

    public String name = "";

    public Prop(Node node) {
        super(Type.PROP);
        this.node = node;
        this.pins = new ArrayList<>();
        node.props.add(this);
    }

    /**
     * Limited access constructor intended for use by {@link EditorSerializer}
     * to create {@link Prop} instances from saved json data.
     */
    Prop(long savedId, Node node) {
        super(Type.PROP, savedId);
        this.node = node;
        this.pins = new ArrayList<>();
        node.props.add(this);
    }

    public abstract Object getData();
    public abstract void setData(Json json, JsonValue dataValue);

    public Stream<Pin> inputPins() {
        return getPins(Pin::isInput);
    }

    public Stream<Pin> outputPins() {
        return getPins(Pin::isOutput);
    }

    public Stream<Pin> getPins(Predicate<? super Pin> filter) {
        return pins.stream().filter(filter);
    }

    public abstract void render();
    public void renderInfoPane() {
        ImGui.textDisabled("no data");
    }
}
