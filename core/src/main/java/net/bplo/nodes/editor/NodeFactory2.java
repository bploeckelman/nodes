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

        // wire up prop dependencies now that all props are instantiated
        // TODO(brian): this works for the test metadata, but its more complicated than I'd like
        node.props.stream().filter(prop -> prop.dependsOn != null)
            .forEach(dstProp -> {
                node.findProp(dstProp.dependsOn).ifPresentOrElse(srcProp -> {
                    var srcPropType = nodeType.findPropType(srcProp.propTypeId);
                    var dstPropType = nodeType.findPropType(dstProp.propTypeId);
                    if (srcPropType.isEmpty() || dstPropType.isEmpty()) {
                        Util.log(TAG, "Failed to resolve prop types for dependency: %s -> %s"
                            .formatted(dstProp.propTypeId, dstProp.dependsOn));
                        return;
                    }

                    // setup change callback
                    srcProp.onChange = (newValue) -> updateDependentProp(
                        editor, dstProp, srcProp, newValue, dstPropType.get(), srcPropType.get());

                    // trigger initial update based on current value
                    updateDependentProp(editor, dstProp, srcProp, srcProp.getData(), dstPropType.get(), srcPropType.get());
                }, () -> Util.log(TAG, "Failed to find prop dependency: %s -> %s"
                    .formatted(dstProp.propTypeId, dstProp.dependsOn)));
            });

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
            prop.propTypeId = propType.id;
            prop.dependsOn = propType.dependsOn;

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

    private static void updateDependentProp(Editor editor, Prop dstProp, Prop srcProp, Object value, Metadata.PropType dstPropType, Metadata.PropType srcPropType) {
        if (dstProp instanceof PropThumbnail thumbnail && srcProp instanceof PropSelect select) {
            updateThumbnailFromSelect(editor, thumbnail, select, dstPropType, srcPropType);
        }
//        if (dstProp instanceof PropSelect dstSelect && srcProp instanceof PropSelect srcSelect) {
//             TODO(brian): add select chaining for character select -> character pose select
//        }
        // ... add more combinations as needed...
    }
    private static void updateThumbnailFromSelect(Editor editor, PropThumbnail thumbnail, PropSelect select, Metadata.PropType thumbnailPropType, Metadata.PropType selectPropType) {
        if (thumbnailPropType.display == null || selectPropType.assetType == null) {
            return;
        }

        // Parse the display path for the thumbnail
        Metadata.Display display = new Metadata.Display(thumbnailPropType.display);
        if (!"#{value}".equals(display.type)) {
            Util.log(TAG, "Only #{value}.field format is supported for display paths, got: " + display.type);
            return;
        }

        // Get the selected item ID/name
        var selectData = (PropSelect.Data) select.getData();
        var selectedOption = selectData.getSelectedOption();
        if (selectedOption.isEmpty()) {
            // Clear the thumbnail if nothing is selected
            thumbnail.assetRef = null;
            return;
        }

        // Find the asset item for the selected option
        var registry = editor.metadataRegistry;
        var assetType = registry.findAssetType(selectPropType.assetType);
        if (assetType.isEmpty()) {
            Util.log(TAG, "Asset type not found: " + selectPropType.assetType);
            return;
        }

        // Find the asset item by name
        var assetItems = assetType.get().items;
        var selectedItem = Util.asList(assetItems).stream()
            .filter(item -> item.name.equals(selectedOption))
            .findFirst();
        if (selectedItem.isEmpty()) {
            Util.log(TAG, "Asset item not found with name: " + selectedOption);
            return;
        }

        // Get the field from the selected item (like 'portrait')
        var itemProperties = selectedItem.get().properties;
        if (!itemProperties.containsKey(display.field)) {
            Util.log(TAG, "Property not found in asset item: " + display.field);
            return;
        }

        var fieldValue = itemProperties.get(display.field);
        if (!(fieldValue instanceof Metadata.AssetItemRef)) {
            Util.log(TAG, "Expected AssetItemRef for field: " + display.field);
            return;
        }

        // Set the asset reference on the thumbnail
        thumbnail.assetRef = (Metadata.AssetItemRef) fieldValue;

        // Force a refresh of the thumbnail
        thumbnail.clearImage();
    }
}
