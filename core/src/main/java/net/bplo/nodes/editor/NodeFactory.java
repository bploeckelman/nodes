package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.GdxRuntimeException;
import net.bplo.nodes.Main;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.meta.Metadata;
import net.bplo.nodes.editor.meta.Metadata.PropType;
import net.bplo.nodes.editor.meta.PropBindingResolver;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class NodeFactory {

    private static final String TAG = NodeFactory.class.getSimpleName();

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
            createProp(node, propType);
        }

        var bindingResolver = new PropBindingResolver(editor.assetResolver);
        bindingResolver.resolveBindings(node, nodeType.props);

        return node;
    }

    private static void createProp(Node node, PropType<?> propType) {
        try {
            var ctor = propType.getPropClass().getDeclaredConstructor(Node.class);
            var prop = (Prop) ctor.newInstance(node);
            prop.name = Objects.requireNonNullElse(propType.name, prop.defaultName());
            prop.propTypeId = propType.id;
            populatePropData(node, propType, prop);
            Util.log(TAG, "Created prop: name='%s' id='%s' type='%s'".formatted(prop.name, propType.id, propType.propClass));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new GdxRuntimeException(e);
        }
    }

    private static void populatePropData(Node node, PropType<?> propType, Prop prop) {
        if (prop instanceof PropSelect select && propType.assetType != null) {
            var editor = Main.app.editor;
            var selectData = (PropSelect.Data) select.getData();
            var options = editor.metadata.findAssetType(propType.assetType)
                    .map(type -> Util.asList(type.items).stream()
                            .map(item -> item.name)
                            .toArray(String[]::new))
                    .orElse(new String[0]);
            selectData.options = options;
            selectData.selectedIndex = options.length > 0 ? 0 : -1;
        }
        // add other handlers as needed
    }
}
