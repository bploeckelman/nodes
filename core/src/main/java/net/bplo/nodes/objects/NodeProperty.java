package net.bplo.nodes.objects;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeProperty {

    public final List<Pin> pins = new ArrayList<>();

    public Node node;

    public NodeProperty() {
        this(null);
    }

    public NodeProperty(Node node) {
        this.node = node;
    }

    public abstract void render();
}
