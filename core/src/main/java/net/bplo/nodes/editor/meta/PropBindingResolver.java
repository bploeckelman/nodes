package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.Node;
import net.bplo.nodes.editor.Prop;
import net.bplo.nodes.editor.PropInputText;
import net.bplo.nodes.editor.PropSelect;
import net.bplo.nodes.editor.PropThumbnail;

import java.util.HashMap;

public class PropBindingResolver {

    private static final String TAG = PropBindingResolver.class.getSimpleName();

    private final AssetResolver assetResolver;

    public PropBindingResolver(AssetResolver assetResolver) {
        this.assetResolver = assetResolver;
    }

    public void resolveBindings(Node node, Array<Metadata.PropType<?>> propTypes) {
        // collect props
        var propMap = new HashMap<String, Prop>();
        for (var prop : node.props) {
            propMap.put(prop.propTypeId, prop);
        }

        // setup bindings
        for (var propType : propTypes) {
            if (propType.binding == null) continue;

            var targetProp = propMap.get(propType.id);
            var sourceProp = propMap.get(propType.binding.sourceId);

            if (targetProp == null || sourceProp == null) {
                Util.log(TAG, "Failed to resolve prop binding: %s -> %s".formatted(propType.id, propType.binding.sourceId));
                continue;
            }

            // set change listener
            sourceProp.onChange = newValue -> {
                if (propType.binding.transformer != null) {
                    var transformed = propType.binding.transformer.apply(newValue);
                    applyTransformedValue(targetProp, transformed);
                }
            };

            // apply initial value
            if (propType.binding.transformer != null) {
                var initialValue = propType.binding.transformer.apply(sourceProp.getData());
                applyTransformedValue(targetProp, initialValue);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void applyTransformedValue(Prop targetProp, Object value) {
        if (targetProp instanceof PropThumbnail thumbnail && value instanceof Metadata.AssetRef<?> assetRef) {
            thumbnail.assetRef = (Metadata.AssetRef<Texture>) assetRef;
            thumbnail.clearImage();
        } else if (targetProp instanceof PropSelect select && value instanceof String[] strings) {
            var selectData = (PropSelect.Data) select.getData();
            selectData.options = strings;
            selectData.selectedIndex = selectData.options.length > 0 ? 0 : -1;
        } else if (targetProp instanceof PropInputText inputText && value instanceof String string) {
            inputText.setText(string);
        }
        // TODO(brian): add more handlers as needed
    }
}
