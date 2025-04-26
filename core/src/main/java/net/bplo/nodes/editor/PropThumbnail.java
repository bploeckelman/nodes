package net.bplo.nodes.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.meta.Metadata;

import java.util.Objects;

public class PropThumbnail extends Prop {

    private static final String TAG = PropThumbnail.class.getSimpleName();

    private boolean thumbnailVisible = true;
    private EditorWidget.Image image;

    // TODO(brian): set from metadata PropType by resolving 'display'
    public Metadata.AssetItemRef assetRef;

    public PropThumbnail(Node node) {
        super(node);
    }

    PropThumbnail(long savedId, Node node) {
        super(savedId, node);
    }

    @Override
    public String defaultName() {
        return "Thumbnail";
    }

    @Override
    public Object getData() {
        return assetRef;
    }

    @Override
    public void setData(Json json, JsonValue dataValue) {

    }

    @Override
    public void render() {
        if (image == null && assetRef != null) {
            // try to resolve the referenced asset and use it to create the thumbnail image
            editor.metadataRegistry.findAssetType(assetRef.type)
                .ifPresent(assetType -> {
                    assetType.findItem(assetRef.id)
                        .ifPresent(assetItem -> {
                            // TODO(brian): resolve through Assets in order to handle caching
                            var metadataRootPath = Gdx.files.absolute(editor.metadataRegistry.filePath).parent().path();
                            var assetTypeBasePath = Objects.requireNonNullElse(assetType.basePath, ".");
                            var assetPath = metadataRootPath + "/" + assetTypeBasePath + "/" + assetItem.path;
                            var texture = new Texture(Gdx.files.absolute(assetPath));
                            // TODO(brian): allow override of thumbnail size for property
                            image = EditorWidget.Image.from(texture);
                            Util.log(TAG, "Loaded texture from asset reference: " + assetPath);
                        });
                });
        }

        ImGui.beginGroup();
        {
            var label = name + "##" + label();
            if (ImGui.checkbox(label, thumbnailVisible)) {
                thumbnailVisible = !thumbnailVisible;
            }
            if (thumbnailVisible && image != null) {
                var cursorPos = ImGui.getCursorPos();
                ImGui.setCursorPosX(cursorPos.x + (node.width - image.size().x) / 2f);
                ImGui.image(image.id(), image.size(), image.uv1(), image.uv2());
            }
        }
        ImGui.endGroup();
        bounds.update();
    }
}
