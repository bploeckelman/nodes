package net.bplo.nodes.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Timer;
import imgui.ImColor;
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
import net.bplo.nodes.Main;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;
import net.bplo.nodes.imgui.FontAwesomeIcons;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiLayout;
import net.bplo.nodes.imgui.ImGuiPlatform;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.bplo.nodes.editor.EditorUtil.Fonts;

public class Editor implements Disposable {

    public static final String TAG = Editor.class.getSimpleName();
    public static final String SETTINGS_FILE = "editor.json";
    public static final String DEFAULT_FONT = "play-regular.ttf";

    public static class TestProperty extends Prop {

        private final int backgroundColor = ImColor.rgba("#00004f2f");
        private final ImGuiWidgetBounds bounds = new ImGuiWidgetBounds();

        public TestProperty(Node node) {
            super(node);
            new Pin(this, PinKind.INPUT,  PinType.DATA);
            new Pin(this, PinKind.OUTPUT, PinType.DATA);
        }

        TestProperty(long savedId, Node node) {
            super(savedId, node);
        }

        @Override
        public void render() {
            ImGui.beginGroup();
            {
                var contentWidth = node.width - 2 * Pin.SIZE;

                ImGuiLayout.beginColumn(Pin.SIZE);
                {
                    inputPins().forEach(Pin::render);
                }
                ImGuiLayout.nextColumn(contentWidth);
                {
                    // NOTE: same approach as in Node.render() to ensure a fixed column width
                    //  when the only widget in the column is text, which collapses to fit the string
                    var cursorPos = ImGui.getCursorPos();
                    ImGui.setNextItemAllowOverlap();
                    ImGui.dummy(contentWidth, Pin.SIZE);
                    ImGui.setCursorPos(cursorPos);
                    ImGui.text("prop");
                }
                ImGuiLayout.nextColumn(Pin.SIZE);
                {
                    outputPins().forEach(Pin::render);
                }
                ImGuiLayout.endColumn();
            }
            ImGui.endGroup();
            bounds.update();
        }

        @Override
        public void renderAfterNode() {
            var draw = NodeEditor.getNodeBackgroundDrawList(node.id);
            var rounding = NodeEditor.getStyle().getNodeRounding();
            draw.addRectFilled(bounds.min(), bounds.max(), backgroundColor, rounding);
        }
    }

    /**
     * String identifiers for each right-click context menu popup
     */
    private static class PopupIds {
        static final String NODE_CREATE = "Create Node";
        static final String NODE = "Node Context Menu";
        static final String PIN  = "Pin Context Menu";
        static final String LINK = "Link Context Menu";
    }

    /**
     * Container for data used by right-click context menus
     */
    public static class ContextMenu {
        public ImLong nodeId = new ImLong();
        public ImLong pinId = new ImLong();
        public ImLong linkId = new ImLong();
        public ImVec2 newNodePosition = new ImVec2();
        public Pin    newNodeLinkPin = null;
    }

    private final Json json;
    private final ContextMenu contextMenu;
    private final NodeEditorContext editorContext;

    public final Main app;
    public final List<Node> nodes;
    public final List<Prop> props;
    public final List<Pin>  pins;
    public final List<Link> links;
    public final Map<Long, EditorObject> objectsById;

    public Editor() {
        var config = new NodeEditorConfig();
        config.setSettingsFile(SETTINGS_FILE);

        this.app = Main.app;
        this.nodes = new ArrayList<>();
        this.props = new ArrayList<>();
        this.pins = new ArrayList<>();
        this.links = new ArrayList<>();
        this.objectsById = new HashMap<>();
        this.contextMenu = new ContextMenu();
        this.editorContext = NodeEditor.createEditor(config);

        NodeEditor.setCurrentEditor(editorContext);
        // NOTE: disabling key shortcuts because 'f' is bound to zoom by default
        //  and I can't find an easy way to override it when doing text input
        NodeEditor.enableShortcuts(false);

        this.json = new Json();
        json.setSerializer(EditorSerializer.NodeList.class, new EditorSerializer());
    }

    @Override
    public void dispose() {
        NodeEditor.destroyEditor(editorContext);
    }

    /**
     * Update the editor state. Must be called between
     * {@link ImGuiPlatform#startFrame()}
     * and {@link ImGuiPlatform#endFrame()}
     * to ensure that the input processor is active.
     */
    public void update(float delta) {
    }

    public void render() {
        NodeEditor.setCurrentEditor(editorContext);
        pushWindowStyles();

        // editor window always fills entire screen
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSize(), ImGuiCond.Always);

