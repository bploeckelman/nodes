package net.bplo.nodes.objects;

import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import lombok.RequiredArgsConstructor;
import net.bplo.nodes.editor.EditorObject;
import net.bplo.nodes.editor.EditorUtil;
import net.bplo.nodes.objects.utils.PinType;

public class Link extends EditorObject {

    public final Pin src;
    public final Pin dst;

    private final Appearance appearance;

    @RequiredArgsConstructor
    public enum Appearance {
          FLOW (5f, EditorUtil.Colors.Vec4.cyan)
        , DATA (2f, EditorUtil.Colors.Vec4.yellow)
        ;
        public final float thickness;
        public final ImVec4 color;
    }

    public Link(Pin src, Pin dst) {
        super(Type.LINK);
        this.src = src;
        this.dst = dst;

        var isFlow = (src.type == PinType.FLOW && dst.type == PinType.FLOW);
        this.appearance = isFlow ? Appearance.FLOW : Appearance.DATA;
    }

    @Override
    public void render() {
        NodeEditor.link(id, src.id, dst.id, appearance.color, appearance.thickness);
    }
}
