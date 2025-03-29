package net.bplo.nodes.assets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import lombok.RequiredArgsConstructor;
import net.bplo.nodes.assets.framework.AssetContainer;
import net.bplo.nodes.assets.framework.AssetEnum;

public class Icons extends AssetContainer<Icons.Type, TextureRegion> {

    public static ObjectMap<Type, TextureRegion> container;

    @RequiredArgsConstructor
    public enum Type implements AssetEnum<TextureRegion> {
          PIN_FLOW_LINKED ("icons/pin-flow-linked")
        , PIN_FLOW        ("icons/pin-flow")
        , PIN_DATA_LINKED ("icons/pin-data-linked")
        , PIN_DATA        ("icons/pin-data")
        , PIN_DATA_INPUT  ("icons/pin-data-input")
        , PIN_DATA_OUTPUT ("icons/pin-data-output")
        ;
        private final String path;

        @Override
        public TextureRegion get() {
            return container.get(this);
        }
    }

    public Icons() {
        super(Icons.class, TextureRegion.class);
        Icons.container = resources;
    }

    @Override
    public void init(Assets assets) {
        var atlas = assets.atlas();
        for (var type : Type.values()) {
            var region = atlas.findRegion(type.path);
            if (region == null) {
                throw new GdxRuntimeException("Missing icon: " + type.path);
            }
            resources.put(type, region);
        }
    }
}
