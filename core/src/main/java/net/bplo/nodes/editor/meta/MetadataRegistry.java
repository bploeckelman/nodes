package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MetadataRegistry {

    private static final String TAG = MetadataRegistry.class.getSimpleName();

    public final String filePath;

    private final ObjectMap<String, Metadata.AssetType> assetTypesById = new ObjectMap<>();
    private final ObjectMap<String, Metadata.NodeType> nodeTypesById = new ObjectMap<>();

    public MetadataRegistry(String filePath) {
        this.filePath = filePath;

        var handle = Gdx.files.absolute(filePath);
        if (!handle.exists() || handle.isDirectory()) {
            throw new GdxRuntimeException("Invalid metadata file path: " + filePath);
        }
        load(handle);
    }

    public List<Metadata.NodeType> getNodeTypes() {
        var nodeTypes = new ArrayList<Metadata.NodeType>();
        for (var nodeType : nodeTypesById.values()) {
            nodeTypes.add(nodeType);
        }
        return nodeTypes;
    }

    public Optional<Metadata.AssetType> findAssetType(String id) {
        return Optional.of(assetTypesById.get(id));
    }

    public Optional<Metadata.NodeType> findNodeType(String id) {
        return Optional.of(nodeTypesById.get(id));
    }

    @SuppressWarnings("unchecked")
    private void load(FileHandle handle) {
        var json = new Json();
        var reader = new JsonReader();
        var jsonStr = handle.readString();
        var root = reader.parse(jsonStr);

        var assetTypes = (Array<Metadata.AssetType>) json.readValue("assetTypes", Array.class, Metadata.AssetType.class, root);
        var nodeTypes  = (Array<Metadata.NodeType>)  json.readValue("nodeTypes",  Array.class, Metadata.NodeType.class, root);

        for (var assetType : assetTypes) {
            if (assetTypesById.containsKey(assetType.id)) {
                Util.log(TAG, "*** Duplicate asset type id: '" + assetType.id + "', skipping.");
                continue;
            }
            assetTypesById.put(assetType.id, assetType);
        }

        for (var nodeType : nodeTypes) {
            if (nodeTypesById.containsKey(nodeType.id)) {
                Util.log(TAG, "*** Duplicate node type id: '" + nodeType.id + "', skipping.");
                continue;
            }
            nodeTypesById.put(nodeType.id, nodeType);
        }

        Util.log(TAG, "Loaded metadata from file: %s".formatted(filePath));
        Util.log(TAG, "- %d asset types".formatted(assetTypes.size));
        Util.log(TAG, "- %d node types".formatted(nodeTypes.size));
    }
}
