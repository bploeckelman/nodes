package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.utils.GdxRuntimeException;

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
        throw new GdxRuntimeException("Not yet implemented");
    }
}
