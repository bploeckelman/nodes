package net.bplo.nodes.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader.FreeTypeFontLoaderParameter;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.Util;
import net.bplo.nodes.assets.framework.AssetContainer;
import net.bplo.nodes.editor.Editor;
import net.bplo.nodes.editor.meta.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Assets implements Disposable {

    private static final String TAG = Assets.class.getSimpleName();
    public static final String PREFERENCES_NAME = "net.bplo.nodes";

    public final ObjectMap<Class<? extends AssetContainer<?, ?>>, AssetContainer<?, ?>> containers;
    public final ObjectMap<String, Object> cache = new ObjectMap<>();

    public final List<Disposable> disposables;
    public final AssetManager mgr;
    public final Preferences prefs;

    public SpriteBatch batch;
    public GlyphLayout layout;
    public TextureAtlas atlas;

    public Texture pixel;
    public TextureRegion pixelRegion;
    public TextureRegion gdx;

    public Assets() {
        containers = new ObjectMap<>();
        containers.put(Icons.class, new Icons());

        prefs = Gdx.app.getPreferences(PREFERENCES_NAME);
        disposables = new ArrayList<>();
        mgr = new AssetManager();
        batch = new SpriteBatch();
        layout = new GlyphLayout();
        disposables.add(mgr);
        disposables.add(batch);

        // create a single pixel texture and associated region
        var pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        {
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(0, 0);
            pixmap.drawPixel(1, 0);
            pixmap.drawPixel(0, 1);
            pixmap.drawPixel(1, 1);

            pixel = new Texture(pixmap);
            pixelRegion = new TextureRegion(pixel);
        }
        disposables.add(pixmap);
        disposables.add(pixel);

        // setup asset manager to support ttf/otf fonts
        var internalFileResolver = new InternalFileHandleResolver();
        var fontLoader = new FreetypeFontLoader(internalFileResolver);
        mgr.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(internalFileResolver));
        mgr.setLoader(BitmapFont.class, ".ttf", fontLoader);
        mgr.setLoader(BitmapFont.class, ".otf", fontLoader);

        // populate the asset manager
        {
            mgr.load("sprites/sprites.atlas", TextureAtlas.class);

            for (var container : containers.values()) {
                container.load(this);
            }

            loadFont("fonts/chevyray-rise.ttf");
            loadFont("fonts/chevyray-roundabout.ttf");
            loadFont("fonts/cousine-regular.ttf");
            loadFont("fonts/droid-sans.ttf");
            loadFont("fonts/fa-regular-400.ttf");
            loadFont("fonts/fa-solid-900.ttf");
            loadFont("fonts/fa-solid-900-v6.ttf");
            loadFont("fonts/noto-sans-cjk-jp-medium.otf");
            loadFont("fonts/play-regular.ttf");
            loadFont("fonts/tahoma.ttf");
        }
        mgr.finishLoading();

        // initialize containers and fetch asset references
        {
            atlas = mgr.get("sprites/sprites.atlas", TextureAtlas.class);

            gdx = atlas.findRegion("libgdx");

            for (var container : containers.values()) {
                container.init(this);
            }
        }
    }

    private final FreeTypeFontLoaderParameter fontLoaderParams = new FreeTypeFontLoaderParameter() {{
        fontParameters.size = 20;
        fontParameters.color = Color.WHITE;
        fontParameters.borderWidth = 1;
        fontParameters.borderColor = Color.BLACK;
        fontParameters.shadowOffsetX = 0;
        fontParameters.shadowOffsetY = 0;
        fontParameters.shadowColor = new Color(0, 0, 0, 0.75f);
        fontParameters.genMipMaps = true;
        fontParameters.minFilter = Texture.TextureFilter.Linear;
        fontParameters.magFilter = Texture.TextureFilter.Linear;
    }};

    private void loadFont(String fileName) {
        fontLoaderParams.fontFileName = fileName;
        mgr.load(fileName, BitmapFont.class, fontLoaderParams);
    }

    public AssetManager mgr() {
        return mgr;
    }

    public TextureAtlas atlas() {
        return atlas;
    }

    public <T> Optional<T> resolveAssetRef(Editor editor, Metadata.AssetItemRef assetRef, Class<T> type) {
        var assetType = editor.metadataRegistry.findAssetType(assetRef.type);
        if (assetType.isEmpty()) {
            Util.log(TAG, "Unknown asset type: " + assetRef.type);
            return Optional.empty();
        }

        var assetItem = assetType.get().findItem(assetRef.id);
        if (assetItem.isEmpty()) {
            Util.log(TAG, "Unknown asset item: " + assetRef.id);
            return Optional.empty();
        }

        var metadataRootPath = Gdx.files.absolute(editor.metadataRegistry.filePath).parent().path();
        var assetTypeBasePath = Objects.requireNonNullElse(assetType.get().basePath, ".");
        var assetPath = metadataRootPath + "/" + assetTypeBasePath + "/" + assetItem.get().path;

        T asset = null;
        if (type == Texture.class) {
            var texture = cache.get(assetRef.cacheKey());
            if (texture == null) {
                texture = new Texture(Gdx.files.absolute(assetPath));
                Util.log(TAG, "Cache miss for %s asset '%s', adding to cache: %s"
                    .formatted(type.getSimpleName(), assetRef.cacheKey(), assetPath));
                cache.put(assetRef.cacheKey(), texture);
            } else {
                Util.log(TAG, "Cache hit for %s asset '%s'".formatted(type.getSimpleName(), assetRef.cacheKey()));
            }
            asset = type.cast(texture);
        } else if (type == String.class) {
            var string = cache.get(assetRef.cacheKey());
            if (string == null) {
                string = assetRef.resolve(editor.metadataRegistry)
                    .map(item -> item.name)
                    .orElse("");
                Util.log(TAG, "Cache miss for %s asset '%s', adding to cache: %s"
                    .formatted(type.getSimpleName(), assetRef.cacheKey(), assetPath));
                cache.put(assetRef.cacheKey(), string);
            } else {
                Util.log(TAG, "Cache hit for %s asset '%s'".formatted(type.getSimpleName(), assetRef.cacheKey()));
            }
            asset = type.cast(string);
        } else {
            Util.log(TAG, "Unsupported asset type: " + type);
        }

        return Optional.ofNullable(asset);
    }

    public void clearCache() {
        for (var item : cache.values()) {
            if (item instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        cache.clear();
    }

    @Override
    public void dispose() {
        disposables.forEach(Disposable::dispose);
        clearCache();
    }
}
