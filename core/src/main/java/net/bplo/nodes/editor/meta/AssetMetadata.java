package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.NodeType;

import java.nio.file.Path;

public class AssetMetadata {

    public static final String TAG = AssetMetadata.class.getSimpleName();

    public String filePath;

    public Array<AssetType> assetTypes = new Array<>();
    public Array<NodeType>  nodeTypes  = new Array<>();

    public record Asset(Object value, Class<?> clazz, Class<?> elementClass) {
        public Asset(Object value, Class<?> clazz) {
            this(value, clazz, null);
        }
    }

    private final ObjectMap<String, Asset> assetsByEntryName = new ObjectMap<>();
    private final ObjectMap<String, AssetType> assetTypesById = new ObjectMap<>();
    private final ObjectMap<AssetType, ObjectMap<String, AssetType.Entry>> assetEntriesByType = new ObjectMap<>();

    @SuppressWarnings("unchecked")
    public static AssetMetadata load(FileHandle fileHandle) {
        var metadata = new AssetMetadata();
        metadata.filePath = fileHandle.path();

        var fileJson = fileHandle.readString();
        var jsonRoot = (new JsonReader()).parse(fileJson);

        var json = new Json();
        metadata.assetTypes = (Array<AssetType>) json.readValue("assetTypes", Array.class, AssetType.class, jsonRoot);
        metadata.nodeTypes  = (Array<NodeType>)  json.readValue("nodeTypes", Array.class, NodeType.class, jsonRoot);

        // TODO(brian): should probably validate uniqueness of ids here too
        for (var assetType : metadata.assetTypes) {
            metadata.assetTypesById.put(assetType.id, assetType);

            var entriesById = new ObjectMap<String, AssetType.Entry>();
            for (var entry : assetType.entries) {
                entriesById.put(entry.id(), entry);
            }
            metadata.assetEntriesByType.put(assetType, entriesById);
        }

        return metadata;
    }

