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
import java.util.Map;
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
        var assets = (Array<AssetType>) json.readValue("assetTypes", Array.class, AssetType.class, root);
        var nodes  = (Array<NodeType>)  json.readValue("nodeTypes",  Array.class, NodeType.class, root);

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
    }

    public static class AssetItem {
        public String typeId;
        public String id;
        public String name;
        public String path;
        public ObjectMap<String, Object> properties = new ObjectMap<>();
    }

    public static class AssetRef<T> {
        public String typeId;
        public String itemId;

        public static <T> AssetRef<T> of(String typeId, String itemId) {
            return new AssetRef<>(typeId, itemId);
        }

        public AssetRef() {}

        public AssetRef(String typeId, String itemId) {
            this.typeId = typeId;
            this.itemId = itemId;
        }

        public String cacheKey() {
            return typeId + "." + itemId;
        }

        public Optional<T> resolve(AssetResolver resolver, Class<T> type) {
            return resolver.resolve(this, type);
        }
    }

    public static class NodeType {
        public String id;
        public String name;
        public int inputs;
        public int outputs;
        public Array<PropType<?>> props = new Array<>();

        public Optional<PropType<?>> findPropType(String id) {
            for (var prop : props) {
                if (prop.id.equals(id)) {
                    return Optional.of(prop);
                }
            }
            return Optional.empty();
        }
    }

    public static class PropType<T> {
        public String propClass;
        public String id;
        public String name;
        public String assetType;
        public PropBinding<T> binding;

        @SuppressWarnings("unchecked")
        public Class<? extends Prop> getPropClass() throws ClassNotFoundException {
            return (Class<? extends Prop>) Class.forName(propClass);
        }
    }

    public static class PropBinding<T> {
        public String sourceId;
        public Function<Object, T> transformer;

        public static <T> PropBinding<T> create(String sourceId, Function<Object, T> transformer) {
            return new PropBinding<>(sourceId, transformer);
        }

        public PropBinding() {}

        private PropBinding(String sourceId, Function<Object, T> transformer) {
            this.sourceId = sourceId;
            this.transformer = transformer;
        }
    }
}
