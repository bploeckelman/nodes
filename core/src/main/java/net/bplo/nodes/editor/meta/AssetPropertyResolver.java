package net.bplo.nodes.editor.meta;

import net.bplo.nodes.Util;

public class AssetPropertyResolver {

    private static final String TAG = AssetPropertyResolver.class.getSimpleName();

    public static Object resolveValue(String property, String linkPath) {
        // TODO(brian):
        //  - parse linkPath (eg. 'character.name'),
        //  - find referenced value in assets metadata,
        //  - return resolved value

        Util.log(TAG, "Unable to resolve value for property: %s, linkPath: %s".formatted(property, linkPath));
        return null;
    }
}
