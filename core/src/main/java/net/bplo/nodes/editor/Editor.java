package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Disposable;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.extension.nodeditor.NodeEditor;
import imgui.extension.nodeditor.NodeEditorConfig;
import imgui.extension.nodeditor.NodeEditorContext;
import imgui.extension.nodeditor.flag.NodeEditorStyleColor;
import imgui.extension.nodeditor.flag.NodeEditorStyleVar;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.bplo.nodes.Main;
import net.bplo.nodes.objects.EditorObject;
import net.bplo.nodes.objects.Link;
import net.bplo.nodes.objects.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Editor implements Disposable {

    public static final String SETTINGS_FILE = "editor.json";
    public static final String DEFAULT_FONT = "play-regular.ttf";

    public final Main app;
    public final List<Node> nodes;
    public final List<Link> links;
    public final Map<Long, EditorObject> objectsById;
    public final NodeEditorContext context;

    public boolean addTestNode = true;

    public Editor() {
        var config = new NodeEditorConfig();
        config.setSettingsFile(SETTINGS_FILE);

        this.app = Main.app;
        this.nodes = new ArrayList<>();
        this.links = new ArrayList<>();
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
