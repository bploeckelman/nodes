package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import net.bplo.nodes.editor.NodeType;

public class AssetMetadata {

    public static final String TAG = AssetMetadata.class.getSimpleName();

    public String filePath;

    public Array<AssetRoot> assetRoots = new Array<>();
    public Array<AssetType> assetTypes = new Array<>();
    public Array<NodeType>  nodeTypes  = new Array<>();

    @SuppressWarnings("unchecked")
    public static AssetMetadata load(FileHandle fileHandle) {
        var metadata = new AssetMetadata();
        metadata.filePath = fileHandle.path();

        var fileJson = fileHandle.readString();
        var jsonRoot = (new JsonReader()).parse(fileJson);

        var json = new Json();
        metadata.assetRoots = (Array<AssetRoot>) json.readValue("assetRoots", Array.class, AssetRoot.class, jsonRoot);
        metadata.assetTypes = (Array<AssetType>) json.readValue("assetTypes", Array.class, AssetType.class, jsonRoot);
        metadata.nodeTypes  = (Array<NodeType>)  json.readValue("nodeTypes", Array.class, NodeType.class, jsonRoot);

        return metadata;
    }
}
