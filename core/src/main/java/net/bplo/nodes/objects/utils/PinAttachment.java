package net.bplo.nodes.objects.utils;

import net.bplo.nodes.objects.Node;
import net.bplo.nodes.objects.Prop;

import java.util.Optional;

public sealed interface PinAttachment permits PinAttachment.NodeType, PinAttachment.PropertyType {

    record NodeType(Node node) implements PinAttachment {}

    record PropertyType(Prop property) implements PinAttachment {}

    default Optional<NodeType> asNodeAttachment() {
        if (this instanceof NodeType nodeAttachment) {
            return Optional.of(nodeAttachment);
        }
        return Optional.empty();
    }

    default Optional<PropertyType> asPropertyAttachment() {
        if (this instanceof PropertyType propertyAttachment) {
            return Optional.of(propertyAttachment);
        }
        return Optional.empty();
    }
}
