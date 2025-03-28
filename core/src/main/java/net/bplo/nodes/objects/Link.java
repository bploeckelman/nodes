package net.bplo.nodes.objects;

import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.EditorUtil;

public class Link extends EditorObject {

    public final Pin src;
    public final Pin dst;

    public Link(Pin src, Pin dst) {
        super(Type.LINK);
        this.src = src;
        this.dst = dst;
    }

    @Override
    public void render() {
        var color = EditorUtil.Colors.linkPath;
        // TODO(brian): use different colors for different link types
        NodeEditor.link(id, src.id, dst.id, color, 3f);
    }
}
