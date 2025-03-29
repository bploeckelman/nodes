package net.bplo.nodes.assets.framework;

import com.badlogic.gdx.utils.ObjectMap;
import net.bplo.nodes.assets.Assets;

public abstract class AssetContainer<T extends Enum<T> & AssetEnum<ResourceType>, ResourceType> {

    /**
     * Static reference to this asset container instance. This needs to be included
     * in each concrete {@link AssetContainer} implementation as well as setting
     * it in the constructor to allow for global access to each container instance:
     * <code><pre>
     * public class Things extends AssetContainer&lt;Things.Type, TextureRegion&gt; {
     *     public static AssetContainer<Things.Type, TextureRegion> container;
     *     public enum Type implements AssetEnum<TextureRegion> { ... }
     *     public Things() {
     *         super(Things.class, TextureRegion.class);
     *         Things.container = this;
     *     }
     *     &commat;Override
     *     public void init(Assets assets) {
     *         var atlas = assets.atlas;
     *         for (var type : Type.values()) {
     *             var region = atlas.findRegion(type.textureRegionName());
     *             // error checking...
     *             resources.put(type, region);
     *         }
     *     }
     * }</pre></code>
     */
    public static AssetContainer<?, ?> container;

    protected final String containerClassName;
    protected final Class<ResourceType> resourceTypeClass;
    protected final ObjectMap<T, ResourceType> resources;

    public AssetContainer(
        Class<? extends AssetContainer<T, ResourceType>> assetContainerClass,
        Class<ResourceType> resourceTypeClass
    ) {
        this.containerClassName = assetContainerClass.getSimpleName();
        this.resourceTypeClass = resourceTypeClass;
        this.resources = new ObjectMap<>();
    }

    /**
     * Optional: override to perform custom loading in {@link com.badlogic.gdx.assets.AssetManager},
     * intended to be called in {@link Assets} constructor for all {@link AssetContainer} instances.
     */
    public void load(Assets assets) { /* no-op by default */ }

    public abstract void init(Assets assets);
}
