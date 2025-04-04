package net.bplo.nodes.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Prop extends EditorObject {

    public final Node node;
    public final List<Pin> pins;

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
}
