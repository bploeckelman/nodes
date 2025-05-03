package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.Editor;
import net.bplo.nodes.editor.Node;
import net.bplo.nodes.editor.Prop;
import net.bplo.nodes.editor.PropInputText;
import net.bplo.nodes.editor.PropSelect;
import net.bplo.nodes.editor.PropThumbnail;
import net.bplo.nodes.editor.meta.Metadata.AssetRef;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PropBindingResolver {

    private static final String TAG = PropBindingResolver.class.getSimpleName();

    private final Editor editor;

    public PropBindingResolver(Editor editor) {
        this.editor = editor;
    }

    public void resolveBindings(Node node, Array<Metadata.PropType<?>> propTypes) {
        // collect props
        var propMap = new HashMap<String, Prop>();
        for (var prop : node.props) {
            propMap.put(prop.propTypeId, prop);
        }

        // setup bindings
        for (var propType : propTypes) {
            if (propType.binding == null) continue;

            var targetProp = propMap.get(propType.id);
            var sourceProp = propMap.get(propType.binding.sourceId);

            if (targetProp == null || sourceProp == null) {
                Util.log(TAG, "Failed to resolve prop binding: %s -> %s".formatted(propType.id, propType.binding.sourceId));
                continue;
            }

            propType.binding.transformer = createTransformer(propType.binding, propMap);

            // set change listener
            sourceProp.onChange = newValue -> {
                var transformed = propType.binding.transformer.apply(newValue);
                applyTransformedValue(targetProp, transformed);
            };

            // apply initial value
            var initialValue = propType.binding.transformer.apply(sourceProp.getData());
            applyTransformedValue(targetProp, initialValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void applyTransformedValue(Prop targetProp, Object value) {
        if (targetProp instanceof PropThumbnail thumbnail && value instanceof AssetRef<?> assetRef) {
            thumbnail.assetRef = (AssetRef<Texture>) assetRef;
            thumbnail.clearImage();
        } else if (targetProp instanceof PropSelect select && value instanceof String[] strings) {
            var selectData = (PropSelect.Data) select.getData();
            selectData.options = strings;
            selectData.selectedIndex = selectData.options.length > 0 ? 0 : -1;
        } else if (targetProp instanceof PropInputText inputText && value instanceof String string) {
            inputText.setText(string);
        }
        // TODO(brian): add more handlers as needed
    }

    private Function<Object, ?> createTransformer(Metadata.PropBinding<?> binding, Map<String, Prop> propMap) {
        return switch (binding.transformType) {
            case extract_ref -> (value) -> {
                if (!(value instanceof PropSelect.Data data)) return null;
                return getPropertyFromSelection(data.getSelectedOption(), binding.propertyPath);
            };

            case extract_array_names -> (value) -> {
                if (!(value instanceof PropSelect.Data data)) return null;
                var property = getPropertyFromSelection(data.getSelectedOption(), binding.propertyPath);
                if (property instanceof Array<?> array) {
                    return Util.asList(array).stream()
                        .map(this::extractRefName)
                        .toArray(String[]::new);
                }
                return new String[0];
            };

            case resolve_from_array -> (value) -> {
                if (value instanceof PropSelect.Data data) {
                    var selectedItem = data.getSelectedOption();
                    var additionalSource = propMap.get(binding.additionalSourceId);
                    if (additionalSource == null) return null;

                    var additionalData = (PropSelect.Data) additionalSource.getData();
                    var collection = getPropertyFromSelection(additionalData.getSelectedOption(), binding.propertyPath);

                    if (collection instanceof Array<?> array) {
                        return Util.asList(array).stream()
                            .filter(AssetRef.class::isInstance)
                            .map(AssetRef.class::cast)
                            .filter(ref -> ref.itemId.equals(selectedItem))
                            .findFirst()
                            .orElse(null);
                    }
                }
                return null;
            };

            default -> Function.identity();
        };
    }

    private Object getPropertyFromSelection(String selectedName, String propertyPath) {
        // Find which asset type contains an item with this name
        for (var assetType : editor.metadata.assetTypes.values()) {
            var item = Util.asList(assetType.items).stream()
                .filter(i -> i.name.equals(selectedName))
                .findFirst();

            if (item.isPresent()) {
                return item.get().properties.get(propertyPath);
            }
        }
        return null;
    }

    private String extractRefName(Object ref) {
        if (ref instanceof AssetRef<?> assetRef) {
            // Look up the asset's name
            for (var type : editor.metadata.assetTypes.values()) {
                var item = type.findItem(assetRef.itemId);
                if (item.isPresent()) {
                    return item.get().name;
                }
            }
            return assetRef.itemId;
        }
//        if (ref instanceof ObjectMap<?, ?> map) {
//            var itemId = map.get("itemId");
//            if (itemId != null) return itemId.toString();
//        }
        return "Unknown";
    }
}
