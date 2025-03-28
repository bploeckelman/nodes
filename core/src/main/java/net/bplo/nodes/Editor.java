package net.bplo.nodes;

import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImLong;
import net.bplo.nodes.objects.EditorObject;
import net.bplo.nodes.objects.Link;
import net.bplo.nodes.objects.Node;
import net.bplo.nodes.objects.Pin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Editor implements Disposable {

    public static final String SETTINGS_FILE = "editor.json";
    public static final String DEFAULT_FONT = "play-regular.ttf";

    /**
     * String identifiers for each right-click context menu popup
     */
    private static class PopupIds {
        static final String NODE_NEW = "Create Node";
        static final String NODE = "Node Context Menu";
        static final String PIN = "Pin Context Menu";
        static final String LINK = "Link Context Menu";
    }

    public static class ContextMenu {
        public ImLong nodeId = new ImLong();
        public ImLong pinId = new ImLong();
        public ImLong linkId = new ImLong();
        public ImVec2 newNodePosition = new ImVec2();
        public Pin    newNodeLinkPin = null;
    }

    private final ContextMenu contextMenu = new ContextMenu();

    public final Main app;
    public final List<Node> nodes;
    public final List<Link> links;
    public final List<Pin> pins;
    public final Map<Long, EditorObject> objectsById;
    public final NodeEditorContext context;

    public boolean addTestNode = true;

    public Editor() {
        var config = new NodeEditorConfig();
        config.setSettingsFile(SETTINGS_FILE);

        this.app = Main.app;
        this.nodes = new ArrayList<>();
        this.links = new ArrayList<>();
        this.pins = new ArrayList<>();
        this.objectsById = new HashMap<>();
        this.context = NodeEditor.createEditor(config);

        NodeEditor.setCurrentEditor(context);

        // disabling key shortcuts because 'f' is bound to zoom by default
        // and I can't find an easy way to override it when doing text input
        NodeEditor.enableShortcuts(false);
    }

    @Override
    public void dispose() {
        NodeEditor.destroyEditor(context);
    }

    /**
     * Update the editor state. Must be called between
     * {@link net.bplo.nodes.imgui.ImGuiPlatform#startFrame()}
     * and {@link net.bplo.nodes.imgui.ImGuiPlatform#endFrame()}
     * to ensure that the input processor is active.
     */
    public void update(float delta) {
        if (addTestNode) {
            addTestNode = false;

            var node = new Node();
            nodes.add(node);

            var position = new ImVec2(0, 0);
            NodeEditor.setNodePosition(node.id, position);
        }
    }

    public void render() {
        NodeEditor.setCurrentEditor(context);
        pushWindowStyles();

        // editor window always fills entire screen
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSize(), ImGuiCond.Always);

        var flags = ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize;
        if (ImGui.begin("Editor", flags)) {
            NodeEditor.begin("Editor");
            pushEditorStyles();

            nodes.forEach(Node::render);
            links.forEach(Link::render);

            handleCreateLink();
            handleDeleteObject();
            handleContextMenus();

            popEditorStyles();
            NodeEditor.end();
        }
        ImGui.end();

        popWindowStyles();
    }

    public void fitToContent() {
        fitToContent(1f);
    }

    public void fitToContent(float duration) {
        NodeEditor.navigateToContent(duration);
    }

    public void navigateToSelection() {
        NodeEditor.navigateToSelection(1);
    }

    private void handleCreateLink() {

    }

    private void handleDeleteObject() {

    }

    private void handleContextMenus() {
        var mousePos = ImGui.getMousePos();

        // NOTE: about reference frame switching:
        //  popup windows are not done in graph space, they're done in screen space.
        //  `suspend()` changes the positioning reference frame from "graph" to "screen"
        //  then all following calls are in screen space and `resume()` returns to reference frame
        NodeEditor.suspend();
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8f, 8f);

        // open the appropriate popup, if any, depending on the right-click context
        if (NodeEditor.showNodeContextMenu(contextMenu.nodeId)) {
            ImGui.openPopup(PopupIds.NODE);
        } else if (NodeEditor.showPinContextMenu(contextMenu.pinId)) {
            ImGui.openPopup(PopupIds.PIN);
        } else if (NodeEditor.showLinkContextMenu(contextMenu.linkId)) {
            ImGui.openPopup(PopupIds.LINK);
        } else if (NodeEditor.showBackgroundContextMenu()) {
            ImGui.openPopup(PopupIds.NODE_NEW);
            contextMenu.newNodeLinkPin = null;

            // NOTE(brian): we want the mouse pos in 'graph' space not screen space
            //  because its used to position a new node, so we need to 'stash' the
            //  value retrieved above, before NodeEditor.suspend() to use it below
            contextMenu.newNodePosition = new ImVec2(mousePos);
        }

        // node context popup -------------------------------------------------
        if (ImGui.beginPopup(PopupIds.NODE)) {
            ImGui.text("Node Context Menu");
            ImGui.separator();

            var nodeId = contextMenu.nodeId.get();
            findNode(nodeId).ifPresentOrElse(node -> {
                ImGui.text("ID: %d".formatted(node.id));
                ImGui.text("Node: %s".formatted(node.label()));
                ImGui.separator();
                if (ImGui.menuItem("Delete")) {
                    removeNode(node);
                }
            }, () -> ImGui.text("Unknown node: %d".formatted(nodeId)));

            ImGui.endPopup();
        }

        // pin context popup --------------------------------------------------
        if (ImGui.beginPopup(PopupIds.PIN)) {
            ImGui.text("Pin Context Menu");
            ImGui.separator();

            var pinId = contextMenu.pinId.get();
            findPin(pinId).ifPresentOrElse(pin -> {
                ImGui.text("ID: %d".formatted(pin.id));
                ImGui.text("Pin: %s".formatted(pin.label()));
            }, () -> ImGui.text("Unknown pin: %d".formatted(pinId)));

            ImGui.endPopup();
        }

        // link context popup -------------------------------------------------
        if (ImGui.beginPopup(PopupIds.LINK)) {
            ImGui.text("Link Context Menu");
            ImGui.separator();

            var linkId = contextMenu.linkId.get();
            findLink(linkId).ifPresentOrElse(link -> {
                ImGui.text("ID: %d".formatted(link.id));
                ImGui.separator();
                if (ImGui.menuItem("Delete")) {
                    removeLink(link);
                }
            }, () -> ImGui.text("Unknown link: %s".formatted(linkId)));

            ImGui.endPopup();
        }

        // create new node popup ----------------------------------------------
        ImGui.pushStyleVar(ImGuiStyleVar.PopupBorderSize, 2f);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, 10f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 20f, 10f);
        if (ImGui.beginPopup(PopupIds.NODE_NEW)) {
            ImGui.pushFont(EditorUtil.Fonts.nodeHeader);
            ImGui.textColored(EditorUtil.Colors.newNodeHeader, PopupIds.NODE_NEW);
            ImGui.popFont();

            ImGui.separator();
            ImGui.dummy(0, 2);

            // spawn a node if user selects a type from the popup
            Node newNode = null;
            // TODO(brian): TEMP *** -----------------------------
            var nodeTypes = List.of(
                  "---Normal"
                , "Node A"
                , "Node B"
                , "---Fancy"
                , "Node C"
            );
            // TODO(brian): TEMP *** -----------------------------
            for (int i = 0; i < nodeTypes.size(); i++) {
                var type = nodeTypes.get(i);
                if (type.startsWith("---")) {
                    if (i != 0) {
                        ImGui.dummy(0, 2);
                    }
                    var separatorText = type.substring(3);
                    if (separatorText.isBlank()) {
                        ImGui.separator();
                    } else {
                        ImGui.pushStyleColor(ImGuiCol.Text, EditorUtil.Colors.dim);
                        ImGui.pushFont(EditorUtil.Fonts.small);
                        ImGui.separatorText(separatorText);
                        ImGui.popFont();
                        ImGui.popStyleColor();
                    }
                    continue;
                }

                if (ImGui.menuItem(type)) {
                    newNode = new Node();
                }
            }
            ImGui.dummy(0, 2);

            // if a new node was spawned, add it to the editor
            if (newNode != null) {
                addNode(newNode);

                // position the new node near the right-click position
                NodeEditor.setNodePosition(newNode.id, contextMenu.newNodePosition);

                // TODO: auto-connect a pin in the new node to a link that was dragged out
            }

            ImGui.endPopup();
        }
        ImGui.popStyleVar(3);

        ImGui.popStyleVar();
        NodeEditor.resume();
    }

    private void addNode(Node node) {
        nodes.add(node);
        objectsById.put(node.id, node);
    }

    private void removeNode(Node node) {
        nodes.remove(node);
        objectsById.remove(node.id);
    }

    private void addLink(Link link) {
        links.add(link);
        objectsById.put(link.id, link);
    }

    private void removeLink(Link link) {
        links.remove(link);
        objectsById.remove(link.id);
    }

    private Optional<Node> findNode(long id) {
        return nodes.stream().filter(node -> node.id == id).findFirst();
    }

    private Optional<Link> findLink(long id) {
        return links.stream().filter(link -> link.id == id).findFirst();
    }

    private Optional<Pin> findPin(long id) {
        return pins.stream().filter(pin -> pin.id == id).findFirst();
    }

    private void pushWindowStyles() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 5, 5);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowTitleAlign, 0.5f, 0.5f);
        ImGui.pushFont(app.imgui.getFont(DEFAULT_FONT));
    }

    private void popWindowStyles() {
        ImGui.popFont();
        ImGui.popStyleVar(4);
    }

    private void pushEditorStyles() {
        NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBg,        new ImVec4(1f, 1f, 1f, 0.0f));
        NodeEditor.pushStyleColor(NodeEditorStyleColor.NodeBorder,    new ImVec4( 0.6f,  0.6f,  0.6f, 0.8f));
        NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRect,       new ImVec4( 0.24f, 0.6f, 1f, 0.6f));
        NodeEditor.pushStyleColor(NodeEditorStyleColor.PinRectBorder, new ImVec4( 0.24f, 0.6f, 1f, 0.6f));

        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodePadding,             new ImVec4(6, 6, 6, 6));
        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodeRounding,            5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.NodeBorderWidth,         1.5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.HoveredNodeBorderWidth,  2.5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.SelectedNodeBorderWidth, 3.5f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.PinBorderWidth,          2f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.PinRadius,               10f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.LinkStrength,            250f);
        NodeEditor.pushStyleVar(NodeEditorStyleVar.SourceDirection,         new ImVec2( 1.0f, 0.0f));
        NodeEditor.pushStyleVar(NodeEditorStyleVar.TargetDirection,         new ImVec2(-1.0f, 0.0f));
    }

    private void popEditorStyles() {
        NodeEditor.popStyleVar(10);
        NodeEditor.popStyleColor(4);
    }
}
