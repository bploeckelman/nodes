package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.Prop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Metadata {

    private static final String TAG = Metadata.class.getSimpleName();

    public final String path;
    public final Map<String, AssetType> assetTypes = new HashMap<>();
    public final Map<String, NodeType>  nodeTypes = new HashMap<>();

    @SuppressWarnings("unchecked")
    public Metadata(String path) {
        this.path = path;
        Util.log(TAG, "Loading metadata: '%s'".formatted(path));

        var handle = Gdx.files.absolute(path);
        if (!handle.exists() || handle.isDirectory()) {
            throw new GdxRuntimeException("Invalid metadata file path: " + path);
        }

        var jsonStr = handle.readString();
        var root = (new JsonReader()).parse(jsonStr);

        var json = new Json();
        var assets = (Array<Metadata.AssetType>) json.readValue("assetTypes", Array.class, Metadata.AssetType.class, root);
        var nodes  = (Array<Metadata.NodeType>)  json.readValue("nodeTypes",  Array.class, Metadata.NodeType.class, root);

        for (var type : assets) {
            if (assetTypes.containsKey(type.id)) {
                Util.log(TAG, "*** Duplicate asset type id: '" + type.id + "', skipping.");
                continue;
            }
            assetTypes.put(type.id, type);
        }

        for (var type : nodes) {
            if (nodeTypes.containsKey(type.id)) {
                Util.log(TAG, "*** Duplicate node type id: '" + type.id + "', skipping.");
                continue;
            }
            nodeTypes.put(type.id, type);
        }

        Util.log(TAG, "Loaded %d asset types and %d node types".formatted(assetTypes.size(), nodeTypes.size()));
    }

    public Optional<AssetType> findAssetType(String assetTypeId) {
        return Optional.ofNullable(assetTypes.get(assetTypeId));
    }

    public Optional<NodeType> findNodeType(String nodeTypeId) {
        return Optional.ofNullable(nodeTypes.get(nodeTypeId));
    }

    public static class AssetType {
        public String id;
        public String name;
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
        public String typeId;
        public String id;
        public String name;
        public String path;
        public ObjectMap<String, Object> properties = new ObjectMap<>();
    }

    public static class AssetRef<T> {
        public final String typeId;
        public final String itemId;

        public static <T> AssetRef<T> of(String typeId, String itemId) {
            return new AssetRef<>(typeId, itemId);
        }

        private AssetRef(String typeId, String itemId) {
            this.typeId = typeId;
            this.itemId = itemId;
        }

        public String cacheKey() {
            return typeId + "." + itemId;
        }

        public Optional<T> resolve(AssetResolver resolver, Class<T> type) {
            return resolver.resolve(this, type);
        }

//        public Optional<AssetItem> resolve(MetadataRegistry registry) {
//            var item = registry.findAssetType(typeId)
//                .flatMap(assetType -> assetType.findItem(itemId));
//
//            if (item.isEmpty()) {
//                Util.log(TAG, "Invalid asset item: '" + itemId + "' in asset type: '" + typeId + "'");
//                return Optional.empty();
//            }
//
//            return item;
//        }
    }

    public static class NodeType {
        public String id;
        public String name;
        public int inputs;
        public int outputs;
        public Array<PropType> props = new Array<>();

        public Optional<PropType> findPropType(String id) {
            for (var prop : props) {
                if (prop.id.equals(id)) {
                    return Optional.of(prop);
                }
            }
            return Optional.empty();
        }
    }

    public static class PropType<T> {
        public String id;
        public String name;
        public Class<? extends Prop> propClass;
        public PropBinding<T> binding;
    }

    public static class PropBinding<T> {
        public String sourceId;
        public Function<Object, T> transformer;

        public static <T> PropBinding<T> create(String sourceId, Function<Object, T> transformer) {
            return new PropBinding<>(sourceId, transformer);
        }

        private PropBinding(String sourceId, Function<Object, T> transformer) {
            this.sourceId = sourceId;
            this.transformer = transformer;
        }
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
