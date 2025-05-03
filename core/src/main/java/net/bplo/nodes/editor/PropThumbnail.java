package net.bplo.nodes.editor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import net.bplo.nodes.editor.meta.Metadata;

public class PropThumbnail extends Prop {

    private static final String TAG = PropThumbnail.class.getSimpleName();

    public boolean thumbnailVisible = true;
    private EditorWidget.Image image;

    public Metadata.AssetRef<Texture> assetRef;

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
            // TODO(brian): allow override of thumbnail size for property
            assetRef.resolve(editor.assetResolver, Texture.class)
                .ifPresent(texture -> image = EditorWidget.Image.from(texture));
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

    public void clearImage() {
        if (image == null) return;
        image = null;
    }
}
