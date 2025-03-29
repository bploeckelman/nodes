package net.bplo.nodes.assets;

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
import net.bplo.nodes.assets.framework.AssetContainer;

import java.util.ArrayList;
import java.util.List;

public class Assets implements Disposable {

    public final ObjectMap<Class<? extends AssetContainer<?, ?>>, AssetContainer<?, ?>> containers;

    public final List<Disposable> disposables;
    public final AssetManager mgr;

    public SpriteBatch batch;
    public GlyphLayout layout;
    public TextureAtlas atlas;

    public Texture pixel;
    public TextureRegion pixelRegion;
    public TextureRegion gdx;

    public Assets() {
        containers = new ObjectMap<>();
        containers.put(Icons.class, new Icons());

        disposables = new ArrayList<>();

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

        mgr = new AssetManager();
        batch = new SpriteBatch();
        layout = new GlyphLayout();
        disposables.add(mgr);
        disposables.add(batch);

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

    @Override
    public void dispose() {
        disposables.forEach(Disposable::dispose);
    }
}
