package net.bplo.nodes.objects;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.assets.Icons;
import net.bplo.nodes.editor.EditorObject;
import net.bplo.nodes.editor.EditorUtil;
import net.bplo.nodes.objects.utils.PinAttachment;
import net.bplo.nodes.objects.utils.PinCompatibility;
import net.bplo.nodes.objects.utils.PinKind;
import net.bplo.nodes.objects.utils.PinType;

public class Pin extends EditorObject {

    private static final String TAG = Pin.class.getSimpleName();

    public static final float SIZE = 30f;

    public final PinKind kind;
    public final PinType type;
    public final PinAttachment attachment;

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
    }

    public static boolean isInput(Pin pin) {
        return pin.kind == PinKind.INPUT;
    }

    public static boolean isOutput(Pin pin) {
        return pin.kind == PinKind.OUTPUT;
    }

    // TODO(brian): needs to be refined
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

//        var isLinkedTo = src.node.linkedTo(dst.node);
//        else if (isLinkedTo) return Compatibility.reject("Cannot link pins already linked");

        return PinCompatibility.ok();
    }

    @Override
    public void render() {
        NodeEditor.beginPin(id, kind.value);
        ImGui.pushID(id);
        ImGui.beginGroup();

        var icon = buildIcon();
        ImGui.image(icon.id(), icon.size(), icon.uv1(), icon.uv2());

        // get the icon image widget extents
        var min = ImGui.getItemRectMin();
        var max = ImGui.getItemRectMax();

        // configure the pin rectangle and pivot, ie. where link lines start from
        var halfSize = SIZE / 2f;
        var center = new ImVec2(
            min.x + (max.x - min.x) / 2f,
            min.y + (max.y - min.y) / 2f);
        var spacing = ImGui.getStyle().getItemInnerSpacing();

        // pin click region is slightly larger than the icon
        NodeEditor.pinRect(
            center.x - halfSize - spacing.x,
            center.y - halfSize - spacing.y,
            center.x + halfSize + spacing.x,
            center.y + halfSize + spacing.y);

        // link ends 'lock' at the center of the icon,
        // requires NodeEditorStyleVar.PinRadius to be set to zero
        NodeEditor.pinPivotRect(center, center);

        ImGui.endGroup();
        ImGui.popID();
        NodeEditor.endPin();
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
