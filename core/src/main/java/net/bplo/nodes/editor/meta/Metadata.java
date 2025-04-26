package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Metadata {

    private static final String TAG = Metadata.class.getSimpleName();

    public static class AssetType {
        public String id;
        public String name;
        public String basePath;
        public Array<AssetItem> items = new Array<>();

        public Optional<AssetItem> findItem(String itemId) {
            for (var item : items) {
                if (item.id.equals(itemId)) {
                    return Optional.of(item);
                }
            }
            return Optional.empty();
        }

        public <T> List<T> getItemFieldValues(String fieldName, Class<T> fieldType) {
            return Util.asList(items).stream()
                .map(item -> {
                    var fieldValue = switch (fieldName) {
                        case "id"   -> item.id;
                        case "name" -> item.name;
                        case "path" -> item.path;
                        // if fieldName isn't one of the explicit fields,
                        // try to find it at the top level of item properties
                        default -> item.properties.get(fieldName);
                    };
                    // validate that the field was found
                    if (fieldValue == null) {
                        Util.log(TAG, "Unknown asset item field: '%s' in asset type: '%s'".formatted(fieldName, id));
                        return null;
                    }
                    // validate that the field is the expected type
                    if (!fieldType.isAssignableFrom(fieldValue.getClass())) {
                        Util.log(TAG, "Invalid asset item field type: '%s' in asset type: '%s'".formatted(fieldValue.getClass(), id));
                        return null;
                    }
                    return fieldType.cast(fieldValue);
                })
                .filter(Objects::nonNull)
                .toList();
        }
    }

    public static class AssetItem {
        public String id;
        public String name;
        public String path;
        public ObjectMap<String, Object> properties = new ObjectMap<>();
    }

    public static class AssetItemRef {
        public String type;
        public String id;

        public Optional<AssetItem> resolve(MetadataRegistry registry) {
            var item = registry.findAssetType(type)
                .flatMap(assetType -> assetType.findItem(id));

            if (item.isEmpty()) {
                Util.log(TAG, "Invalid asset item: '" + id + "' in asset type: '" + type + "'");
                return Optional.empty();
            }

            return item;
        }
    }

    public static class NodeType {
        public String id;
        public String name;
        public int inputs;
        public int outputs;
        public Array<PropType> props = new Array<>();
    }

    public static class PropType {
        public String type;
        public String id;
        public String name;
        public String assetType;
        public String display;
    }

    public static class Display {
        public String type;
        public String field;

        public Display() {}

        public Display(String rawValue) {
            var parts = rawValue.split("\\.");
            if (parts.length < 2) {
                Util.log(TAG, "Invalid display string: " + rawValue);
            } else {
                this.type  = parts[0];
                this.field = parts[1];
            }
        }
    }
}
