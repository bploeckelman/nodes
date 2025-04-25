package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.GdxRuntimeException;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.meta.Metadata;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class NodeFactory2 {

    private static final String TAG = NodeFactory2.class.getSimpleName();

    public static Node createNode(Editor editor, Metadata.NodeType nodeType) {
        var node = new Node();
        node.headerText = nodeType.name;

        for (int i = 0; i < nodeType.inputs; i++) {
            new Pin(node, PinKind.INPUT, PinType.FLOW);
        }

        for (int i = 0; i < nodeType.outputs; i++) {
            new Pin(node, PinKind.OUTPUT, PinType.FLOW);
        }

        for (var propType : nodeType.props) {
            createProp(editor, node, propType);
        }

        return node;
    }

    private static void createProp(Editor editor, Node node, Metadata.PropType propType) {
        var propClass = getPropClass(propType);
        if (propClass == null) {
            Util.log(TAG, "Unsupported prop type: " + propType.type);
            return;
        }

        try {
            var ctor = propClass.getDeclaredConstructor(Node.class);
            var prop = (Prop) ctor.newInstance(node);
            prop.name = Objects.requireNonNullElse(propType.name, prop.defaultName());
            // TODO(brian): get prop data by resolving 'propType.display' field
            Util.log(TAG, "Created prop: name='%s' id='%s' type='%s'".formatted(prop.name, propType.id, propType.type));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new GdxRuntimeException(e);
        }
    }

    private static Class<? extends Prop> getPropClass(Metadata.PropType propType) {
        return switch (propType.type) {
            case "float" -> PropFloat.class;
            case "integer" -> PropInteger.class;
            case "select" -> PropSelect.class;
            // TODO(brian): create separate single/multi line editable text prop types
            case "input-text", "input-text-multiline" -> PropEditableText.class;
            default -> null;
        };
    }
}
