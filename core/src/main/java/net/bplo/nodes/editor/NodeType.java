package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Array;

public class NodeType {
    public String id;
    public String name;
    public int inputs;
    public int outputs;
    public Array<PropType> propTypes = new Array<>();

    public static class PropType {
        public String type;
        public String id;
        public String name;
        public String category;
        public String linkTo;
    }
}
