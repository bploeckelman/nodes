package net.bplo.nodes.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AssetCache {

    private final Map<String, Object> assets = new HashMap<>();

    public <T> T getAsset(String path, Class<T> type) {
        // Load and cache assets by path
        if (!assets.containsKey(path)) {
            assets.put(path, loadAsset(path, type));
        }
        return type.cast(assets.get(path));
    }

    private <T> Optional<T> loadAsset(String path, Class<T> type) {
        // Type-specific asset loading
        if (type == Texture.class) {
            return Optional.of(type.cast(new Texture(Gdx.files.internal(path))));
        }
        // TODO(brian): handle other asset types
        return Optional.empty();
    }
}
