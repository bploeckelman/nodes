package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import lombok.RequiredArgsConstructor;
import net.bplo.nodes.EditorUtil;
import net.bplo.nodes.imgui.FontAwesomeIcons;

public class Pin extends EditorObject {

    public enum PinType { FLOW, DATA }

    @RequiredArgsConstructor
    public enum PinKind {
          INPUT (NodeEditorPinKind.Input)
        , OUTPUT(NodeEditorPinKind.Output);
        public final int value;
    }

    public sealed interface Attachment permits Attachment.NodeType, Attachment.PropertyType {
        record NodeType(Node node)                 implements Attachment {}
        record PropertyType(NodeProperty property) implements Attachment {}
    }

    public record Compatibility(boolean compatible, String message) {

        public static Compatibility ok() {
            return new Compatibility(true, "");
        }

        public static Compatibility reject(String message) {
            return new Compatibility(false, message);
        }

        public boolean incompatible() {
            return !compatible;
        }
    }

    public final PinKind kind;
    public final PinType type;
    public final Attachment attachment;

    public Pin(PinKind kind, PinType type, Node node) {
        this(kind, type, new Attachment.NodeType(node));
    }

    public Pin(PinKind kind, PinType type, NodeProperty property) {
        this(kind, type, new Attachment.PropertyType(property));
    }

    private Pin(PinKind kind, PinType type, Attachment attachment) {
        super(Type.PIN);
        this.kind = kind;
        this.type = type;
        this.attachment = attachment;
    }

    public static boolean isInput(Pin pin) { return pin.kind == PinKind.INPUT; }

    public static boolean isOutput(Pin pin) { return pin.kind == PinKind.OUTPUT; }

    // TODO(brian): needs to be refined
    public Compatibility canLinkTo(Pin dst) {
        var src = this;

        // check for incompatible states
        var isSelfLink = (src == dst);
        var isSameKind = (src.kind == dst.kind);
        var isDiffType = (src.type != dst.type);
//        var isSameNode = (src.node.id == dst.node.id);
//        var isLinkedTo = src.node.linkedTo(dst.node);

        if      (isSelfLink) return Compatibility.reject("Cannot link pin to itself");
        else if (isSameKind) return Compatibility.reject("Incompatible pin kinds, in/out");
        else if (isDiffType) return Compatibility.reject("Incompatible pin types");
//        else if (isSameNode) return Compatibility.reject("Cannot link pins in same node");
//        else if (isLinkedTo) return Compatibility.reject("Cannot link pins already linked");

        return Compatibility.ok();
    }

    @Override
    public void render() {
        NodeEditor.beginPin(id, kind.value);
        ImGui.pushID(id);
        ImGui.beginGroup();

//        var isLinked = NodeEditor.pinHadAnyLinks(id);
//        var iconType = switch (type) {
//            case FLOW -> isLinked ? Icons.Type.FLOW_CONNECTED : Icons.Type.FLOW_DISCONNECTED;
//            case DATA -> isLinked ? Icons.Type.FLIP_FULL : Icons.Type.TOKEN_IN;
//        };

//        var iconTex = Icons.container.get(iconType);
//        var iconSize = ICON_SIZE; // TODO(brian): base on `type`
//        var icon = ImGuiImage.from(iconTex, iconSize, iconSize);

//        var gap = 2f;
//        if (PinKind.INPUT == kind) {
//            ImGui.image(icon.id(), icon.size(), icon.uv1(), icon.uv2());
//            ImGui.sameLine(0, gap);
//            ImGui.text(label);
//        } else if (PinKind.OUTPUT == kind) {
//            ImGui.text(label);
//            ImGui.sameLine(0, gap);
//            ImGui.image(icon.id(), icon.size(), icon.uv1(), icon.uv2());
//        }
        ImGui.pushFont(EditorUtil.Fonts.icons);
        ImGui.text(FontAwesomeIcons.mapPin);
        ImGui.popFont();

        ImGui.endGroup();
        ImGui.popID();
        NodeEditor.endPin();
    }
}
