package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.ObjectMap;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImLong;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.imgui.ImGuiColors;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class EditorNodePane extends EditorPane {

    private static final String TAG = EditorNodePane.class.getSimpleName();

    // String identifiers for each right-click context menu popup
    private static final String POPUP_CREATE_NODE = "Create Node";
    private static final String POPUP_NODE = "Node Context Menu";
    private static final String POPUP_PIN  = "Pin Context Menu";
    private static final String POPUP_LINK = "Link Context Menu";

    private final ContextMenu contextMenu;

    private long[] selectedObjectIds;

    // TODO(brian): this should be ordered, and match insertion order in the 'create node' context menu
    final ObjectMap<String, NodeType> nodeTypes = new ObjectMap<>();

    public boolean showIds = false;

    public EditorNodePane(Editor editor) {
        super(editor);
        this.contextMenu = new ContextMenu();
        this.selectedObjectIds = new long[0];
    }

    @Override
    public void render() {
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSize(), ImGuiCond.Always);
        var flags = ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize;
        if (ImGui.begin("Editor", flags)) {
            // render editor menubar widgets
                              EditorWidget.renderSaveButton(editor);
            ImGui.sameLine(); EditorWidget.renderLoadButton(editor);
            ImGui.sameLine(); EditorWidget.renderShowIdsToggle(editor);
            ImGui.sameLine(); EditorWidget.renderLoadAssetMetadataButton(editor);
            ImGui.sameLine(); EditorWidget.renderSetContentPathButton(editor);
            ImGui.sameLine(); EditorWidget.renderContentCombo(editor);

            NodeEditor.begin("Editor");
            pushStyles();

            editor.nodes.forEach(Node::render);
            editor.links.forEach(Link::render);

            if (showIds) {
                editor.nodes.forEach(Node::renderIds);
            }

            // imgui-node-editor doesn't support a subset of imgui widgets within a node context,
            // props can emulate some of those by using a popup rendered outside a node context
            NodeEditor.suspend();
            for (var node : editor.nodes) {
                for (var prop : node.props) {
                    prop.renderPopup();
                }
            }
            NodeEditor.resume();

            handleCreateLink();
            handleDeleteObject();
            handleContextMenus();
            handleSelectionChanges();

            popStyles();
            NodeEditor.end();
        }
        ImGui.end();
    }

    private void handleCreateLink() {
        if (NodeEditor.beginCreate(ImGuiColors.medBlue.asVec4(), 6f)) {
            var aPinId = new ImLong();
            var bPinId = new ImLong();

            if (NodeEditor.queryNewLink(aPinId, bPinId)) {
                var srcPin = editor.findPin(aPinId.get());
                var dstPin = editor.findPin(bPinId.get());
                if (srcPin.isPresent() && dstPin.isPresent()) {
                    var src = srcPin.get();
                    var dst = dstPin.get();

                    // swap src/dst if necessary to ensure links are
                    // directionally correct: src(out) -> dst(in)
                    if (PinKind.INPUT  == src.kind && PinKind.OUTPUT == dst.kind) {
                        var temp = src;
                        src = dst;
                        dst = temp;
                    }

                    // reject links for pin pairs that are incompatible,
                    // otherwise create a new link if accepted
                    var compatibility = src.canLinkTo(dst);
                    if (compatibility.incompatible()) {
                        EditorMessage.show(EditorMessage.Type.ERROR, compatibility.message());
                        NodeEditor.rejectNewItem(ImGuiColors.red.asVec4(), 4f);
                    } else {
                        EditorMessage.show(EditorMessage.Type.ACCEPT, "Create link");
                        if (NodeEditor.acceptNewItem(ImGuiColors.lime.asVec4(), 8f)) {
                            editor.add(new Link(src, dst));
                        }
                    }
                }
            }

            NodeEditor.endCreate();
        }
    }

    private void handleDeleteObject() {
        // handle deleting nodes and links
        if (NodeEditor.beginDelete()) {
            var id = new ImLong();

            // if this is a node, remove it
            while (NodeEditor.queryDeletedNode(id)) {
                if (NodeEditor.acceptDeletedItem()) {
                    editor.findNode(id.get()).ifPresent(editor::remove);
                }
            }

            // if this is a link, remove it
            while (NodeEditor.queryDeletedLink(id)) {
                if (NodeEditor.acceptDeletedItem()) {
                    editor.findLink(id.get()).ifPresent(editor::remove);
                }
            }

            NodeEditor.endDelete();
        }
    }

    private void handleContextMenus() {
        var mousePos = ImGui.getMousePos();

        // NOTE: about reference frame switching:
        //  popup windows are not done in graph space, they're done in screen space.
        //  `suspend()` changes the positioning reference frame from "graph" to "screen"
        //  then all following calls are in screen space and `resume()` returns to reference frame
        NodeEditor.suspend();
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 2f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 20f, 10f);

        // open the appropriate popup, if any, depending on the right-click context
        if      (NodeEditor.showNodeContextMenu(contextMenu.nodeId)) ImGui.openPopup(POPUP_NODE);
        else if (NodeEditor.showPinContextMenu(contextMenu.pinId))   ImGui.openPopup(POPUP_PIN);
        else if (NodeEditor.showLinkContextMenu(contextMenu.linkId)) ImGui.openPopup(POPUP_LINK);
        else if (NodeEditor.showBackgroundContextMenu()) {
            ImGui.openPopup(POPUP_CREATE_NODE);
            contextMenu.newNodeLinkPin = null;

            // NOTE(brian): we want the mouse pos in 'graph' space not screen space
            //  because its used to position a new node, so we need to 'stash' the
            //  value retrieved above, before NodeEditor.suspend() to use it below
            contextMenu.newNodePosition = new ImVec2(mousePos);
        }

        // node context popup -------------------------------------------------
        if (ImGui.beginPopup(POPUP_NODE)) {
            var nodeId = contextMenu.nodeId.get();
            editor.findNode(nodeId).ifPresentOrElse(
                node -> node.renderContextMenu(editor),
                () -> ImGui.text("Unknown node: %d".formatted(nodeId)));
            ImGui.endPopup();
        }

        // pin context popup --------------------------------------------------
        if (ImGui.beginPopup(POPUP_PIN)) {
            var pinId = contextMenu.pinId.get();
            editor.findPin(pinId).ifPresentOrElse(
                pin -> pin.renderContextMenu(editor),
                () -> ImGui.text("Unknown pin: %d".formatted(pinId)));
            ImGui.endPopup();
        }

        // link context popup -------------------------------------------------
        if (ImGui.beginPopup(POPUP_LINK)) {
            var linkId = contextMenu.linkId.get();
            editor.findLink(linkId).ifPresentOrElse(
                link -> link.renderContextMenu(editor),
                () -> ImGui.text("Unknown link: %s".formatted(linkId)));
            ImGui.endPopup();
        }

        // create new node popup ----------------------------------------------
        if (ImGui.beginPopup(POPUP_CREATE_NODE)) {
            ImGui.pushFont(EditorUtil.Fonts.nodeHeader);
            ImGui.textColored(ImGuiColors.cyan.asVec4(), POPUP_CREATE_NODE);
            ImGui.popFont();
            if (nodeTypes.isEmpty()) {
                ImGui.separator();
                ImGui.textDisabled("""
                    No asset metadata loaded,
                    showing test node types.
                    """);
            }

            ImGui.separator();
            ImGui.dummy(0, 2);

            // spawn a node if user selects a type from the popup
            Node newNode = null;
            if (nodeTypes.isEmpty()) {
                // TODO(brian): TEMP *** -----------------------------
                var separatorPrefix = "---";
                var nodeTypes = List.of(
                    separatorPrefix + "Normal"
                    , "Node A"
                    , "Node B"
                    , separatorPrefix + "Fancy"
                    , "Node C"
                );
                // TODO(brian): TEMP *** -----------------------------
                for (int i = 0; i < nodeTypes.size(); i++) {
                    var type = nodeTypes.get(i);
                    if (type.startsWith(separatorPrefix)) {
                        if (i != 0) {
                            ImGui.dummy(0, 2);
                        }
                        var separatorText = type.substring(separatorPrefix.length());
                        if (separatorText.isBlank()) {
                            ImGui.separator();
                        } else {
                            ImGui.pushStyleColor(ImGuiCol.Text, ImGuiColors.medGray.asInt());
                            ImGui.pushFont(EditorUtil.Fonts.small);
                            ImGui.separatorText(separatorText);
                            ImGui.popFont();
                            ImGui.popStyleColor();
                        }
                        continue;
                    }

                    if (ImGui.menuItem(type)) {
                        newNode = editor.createTestNode();
                    }
                }
            } else {
                for (var nodeTypeEntry : nodeTypes.entries()) {
                    var nodeName = nodeTypeEntry.key;
                    var nodeType = nodeTypeEntry.value;

                    if (ImGui.menuItem(nodeName)) {
                        newNode = NodeFactory.createNode(editor, nodeType);
                        break;
                    }
                }
            }
            ImGui.dummy(0, 2);

            // if a new node was spawned, add it to the editor
            if (newNode != null) {
                // TODO(brian): auto-connect a pin in the new node to a link that was dragged out
                // position the new node near the right-click position
                NodeEditor.setNodePosition(newNode.id, contextMenu.newNodePosition);
                editor.add(newNode);
            }

            ImGui.endPopup();
        }

        ImGui.popStyleVar(2);
        NodeEditor.resume();
    }

    private void handleSelectionChanges() {
        var selectedObjectCount = NodeEditor.getSelectedObjectCount();
        if (selectedObjectCount != selectedObjectIds.length) {
            selectedObjectIds = new long[selectedObjectCount];
        }

        var selectedNodesCount = NodeEditor.getSelectedNodes(selectedObjectIds, selectedObjectCount);
        if (selectedNodesCount == 0) {
            editor.infoPane.clear();
        } else {
            var selectedNodes = Arrays.stream(selectedObjectIds)
                .mapToObj(editor::findNode)
                .flatMap(Optional::stream)
                .toList();
            editor.infoPane.select(selectedNodes);
        }
    }

    private void pushStyles() {
        NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBg,        new ImVec4(1f, 1f, 1f, 0.0f));
        NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBorder,    new ImVec4( 0.6f,  0.6f,  0.6f, 0.8f));
        NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRect,       new ImVec4( 0.24f, 0.6f, 1f, 0.6f));
        NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRectBorder, new ImVec4( 0.24f, 0.6f, 1f, 0.6f));

        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodePadding,             new ImVec4(4, 4, 4, 4));
        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodeRounding,            5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodeBorderWidth,         1.5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.HoveredNodeBorderWidth,  2.5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.SelectedNodeBorderWidth, 3.5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.PinBorderWidth,          2f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.PinRadius,               0f); // NOTE: radius of circle that links can 'slide around' on at their start/end points, 0 means exactly at pivot location
        NodeEditor.pushStyleVar(NodeEditorStyleVar.LinkStrength,            250f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.SourceDirection,         new ImVec2( 1.0f, 0.0f));
        NodeEditor.pushStyleVar(NodeEditorStyleVar.TargetDirection,         new ImVec2(-1.0f, 0.0f));
    }

    private void popStyles() {
        NodeEditor.popStyleVar(10);
        NodeEditor.popStyleColor(4);
    }

    /**
     * Container for data used by right-click context menus
     */
    public static class ContextMenu {
        public ImLong nodeId = new ImLong();
        public ImLong pinId  = new ImLong();
        public ImLong linkId = new ImLong();
        public ImVec2 newNodePosition = new ImVec2();
        public Pin    newNodeLinkPin  = null;
    }
}
