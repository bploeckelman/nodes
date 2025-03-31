package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import lombok.RequiredArgsConstructor;
import net.bplo.nodes.editor.utils.PinType;
import net.bplo.nodes.imgui.FontAwesomeIcons;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiLayout;

import java.util.Optional;

public class Link extends EditorObject {

    public final Pin src;
    public final Pin dst;

    private final Appearance appearance;

    @RequiredArgsConstructor
    public enum Appearance {
          FLOW (5f, ImGuiColors.cyan.asVec4())
        , DATA (2f, ImGuiColors.yellow.asVec4())
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

        connect();
    }

    /**
     * Limited access constructor intended for use by {@link EditorSerializer}
     * to create {@link Link} instances from saved json data.
     */
    Link(long savedId, Pin src, Pin dst) {
        super(Type.LINK, savedId);
        this.src = src;
        this.dst = dst;

        var isFlow = (src.type == PinType.FLOW && dst.type == PinType.FLOW);
        this.appearance = isFlow ? Appearance.FLOW : Appearance.DATA;

        connect();
    }

    public void connect() {
        srcNode().ifPresent(node -> node.outgoingLinks.add(this));
        dstNode().ifPresent(node -> node.incomingLinks.add(this));
    }

    public void disconnect() {
        srcNode().ifPresent(node -> node.outgoingLinks.remove(this));
        dstNode().ifPresent(node -> node.incomingLinks.remove(this));
    }

    public Optional<Node> srcNode() {
        return src.attachment.getNode();
    }

    public Optional<Node> dstNode() {
        return dst.attachment.getNode();
    }

    @Override
    public void render() {
        NodeEditor.link(id, src.id, dst.id, appearance.color, appearance.thickness);
    }

    @Override
    public void renderContextMenu(Editor editor) {
        headerText("Link #%d".formatted(id));

        separatorText("Connects");
        ImGui.pushFont(EditorUtil.Fonts.icons);
        {
            // calculate text column size as max of src/dst node text widths
            var srcTextWidth = srcNode().map(node -> ImGui.calcTextSizeX(node.label())).orElse(0f);
            var dstTextWidth = dstNode().map(node -> ImGui.calcTextSizeX(node.label())).orElse(0f);
            var columnWidth  = Math.max(srcTextWidth, dstTextWidth);
            var nodeColor  = ImGuiColors.medYellow.asInt();
            var pinColor   = ImGuiColors.darkYellow.asInt();
            var arrowColor = ImGuiColors.white.asInt();

            // link source
            ImGuiLayout.beginColumn(columnWidth);
            {
                srcNode().ifPresent(srcNode -> ImGui.textColored(nodeColor, srcNode.label()));
                ImGui.textColored(pinColor, src.label());
            }
            // link arrow
            ImGuiLayout.nextColumn();
            {
                ImGui.textColored(arrowColor, FontAwesomeIcons.arrowRightLong);
                ImGui.textColored(arrowColor, FontAwesomeIcons.arrowRightLong);
            }
            // link destination
            ImGuiLayout.nextColumn(columnWidth);
            {
                dstNode().ifPresent(dstNode -> ImGui.textColored(nodeColor, dstNode.label()));
                ImGui.textColored(pinColor, dst.label());
            }
            ImGuiLayout.endColumn();
        }
        ImGui.popFont();

        separatorText("Actions");
        deleteButton(editor);

        ImGui.dummy(0, 4);
    }
}