        var flags = ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize;
        if (ImGui.begin("Editor", flags)) {

            ImGui.pushFont(Fonts.icons);
            if (ImGui.button(FontAwesomeIcons.floppyDisk + " Save")) {
                save();
            }
            ImGui.sameLine();
            if (ImGui.button(FontAwesomeIcons.folderOpen + " Load")) {
                load();
            }
            ImGui.popFont();

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
        if (NodeEditor.beginCreate(ImGuiColors.medBlue.asVec4(), 6f)) {
            var aPinId = new ImLong();
            var bPinId = new ImLong();

            if (NodeEditor.queryNewLink(aPinId, bPinId)) {
                var srcPin = findPin(aPinId.get());
                var dstPin = findPin(bPinId.get());
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
                            add(new Link(src, dst));
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
                    findNode(id.get()).ifPresent(this::remove);
                }
            }

            // if this is a link, remove it
            while (NodeEditor.queryDeletedLink(id)) {
                if (NodeEditor.acceptDeletedItem()) {
                    findLink(id.get()).ifPresent(this::remove);
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
        if (NodeEditor.showNodeContextMenu(contextMenu.nodeId)) {
            ImGui.openPopup(PopupIds.NODE);
        } else if (NodeEditor.showPinContextMenu(contextMenu.pinId)) {
            ImGui.openPopup(PopupIds.PIN);
        } else if (NodeEditor.showLinkContextMenu(contextMenu.linkId)) {
            ImGui.openPopup(PopupIds.LINK);
        } else if (NodeEditor.showBackgroundContextMenu()) {
            ImGui.openPopup(PopupIds.NODE_CREATE);
            contextMenu.newNodeLinkPin = null;

            // NOTE(brian): we want the mouse pos in 'graph' space not screen space
            //  because its used to position a new node, so we need to 'stash' the
            //  value retrieved above, before NodeEditor.suspend() to use it below
            contextMenu.newNodePosition = new ImVec2(mousePos);
        }

        // node context popup -------------------------------------------------
        if (ImGui.beginPopup(PopupIds.NODE)) {
            var nodeId = contextMenu.nodeId.get();
            findNode(nodeId).ifPresentOrElse(
                node -> node.renderContextMenu(this),
                () -> ImGui.text("Unknown node: %d".formatted(nodeId)));
            ImGui.endPopup();
        }

        // pin context popup --------------------------------------------------
        if (ImGui.beginPopup(PopupIds.PIN)) {
            var pinId = contextMenu.pinId.get();
            findPin(pinId).ifPresentOrElse(
                pin -> pin.renderContextMenu(this),
                () -> ImGui.text("Unknown pin: %d".formatted(pinId)));
            ImGui.endPopup();
        }

        // link context popup -------------------------------------------------
        if (ImGui.beginPopup(PopupIds.LINK)) {
            var linkId = contextMenu.linkId.get();
            findLink(linkId).ifPresentOrElse(
                link -> link.renderContextMenu(this),
                () -> ImGui.text("Unknown link: %s".formatted(linkId)));
            ImGui.endPopup();
        }

        // create new node popup ----------------------------------------------
        if (ImGui.beginPopup(PopupIds.NODE_CREATE)) {
            ImGui.pushFont(Fonts.nodeHeader);
            ImGui.textColored(ImGuiColors.cyan.asVec4(), PopupIds.NODE_CREATE);
            ImGui.popFont();

            ImGui.separator();
            ImGui.dummy(0, 2);

            // spawn a node if user selects a type from the popup
            Node newNode = null;
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
                        ImGui.pushFont(Fonts.small);
                        ImGui.separatorText(separatorText);
                        ImGui.popFont();
                        ImGui.popStyleColor();
                    }
                    continue;
                }

                if (ImGui.menuItem(type)) {
                    newNode = createTestNode();
                }
            }
            ImGui.dummy(0, 2);

            // if a new node was spawned, add it to the editor
            if (newNode != null) {
                // TODO(brian): auto-connect a pin in the new node to a link that was dragged out
                // position the new node near the right-click position
                NodeEditor.setNodePosition(newNode.id, contextMenu.newNodePosition);
                add(newNode);
            }

            ImGui.endPopup();
        }

