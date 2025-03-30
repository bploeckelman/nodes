package net.bplo.nodes.objects.utils;

import net.bplo.nodes.objects.Node;
import net.bplo.nodes.objects.Prop;

import java.util.Optional;

public sealed interface PinAttachment permits PinAttachment.NodeType, PinAttachment.PropType {

    record NodeType(Node node) implements PinAttachment {}

    record PropType(Prop property) implements PinAttachment {}

    default Optional<Node> getNode() {
        if      (this instanceof NodeType(Node node)) return Optional.of(node);
        else if (this instanceof PropType(Prop prop)) return Optional.of(prop.node);
        return Optional.empty();
    }

    default Optional<NodeType> asNodeAttachment() {
        if (this instanceof NodeType nodeAttachment) {
            return Optional.of(nodeAttachment);
        }
        return Optional.empty();
    }

    default Optional<PropType> asPropertyAttachment() {
        if (this instanceof PropType propertyAttachment) {
            return Optional.of(propertyAttachment);
        }
        return Optional.empty();
    }
}
