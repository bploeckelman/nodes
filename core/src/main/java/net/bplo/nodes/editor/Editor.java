package net.bplo.nodes.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.Timer;
import imgui.ImGui;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import net.bplo.nodes.Main;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.meta.AssetMetadata;
import net.bplo.nodes.editor.meta.MetadataRegistry;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;
import net.bplo.nodes.imgui.ImGuiPlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Editor implements Disposable {

    public static final String TAG = Editor.class.getSimpleName();
    public static final String SETTINGS_FILE = "editor.json";
    public static final String DEFAULT_FONT = "play-regular.ttf";

    private final Json json;
    private final NodeEditorContext editorContext;

    public final Main app;
    public final List<Node> nodes;
    public final List<Prop> props;
    public final List<Pin>  pins;
    public final List<Link> links;
    public final Map<Long, EditorObject> objectsById;

    final EditorInfoPane infoPane;
    final EditorNodePane nodePane;

    AssetMetadata assetMetadata; // TODO(brian): remove me
    MetadataRegistry metadataRegistry;

    public Editor() {
        EditorContent.refresh();

        var config = new NodeEditorConfig();
        config.setSettingsFile(SETTINGS_FILE);

        this.app = Main.app;
        this.nodes = new ArrayList<>();
        this.props = new ArrayList<>();
        this.pins = new ArrayList<>();
        this.links = new ArrayList<>();
        this.objectsById = new HashMap<>();
        this.editorContext = NodeEditor.createEditor(config);

        NodeEditor.setCurrentEditor(editorContext);
        // NOTE: disabling key shortcuts because 'f' is bound to zoom by default
        //  and I can't find an easy way to override it when doing text input
        NodeEditor.enableShortcuts(false);

        this.json = new Json();
        json.setSerializer(EditorSerializer.NodeList.class, new EditorSerializer());

        this.infoPane = new EditorInfoPane(this);
        this.nodePane = new EditorNodePane(this);
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
    public void update() {
        var delta = ImGui.getIO().getDeltaTime();
        infoPane.update(delta);
        nodePane.update(delta);
    }

    public void render() {
        ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), ImGuiDockNodeFlags.PassthruCentralNode);
        NodeEditor.setCurrentEditor(editorContext);
        pushStyles();
        nodePane.render();
        infoPane.render();
        popStyles();
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

    void loadAssetMetadata(String filePath) {
        metadataRegistry = new MetadataRegistry(filePath);
        for (var nodeType : metadataRegistry.getNodeTypes()) {
            nodePane.nodeTypes.put(nodeType.name, nodeType);
        }
    }

    void save() {
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

    void load() {
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
            node.pins.forEach(pin -> objectsById.put(pin.id, pin));

            props.addAll(node.props);
            node.props.forEach(prop -> {
                objectsById.put(prop.id, prop);

                pins.addAll(prop.pins);
                prop.pins.forEach(pin -> objectsById.put(pin.id, pin));
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

    Optional<Node> findNode(long id) {
        return nodes.stream().filter(node -> node.id == id).findFirst();
    }

    Optional<Prop> findProp(long id) {
        return props.stream().filter(prop -> prop.id == id).findFirst();
    }

    Optional<Pin> findPin(long id) {
        return pins.stream().filter(pin -> pin.id == id).findFirst();
    }

    Optional<Link> findLink(long id) {
        return links.stream().filter(link -> link.id == id).findFirst();
    }

    private void pushStyles() {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding,    0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize,  0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding,    5,    5);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowTitleAlign, 0.5f, 0.5f);
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding,     10f);
        ImGui.pushFont(app.imgui.getFont(DEFAULT_FONT));
    }

    private void popStyles() {
        ImGui.popFont();
        ImGui.popStyleVar(5);
    }

    Node createTestNode() {
        var node = new Node();
        node.headerText = "Dialogue";
        new Pin(node, PinKind.INPUT, PinType.FLOW);
        new Pin(node, PinKind.OUTPUT, PinType.FLOW);
        new PropTest(node);
        new PropInteger(node);
        new PropFloat(node);
        new PropSelect(node, new PropSelect.Data(0, List.of("Foo", "Bar", "Baz", "Buzz", "Booze", "Quux")));
        var editableText = new PropEditableText(node);
        editableText.name = "Text";
        editableText.setText("""
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed pretium, purus nec ullamcorper dictum,
        felis purus accumsan enim, ac tristique tellus orci id dolor.

        Praesent id felis sit amet magna varius congue at ac felis. Vivamus elit sapien, imperdiet id sem blandit,
        sodales tincidunt velit. Nulla blandit, lorem in maximus ullamcorper, dolor justo interdum elit,
        vitae pretium eros nibh a est.
        """);

        return node;
    }
}
