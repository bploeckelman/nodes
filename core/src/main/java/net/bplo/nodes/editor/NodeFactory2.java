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
            Util.log(TAG, "*** Unsupported prop type: " + propType.type);
            return;
        }

        try {
            var ctor = propClass.getDeclaredConstructor(Node.class);
            var prop = (Prop) ctor.newInstance(node);
            prop.name = Objects.requireNonNullElse(propType.name, prop.defaultName());

            Util.log(TAG, "Created prop: name='%s' id='%s' type='%s'".formatted(prop.name, propType.id, propType.type));

            var hasAssetType = propType.assetType != null;
            var hasDisplay   = propType.display   != null;
            if (hasAssetType && hasDisplay) {
                var display = new Metadata.Display(propType.display);

                // resolve and set prop data based on prop type
                if (prop instanceof PropSelect select) {
                    var selectData = (PropSelect.Data) select.getData();
                    var assetType = editor.metadataRegistry.findAssetType(propType.assetType);
                    if (assetType.isEmpty()) {
                        Util.log(TAG, "Failed to find asset type: " + propType.assetType);
                        return;
                    }

                    var options = assetType.get().getItemFieldValues(display.field, String.class);
                    Util.log(TAG, "Resolved %d options for select prop '%s' from display value '%s': %s"
                        .formatted(options.size(), propType.name, propType.display, options.stream().collect(Util.join())));

                    selectData.options = options.toArray(new String[0]);
                    selectData.selectedIndex = options.isEmpty() ? -1 : 0;
                }
                else if (prop instanceof PropThumbnail thumbnail) {
                    var assetType = editor.metadataRegistry.findAssetType(propType.assetType);
                    if (assetType.isEmpty()) {
                        Util.log(TAG, "Failed to find asset type: " + propType.assetType);
                        return;
                    }

                    // TODO(brian): testing, resolve one assetType item ref
                    // TODO(brian): need to finalize asset ref lookups (loading/caching), and property dependencies
                    //   eg. 'portrait' prop assetRef depends on current selection from character select prop
                    //   - add optional 'dependsOn' field to PropType to indicate which prop (by id) the dependent prop references
                    var portraits = assetType.get().getItemFieldValues(display.field, Metadata.AssetItemRef.class);
                    if (portraits.isEmpty()) {
                        Util.log(TAG, "Expected 1 portrait asset item ref for thumbnail prop '%s' from display value '%s', got %d"
                            .formatted(propType.name, propType.display, portraits.size()));
                        return;
                    }
                    thumbnail.assetRef = portraits.getFirst();
                }
            }
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new GdxRuntimeException(e);
        }
    }

    private static Class<? extends Prop> getPropClass(Metadata.PropType propType) {
        return switch (propType.type) {
            case "float" -> PropFloat.class;
            case "integer" -> PropInteger.class;
            case "select" -> PropSelect.class;
            case "thumbnail" -> PropThumbnail.class;
            // TODO(brian): create separate single/multi line editable text prop types
            case "input-text", "input-text-multiline" -> PropEditableText.class;
            default -> null;
        };
    }
}
