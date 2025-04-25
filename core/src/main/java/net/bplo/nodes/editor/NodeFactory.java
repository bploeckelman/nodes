package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.GdxRuntimeException;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;

import java.lang.reflect.InvocationTargetException;

public class NodeFactory {

    private static final String TAG = NodeFactory.class.getSimpleName();

    /**
     * Create a new {@link Node}, configured based on the specified {@link NodeType}
     */
    public static Node createNode(Editor editor, NodeType nodeType) {
        var node = new Node();
        node.headerText = nodeType.name;

        for (int i = 0; i < nodeType.inputs; i++) {
            new Pin(node, PinKind.INPUT, PinType.FLOW);
        }

        for (int i = 0; i < nodeType.outputs; i++) {
            new Pin(node, PinKind.OUTPUT, PinType.FLOW);
        }

        for (var propDef : nodeType.props) {
            createProp(editor, node, propDef);
        }

        return node;
    }

    private static void createProp(Editor editor, Node node, NodeType.PropDef propDef) {
        Class<? extends Prop> propClass;
        try {
            var clazz = Class.forName(propDef.type);
            if (!Prop.class.isAssignableFrom(clazz)) {
                throw new ReflectiveOperationException("Class is not a Prop subclass");
            }
            //noinspection unchecked
            propClass = (Class<? extends Prop>) clazz;
        } catch (ReflectiveOperationException e) {
            Util.log(TAG, "Unable to create prop, unsupported type: " + propDef.type);
            return;
        }

        // instantiate the prop
        try {
            var ctor = propClass.getDeclaredConstructor(Node.class);
            var prop = (Prop) ctor.newInstance(node);

            if (propDef.name != null) {
                prop.name = propDef.name;
            }

            if (propDef.assetEntry != null && editor.assetMetadata != null) {
                var asset = editor.assetMetadata.resolveAssetEntry(propDef.assetEntry);
                Util.log(TAG, "Prop %s asset entry %s: %s".formatted(propDef.id, propDef.assetEntry, asset));

                // update prop data with resolved asset based on prop and asset types
                var propData = prop.getData();
                if (prop instanceof PropSelect select) {
                    var selectData = (PropSelect.Data) propData;
                    //noinspection unchecked
//                    var options = (Array<String>) asset.value();
//                    selectData.options = options.toArray(String.class);
//                    selectData.selectedIndex = 0;
                } else {
                    Util.log(TAG, "AssetEntry handling for prop type '%s' yet implemented".formatted(propDef.id));
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new GdxRuntimeException(e);
        }
    }
}
