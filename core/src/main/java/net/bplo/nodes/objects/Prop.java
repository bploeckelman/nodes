package net.bplo.nodes.objects;

import net.bplo.nodes.editor.EditorObject;

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
