package net.bplo.nodes.objects.utils;

import net.bplo.nodes.objects.Node;
import net.bplo.nodes.objects.Prop;

public sealed interface PinAttachment permits PinAttachment.NodeType, PinAttachment.PropertyType {
    record NodeType(Node node) implements PinAttachment {
    }

    record PropertyType(Prop property) implements PinAttachment {
    }
}
