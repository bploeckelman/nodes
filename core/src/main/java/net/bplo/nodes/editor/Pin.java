package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.assets.Icons;
import net.bplo.nodes.editor.utils.PinAttachment;
import net.bplo.nodes.editor.utils.PinCompatibility;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;

public class Pin extends EditorObject {

    public static final float SIZE = 30f;
    private static final ImVec4 PIN_RECT_PADDING = new ImVec4(2, 2, 2, 2);

    public static boolean isInput(Pin pin)  { return pin.kind == PinKind.INPUT; }
    public static boolean isOutput(Pin pin) { return pin.kind == PinKind.OUTPUT; }

    public final PinKind kind;
    public final PinType type;
    public final PinAttachment attachment;

    private final ImGuiWidgetBounds iconBounds;
    private final ImGuiWidgetBounds pinRectBounds;

    public Pin(Node node, PinKind kind, PinType type) {
        this(new PinAttachment.NodeType(node), kind, type);
        node.pins.add(this);
    }

    public Pin(Prop prop, PinKind kind, PinType type) {
        this(new PinAttachment.PropType(prop), kind, type);
        prop.pins.add(this);
    }

    private Pin(PinAttachment attachment, PinKind kind, PinType type) {
        super(Type.PIN);
        this.kind = kind;
        this.type = type;
        this.attachment = attachment;
        this.iconBounds = new ImGuiWidgetBounds();
        this.pinRectBounds = new ImGuiWidgetBounds();
    }

    /**
     * Limited access constructor intended for use by {@link EditorSerializer}
     * to create {@link Pin} instances from saved json data, when this pin
     * is attached to a {@link Node}
     */
    Pin(long savedId, Node node, PinKind kind, PinType type) {
        this(savedId, new PinAttachment.NodeType(node), kind, type);
        node.pins.add(this);
    }

    /**
     * Limited access constructor intended for use by {@link EditorSerializer}
     * to create {@link Pin} instances from saved json data, when this pin
     * is attached to a {@link Prop}
     */
    Pin(long savedId, Prop prop, PinKind kind, PinType type) {
        this(savedId, new PinAttachment.PropType(prop), kind, type);
        prop.pins.add(this);
    }

    /**
     * Internal shared constructor intended for use by the package-private
     * {@link EditorSerializer} constructors above.
     */
    private Pin(long savedId, PinAttachment attachment, PinKind kind, PinType type) {
        super(Type.PIN, savedId);
        this.kind = kind;
        this.type = type;
        this.attachment = attachment;
        this.iconBounds = new ImGuiWidgetBounds();
        this.pinRectBounds = new ImGuiWidgetBounds();
    }

    public Node getNode() {
        return switch (attachment) {
            case PinAttachment.NodeType nodeAttachment -> nodeAttachment.node();
            case PinAttachment.PropType propAttachment -> propAttachment.prop().node;
        };
    }

    @Override
    public void render() {
        NodeEditor.beginPin(id, kind.value);
        ImGui.pushID(id);

        ImGui.beginGroup();
        {
            var pinIcon = buildPinIcon();
            ImGui.image(
                pinIcon.image.id(),
                pinIcon.image.size(),
                pinIcon.image.uv1(),
                pinIcon.image.uv2(),
                pinIcon.tint);
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

    @Override
    public void renderContextMenu(Editor editor) {
        headerText("Pin #%d".formatted(id));

        separatorText("Details");
        var kindColor = ImGuiColors.salmon.asInt();
        var typeColor = ImGuiColors.coral.asInt();
        var labelColor = ImGuiColors.white.asInt();

        ImGui.textColored(labelColor, "Kind: ");
        ImGui.sameLine();
        ImGui.textColored(kindColor, kind.name().toLowerCase());

        ImGui.textColored(labelColor, "Type: ");
        ImGui.sameLine();
        ImGui.textColored(typeColor, type.name().toLowerCase());

        separatorText("Attachment");
        var nodeColor = ImGuiColors.cyan.asInt();
        var propColor = ImGuiColors.goldenrod.asInt();
        switch (attachment) {
            case PinAttachment.NodeType nodeAttachment -> {
                var node = nodeAttachment.node();
                ImGui.textColored(labelColor, "Node: ");
                ImGui.sameLine();
                ImGui.textColored(nodeColor, node.label());
            }
            case PinAttachment.PropType propAttachment -> {
                var prop = propAttachment.prop();
                var node = prop.node;
                ImGui.textColored(labelColor, "Node: ");
                ImGui.sameLine();
                ImGui.textColored(nodeColor, node.label());

                ImGui.textColored(labelColor, "Prop: ");
                ImGui.sameLine();
                ImGui.textColored(propColor, prop.label());
            }
        }

        ImGui.dummy(0, 4);
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
        if (src.attachment instanceof PinAttachment.PropType(Prop srcProp)
         && dst.attachment instanceof PinAttachment.PropType(Prop dstProp)) {
            var isSameProperty = (srcProp.id == dstProp.id);
            if (isSameProperty) return PinCompatibility.reject("Cannot link pins in same property");
        }

        // TODO(brian): evaluate whether existing links
        //  would invalidate the link under consideration
//        var isLinkedTo = src.node.linkedTo(dst.node);
//        else if (isLinkedTo) return Compatibility.reject("Cannot link pins already linked");

        return PinCompatibility.ok();
    }

    private PinIcon buildPinIcon() {
        record TintedIcon(Icons.Type icon, ImVec4 tint) {}

        var isLinked = NodeEditor.pinHadAnyLinks(id);
        var unlinkedColor = ImGuiColors.white.asVec4();
        var tintedIcon = switch (type) {
            case FLOW -> isLinked
                ? new TintedIcon(Icons.Type.PIN_FLOW_LINKED, Link.Appearance.FLOW.color)
                : new TintedIcon(Icons.Type.PIN_FLOW,        unlinkedColor);
            case DATA -> {
                if (isLinked) {
                    yield new TintedIcon(Icons.Type.PIN_DATA_LINKED, Link.Appearance.DATA.color);
                } else {
                    yield switch (kind) {
                        case INPUT  -> new TintedIcon(Icons.Type.PIN_DATA_INPUT,  unlinkedColor);
                        case OUTPUT -> new TintedIcon(Icons.Type.PIN_DATA_OUTPUT, unlinkedColor);
                    };
                }
            }
        };

        var region = tintedIcon.icon.get();
        var image = EditorUtil.Image.from(region, SIZE, SIZE);
        return new PinIcon(image, tintedIcon.tint);
    }

    private record PinIcon(EditorUtil.Image image, ImVec4 tint) {}
}