    public String assetTypesStr() {
        var sb = new StringBuilder();
        sb.append("[");
        for (var type : assetTypes) {
            sb.append(type.id);
            sb.append(", ");
        }
        if (sb.length() > 2) {
            // remove trailing comma and space
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
        // T_T gdx collection types don't play nice with streams out of the box
        //return Arrays.stream(assetTypes.items)
        //    .map(type -> type.id)
        //    .collect(Collectors.joining(", ", "[", "]"));
    }

    public String nodeTypesStr() {
        var sb = new StringBuilder();
        sb.append("[");
        for (var type : nodeTypes) {
            sb.append(type.id);
            sb.append(", ");
        }
        if (sb.length() > 2) {
            // remove trailing comma and space
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();

        // T_T gdx collection types don't play nice with streams out of the box
        //return Arrays.stream(nodeTypes.items)
        //    .map(type -> type.id)
        //    .collect(Collectors.joining(", ", "[", "]"));
    }

    public Asset resolveAssetEntry(String entryName) {
        // check the cache first
        var asset = assetsByEntryName.get(entryName);
        if (asset != null) {
            return asset;
        }

        // NOTE: 'assetEntry' syntax: "{assetTypeId}.{assetEntryId}"
        //  eg. '{characters.portrait', 'textures.tex-name', ...
        var parts = entryName.split("\\.");
        if (parts.length != 2) {
            Util.log(TAG, "Invalid asset entry name: " + entryName);
            return null;
        }

        var assetTypeId = parts[0];
        var entryId = parts[1];

        // NOTE: 'assetEntry' has an alternative syntax: "{assetType}[].{assetEntryFieldName}"
        //  where '[]' after the 'assetType' indicates that it's not referencing a single entry,
        //  instead it indicates that we want all values for a specific field _across all entries_
        //  for that asset type. This is useful for things like getting the names of all characters
        //  so that they can be used in a select property.
        // TODO(brian): might be worth implementing a proper uri scheme for 'assetEntry' values at some point
        var isFieldRef = assetTypeId.endsWith("[]");
        if (isFieldRef) {
            assetTypeId = assetTypeId.replace("[]", "");
        }

        // lookup the specified asset type and it's entries
        var assetType = assetTypesById.get(assetTypeId);
        if (assetType == null) {
            Util.log(TAG, "Unable to resolve asset type: " + assetTypeId);
            return null;
        }
        var entriesById = assetEntriesByType.get(assetType);
        if (entriesById == null) {
            Util.log(TAG, "Unable to resolve entries for asset type: " + assetTypeId);
            return null;
        }

        // special asset entry, resolves values from a field across all entries for the given asset type
        if (isFieldRef) {
            Class<?> fieldType = null;
            var fieldValues = new Array<>();
            for (var entry : entriesById.values()) {
                // get the field value for the entry
                var fieldValue = entry.get(entryId);
                if (fieldValue == null) {
                    Util.log(TAG, "Unable to resolve field value for asset entry: " + entry);
                    continue;
                }

                // store and validate the field type
                if (fieldType == null) {
                    fieldType = fieldValue.getClass();
                } else if (!fieldType.isInstance(fieldValue)) {
                    Util.log(TAG, "Field value type mismatch for asset entry: " + entry);
                    continue;
                }

                fieldValues.add(fieldValue);
            }

            asset = new Asset(fieldValues, Array.class, fieldType);
            assetsByEntryName.put(entryName, asset);

            Util.log(TAG, "Resolved asset entry: " + entryName + " -> " + asset.value);
            return asset;
        }

        // normal asset entry, resolved by entry id against a single entry for the asset type
        var assetEntry = entriesById.get(entryId);
        if (assetEntry == null) {
            Util.log(TAG, "Unable to resolve asset entry by id: " + entryId);
            return null;
        }

        asset = switch (assetTypeId) {
            case "textures"   -> new Asset(getTexture(assetType, assetEntry), Texture.class);
            case "animations" -> new Asset(getAnimation(assetType, assetEntry), Animation.class);
            case "characters" -> new Asset(getMap(assetType, assetEntry), ObjectMap.class);
            default -> null;
        };
        if (asset == null) {
            Util.log(TAG, "Unable to resolve asset entry: " + entryName);
            return null;
        }
        assetsByEntryName.put(entryName, asset);

        Util.log(TAG, "Resolved asset entry: " + entryName + " -> " + asset.value);
        return asset;
    }

    private Texture getTexture(AssetType type, AssetType.Entry entry) {
        var entryPath = entry.get("path");
        if (!(entryPath instanceof String)) {
            Util.log(TAG, "Invalid path for texture asset entry: " + entry);
            return null;
        }

        var pathStr = type.path + "/" + entryPath;
        var path = Path.of(pathStr).toAbsolutePath().toString();
        var fileHandle = Gdx.files.absolute(path);
        if (!fileHandle.exists() || fileHandle.isDirectory()) {
            Util.log(TAG, "Texture does not exist or is not a file: " + path);
        }
        return new Texture(fileHandle);
    }

    private Animation<Texture> getAnimation(AssetType type, AssetType.Entry entry) {
        var entryPath = entry.get("path");
        if (!(entryPath instanceof String)) {
            Util.log(TAG, "Invalid path for animation asset entry: " + entry);
            return null;
        }

        var pathStr = type.path + "/" + entryPath;
        var path = Path.of(pathStr).toAbsolutePath().toString();

        // TODO(brian): find files with the entryPath prefix and a suffix of "_<index>.png"
        //  load them all as regions and create an animation instance
        var frames = new Array<Texture>();
        Util.log(TAG, "*** TEMP: loading fallback animation asset");
        frames.add(new Texture("libgdx.png"));
        return new Animation<>(1f, frames);
    }

    private String[] getStrings(AssetType type, AssetType.Entry entry) {
        Util.log(TAG, "*** TEMP: loading fallback strings asset");
        return new String[] { "temp1", "temp2", "temp3" };
    }

    private ObjectMap<String, Object> getMap(AssetType type, AssetType.Entry entry) {
        return new ObjectMap<>(entry);
    }
}
