package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static net.bplo.nodes.editor.meta.Metadata.AssetItem;
import static net.bplo.nodes.editor.meta.Metadata.AssetRef;

public class AssetResolver {

    private static final String TAG = AssetResolver.class.getSimpleName();

    private final Metadata metadata;
    private final Map<String, Object> cache;

    public AssetResolver(Metadata metadata) {
        this.metadata = metadata;
        this.cache = new HashMap<>();
    }

    public <T> Optional<T> resolve(AssetRef<T> ref, Class<T> type) {
        if (cache.containsKey(ref.cacheKey())) {
            var cached = cache.get(ref.cacheKey());
            return Optional.of(type.cast(cached));
        }

        return metadata.findAssetType(ref.typeId)
            .flatMap(assetType -> assetType.findItem(ref.itemId))
            .map(assetItem -> loadAsset(ref.cacheKey(), assetItem, type));
    }

    private <T> T loadAsset(String cacheKey, AssetItem item, Class<T> type) {
        Object asset = null;
        if      (type == Texture.class)   asset = loadTexture(item);
        else if (type == Animation.class) asset = loadAnimation(item);
        else if (type == ObjectMap.class) asset = new ObjectMap<>(item.properties);
        else if (type == AssetRef.class)  asset = AssetRef.of(item.id, item.name);
        else if (type == String.class)    asset = item.name;
        else Util.log(TAG, "Unsupported asset type: %s".formatted(type.getSimpleName()));
        if (asset != null) {
            cache.put(cacheKey, asset);
        }
        return type.cast(asset);
    }

    private Texture loadTexture(AssetItem item) {
        return new Texture(item.path);
    }

    @SuppressWarnings("unchecked")
    private Animation<Texture> loadAnimation(AssetItem item) {
        var frameTextures = new Array<Texture>();

        var frames = (Array<AssetItem>) item.properties.get("frames", new Array<AssetItem>(AssetItem.class));
        for (var frame : frames) {
            var frameTexture = loadTexture(frame);
            frameTextures.add(frameTexture);
        }

        if (frameTextures.size == 0) {
            return new Animation<>(1f / 30f, frameTextures);
        }

        // TODO(brian): return fallback animation instead
        throw new GdxRuntimeException("loadAnimation(): No frames found for asset item: '%s' (%s)"
            .formatted(item.name, item.id));
    }
}
