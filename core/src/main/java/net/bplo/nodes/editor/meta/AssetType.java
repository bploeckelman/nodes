package net.bplo.nodes.editor.meta;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class AssetType {

    public String id;
    public String name;
    public String path;
    public Array<Entry> entries = new Array<>();

    public static class Entry extends ObjectMap<String, Object> {
        public Entry() {}
    }

    public AssetType() {}
}
