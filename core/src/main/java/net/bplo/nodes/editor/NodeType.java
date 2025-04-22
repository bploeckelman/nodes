package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Array;

public class NodeType {
    public String id;
    public String name;
    public int inputs;
    public int outputs;
    public Array<PropDef> props = new Array<>();

    public static class PropDef {
        public String type;
        public String id;
        public String name;
        public String assetEntry;
    }
}