        ImGui.popStyleVar(2);
        NodeEditor.resume();
    }

    public void add(EditorObject object) {
        switch (object) {
            case Node node -> {
                objectsById.put(node.id, node);
                node.pins.forEach(this::add);
                node.props.forEach(this::add);
                nodes.add(node);
            }
            case Prop prop -> {
                objectsById.put(prop.id, prop);
                prop.pins.forEach(this::add);
                props.add(prop);
            }
            case Pin pin -> {
                objectsById.put(pin.id, pin);
                pins.add(pin);
            }
            case Link link -> {
                objectsById.put(link.id, link);
                links.add(link);
            }
            case null, default ->
                throw new GdxRuntimeException("Cannot add editor object with null value or unsupported type");
        }
    }

    public void remove(EditorObject object) {
        switch (object) {
            case Node node -> {
                node.pins.forEach(this::remove);
                node.props.forEach(this::remove);
                nodes.remove(node);
                objectsById.remove(node.id);
            }
            case Prop prop -> {
                prop.pins.forEach(this::remove);
                props.remove(prop);
                objectsById.remove(prop.id);
            }
            case Pin pin -> {
                pins.remove(pin);
                objectsById.remove(pin.id);
            }
            case Link link -> {
                link.disconnect();
                links.remove(link);
                objectsById.remove(link.id);
            }
            case null, default ->
                throw new GdxRuntimeException("Cannot remove editor object with null value or unsupported type");
        }
    }

    private void save() {
        var file = EditorFileDialog.openSaveFile();
        if (file == null) {
            Gdx.app.log(TAG, "No file selected");
            return;
        }

        var nodeList = new EditorSerializer.NodeList(nodes);
        var nodeListJson = json.prettyPrint(nodeList); // calls EditorSerializer.write(...)
        Util.log(TAG, "Exported node graph as json:\n%s".formatted(nodeListJson));

        try {
            var fileHandle = Gdx.files.absolute(file.getAbsolutePath());
            fileHandle.writeString(nodeListJson, false);
            Util.log(TAG, "Saved node graph json to file: '%s'".formatted(file.getPath()));
        } catch (GdxRuntimeException e) {
            Gdx.app.error(TAG, "Failed to save file: " + file.getPath(), e);
        }
    }

    private void load() {
        var file = EditorFileDialog.openLoadFile();
        if (file == null) {
            Gdx.app.log(TAG, "No file selected");
            return;
        }

        // clear existing editor objects
        nodes.clear();
        props.clear();
        pins.clear();
        links.clear();
        objectsById.clear();

        EditorSerializer.NodeList nodeList = null;
        try {
            // read json from the file and output it for debugging
            var path = file.toPath();
            var nodeListJson = new String(Files.readAllBytes(path));
            Util.log(TAG, "Loaded node graph json from '%s':\n%s".formatted(path, nodeListJson));

            // read json from the file into a NodeList object
            var jsonValue = new JsonReader().parse(nodeListJson);
            var editorSerializer = json.getSerializer(EditorSerializer.NodeList.class);
            nodeList = editorSerializer.read(json, jsonValue, EditorSerializer.NodeList.class);
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read file", e);
        }

        if (nodeList == null) {
            Gdx.app.error(TAG, "Failed to parse node graph json");
            return;
        }

        // put loaded editor objects in Editor containers
        nodes.addAll(nodeList);
        for (var node : nodes) {
            objectsById.put(node.id, node);

            pins.addAll(node.pins);
            pins.forEach(pin -> objectsById.put(pin.id, pin));

            props.addAll(node.props);
            props.forEach(prop -> {
                objectsById.put(prop.id, prop);

                pins.addAll(prop.pins);
                pins.forEach(pin -> objectsById.put(pin.id, pin));
            });

            node.incomingLinks.forEach(link -> {
                var insertedValue = objectsById.putIfAbsent(link.id, link);
                if (insertedValue == null) {
                    links.add(link);
                }
            });
            node.outgoingLinks.forEach(link -> {
                var insertedValue = objectsById.putIfAbsent(link.id, link);
                if (insertedValue == null) {
                    links.add(link);
                }
            });
        }

        // NOTE: *** important *** update id counter to max(id) + 1
        //  otherwise objects created after load will clobber loaded objects
        var maxObjectId = objectsById.keySet().stream()
            .mapToLong(Long::longValue)
            .max().orElse(0L);
        EditorObject.updateNextIdAfterLoad(maxObjectId);

        Util.log(TAG, "Parsed node graph json into current session");

        // NOTE: pan/zoom values get persisted into the settings file set in the constructor above,
        //  but it doesn't seem to persist through a new launch. Even if we read that file back on save
        //  in order to extract the scroll.x,y values, there's not an API that I can find to restore
        //  those values, so the best we can do for now is center on the loaded nodes...
        //  *but* it has to be delayed until after the nodes are drawn at least once
        //  so that the node editor has the content bounds to fit within.
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                fitToContent(0.33f);
            }
        }, 0.2f);
    }

    private Optional<Node> findNode(long id) {
        return nodes.stream().filter(node -> node.id == id).findFirst();
    }

    private Optional<Prop> findProp(long id) {
        return props.stream().filter(prop -> prop.id == id).findFirst();
    }

    private Optional<Pin> findPin(long id) {
        return pins.stream().filter(pin -> pin.id == id).findFirst();
    }

    private Optional<Link> findLink(long id) {
        return links.stream().filter(link -> link.id == id).findFirst();
    }

    private void pushWindowStyles() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding,    0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize,  0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding,    5,    5);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowTitleAlign, 0.5f, 0.5f);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding,     10f);
        ImGui.pushFont(app.imgui.getFont(DEFAULT_FONT));
    }

    private void popWindowStyles() {
        ImGui.popFont();
        ImGui.popStyleVar(5);
    }

    private void pushEditorStyles() {
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

    private void popEditorStyles() {
        NodeEditor.popStyleVar(10);
        NodeEditor.popStyleColor(4);
    }

    private Node createTestNode() {
        var node = new Node();
        new Pin(node, PinKind.INPUT, PinType.FLOW);
        new Pin(node, PinKind.OUTPUT, PinType.FLOW);
        new TestProperty(node);
        return node;
    }
}
