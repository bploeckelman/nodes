package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import lombok.RequiredArgsConstructor;

public class Pin extends EditorObject {

    public enum PinType { HEADER, PROPERTY }

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

    @Override
    public void render() {
        NodeEditor.beginPin(id, kind.value);
        ImGui.pushID(id);
        ImGui.beginGroup();

//        var isLinked = NodeEditor.pinHadAnyLinks(id);
//        var iconType = switch (type) {
//            case HEADER -> isLinked ? Icons.Type.FLOW_CONNECTED : Icons.Type.FLOW_DISCONNECTED;
//            case PROPERTY -> isLinked ? Icons.Type.FLIP_FULL : Icons.Type.TOKEN_IN;
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

        ImGui.endGroup();
        ImGui.popID();
        NodeEditor.endPin();
    }
}
