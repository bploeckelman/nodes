package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.GdxRuntimeException;
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

            Util.log(TAG, "Created prop: name='%s' id='%s' type='%s'".formatted(prop.name, propType.id, propType.propClass));
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new GdxRuntimeException(e);
        }
    }

//    public static void updateDependentProp(Editor editor, Prop dstProp, Prop srcProp, Object value, PropType dstPropType, PropType srcPropType) {
//        if (dstPropType.display == null) return;
//
//        // parse the display path, eg: '#{value}.{field}'
//        var display = new Display(dstPropType.display);
//        if (!"#{value}".equals(display.type)) return;
//
//        // get the source value
//        var srcData = srcProp.getData();
//        var srcValue = srcProp instanceof PropSelect ? ((PropSelect.Data) srcData).getSelectedOption() : String.valueOf(srcData);
//        if (srcValue.isEmpty()) {
//            clearPropValue(dstProp);
//            return;
//        }
//
//        // resolve the reference based on the source value and field path
//        resolvePropertyReference(editor, dstProp, srcPropType.assetType, srcValue, display.field);
//    }

//    private static void resolvePropertyReference(Editor editor, Prop dstProp, String assetType, String itemName, String propertyPath) {
//        var registry = editor.metadataRegistry;
//        registry.findAssetType(assetType).ifPresent(type -> {
//            // find the item by name
//            var item = Util.asList(type.items).stream()
//                .filter(assetItem -> assetItem.name.equals(itemName))
//                .findFirst();
//
//            if (item.isEmpty()) return;
//
//            // resolve the property path (handle nested paths with dots)
//            var propValue = resolvePropertyPath(item.get(), propertyPath);
//
//            // set the property value based on the prop type
//            setPropValueByType(dstProp, propValue);
//        });
//    }

//    @SuppressWarnings("unchecked")
//    private static Object resolvePropertyPath(AssetItem item, String propertyPath) {
//        var pathParts = propertyPath.split("\\.");
//
//        Object current = item;
//        for (var part : pathParts) {
//            switch (current) {
//                case AssetItem assetItem -> {
//                    // try basic properties first
//                    if      ("id".equals(part))   current = assetItem.id;
//                    else if ("name".equals(part)) current = assetItem.name;
//                    else if ("path".equals(part)) current = assetItem.path;
//                    // then check the properties map
//                    else if (assetItem.properties.containsKey(part)) {
//                        current = assetItem.properties.get(part);
//                    } else {
//                        return null; // property not found
//                    }
//                }
//                case ObjectMap<?, ?> objectMap -> {
//                    // navigate through ObjectMap properties
//                    var map = (ObjectMap<String, Object>) current;
//                    if (!map.containsKey(part)) return null;
//                    current = map.get(part);
//                }
//                default -> {
//                    // can't navigate further
//                    return null;
//                }
//            }
//        }
//        return current;
//    }

//    @SuppressWarnings("unchecked")
//    private static void setPropValueByType(Prop dstProp, Object value) {
//        if (dstProp instanceof PropThumbnail thumbnail && value instanceof AssetRef assetRef) {
//            thumbnail.assetRef = assetRef;
//            thumbnail.clearImage();
//        }
//        else if (dstProp instanceof PropSelect select && value instanceof Array<?> array) {
//            var options = new Array<String>();
//            if (!array.isEmpty() && array.get(0) instanceof AssetRef) {
//                var editor = Main.app.editor;
//                var assets = Main.app.assets;
//                var assetRefs = (Array<AssetRef>) array;
//                for (var assetRef : assetRefs) {
//                    assets.resolveAssetRef(editor, assetRef, String.class)
//                        .ifPresent(options::add);
//                }
//            }
//            var selectData = (PropSelect.Data) select.getData();
//            selectData.options = options.toArray(String.class);
//            selectData.selectedIndex = 0;
//
//        }
//        // add more type handlers as needed
//    }

//    private static void clearPropValue(Prop dstProp) {
//        if (dstProp instanceof PropThumbnail thumbnail) {
//            thumbnail.assetRef = null;
//            thumbnail.clearImage();
//        }
//        // add more type handlers as needed
//    }
}
