package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;

import java.util.Optional;

public class Metadata {

    private static final String TAG = Metadata.class.getSimpleName();

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
}
