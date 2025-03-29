package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.assets.Icons;
import net.bplo.nodes.editor.EditorObject;
import net.bplo.nodes.editor.EditorUtil;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;
import net.bplo.nodes.objects.utils.PinAttachment;
import net.bplo.nodes.objects.utils.PinCompatibility;
import net.bplo.nodes.objects.utils.PinKind;
import net.bplo.nodes.objects.utils.PinType;

public class Pin extends EditorObject {

    public static final float SIZE = 30f;
    private static final ImVec4 PIN_RECT_PADDING = new ImVec4(2, 2, 2, 2);

    public static boolean isInput(Pin pin)  { return pin.kind == PinKind.INPUT; }
    public static boolean isOutput(Pin pin) { return pin.kind == PinKind.OUTPUT; }

    public final PinKind kind;
    public final PinType type;

    public PinAttachment attachment;

    private final ImGuiWidgetBounds iconBounds;
    private final ImGuiWidgetBounds pinRectBounds;

    public Pin(PinKind kind, PinType type, Node node) {
        this(kind, type, new PinAttachment.NodeType(node));
    }

    public Pin(PinKind kind, PinType type, Prop property) {
        this(kind, type, new PinAttachment.PropertyType(property));
    }

    private Pin(PinKind kind, PinType type, PinAttachment attachment) {
        super(Type.PIN);
        this.kind = kind;
        this.type = type;
        this.attachment = attachment;
        this.iconBounds = new ImGuiWidgetBounds();
        this.pinRectBounds = new ImGuiWidgetBounds();
    }

    @Override
    public void render() {
        NodeEditor.beginPin(id, kind.value);
        ImGui.pushID(id);

        ImGui.beginGroup();
        {
            var icon = buildIcon();
            ImGui.image(icon.id(), icon.size(), icon.uv1(), icon.uv2());
            iconBounds.update();

            // configure the pin rectangle and pivot, ie. where link lines start from
            pinRectBounds.setWithPadding(iconBounds, PIN_RECT_PADDING);
            NodeEditor.pinRect(pinRectBounds.min(), pinRectBounds.max());

            // 'lock' link endpoints to the center of the pin icon,
            // requires NodeEditorStyleVar.PinRadius to be set to zero
            NodeEditor.pinPivotRect(iconBounds.center(), iconBounds.center());
        }
        ImGui.endGroup();

        ImGui.popID();
        NodeEditor.endPin();
    }

    public PinCompatibility canLinkTo(Pin dst) {
        var src = this;

        // check for incompatible states
        var isSelfLink = (src == dst);
        var isSameKind = (src.kind == dst.kind);
        var isDiffType = (src.type != dst.type);
        if      (isSelfLink) return PinCompatibility.reject("Cannot link pin to itself");
        else if (isSameKind) return PinCompatibility.reject("Incompatible pin kinds, in/out");
        else if (isDiffType) return PinCompatibility.reject("Incompatible pin types");

        // check for incompatible attachments
        if (src.attachment instanceof PinAttachment.NodeType(Node srcNode)
         && dst.attachment instanceof PinAttachment.NodeType(Node dstNode)) {
            var isSameNode = (srcNode.id == dstNode.id);
            if (isSameNode) return PinCompatibility.reject("Cannot link pins in same node");
        }
        if (src.attachment instanceof PinAttachment.PropertyType(Prop srcProp)
         && dst.attachment instanceof PinAttachment.PropertyType(Prop dstProp)) {
            var isSameProperty = (srcProp.id == dstProp.id);
            if (isSameProperty) return PinCompatibility.reject("Cannot link pins in same property");
        }

        // TODO(brian): evaluate whether existing links
        //  would invalidate the link under consideration
//        var isLinkedTo = src.node.linkedTo(dst.node);
//        else if (isLinkedTo) return Compatibility.reject("Cannot link pins already linked");

        return PinCompatibility.ok();
    }

    private EditorUtil.Image buildIcon() {
        var isLinked = NodeEditor.pinHadAnyLinks(id);

        var region  = switch (type) {
            case FLOW -> isLinked ? Icons.Type.PIN_FLOW_LINKED.get() : Icons.Type.PIN_FLOW.get();
            case DATA -> {
                if (isLinked) {
                    yield Icons.Type.PIN_DATA_LINKED.get();
                } else {
                    yield switch (kind) {
                        case INPUT -> Icons.Type.PIN_DATA_INPUT.get();
                        case OUTPUT -> Icons.Type.PIN_DATA_OUTPUT.get();
                    };
                }
            }
        };

        return EditorUtil.Image.from(region, SIZE, SIZE);
    }
}
