package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import imgui.type.ImString;
import net.bplo.nodes.imgui.ImGuiLayout;

public class PropInputText extends Prop {

    private static final String TAG = PropInputText.class.getSimpleName();

    private final ImString text = new ImString(128);

    public PropInputText(Node node) {
        this(node, "");
    }

    public PropInputText(Node node, String initialText) {
        super(node);
        init(initialText);
    }

    PropInputText(long savedId, Node node) {
        super(savedId, node);
        init(null);
    }

    private void init(String initialText) {
        this.text.set(initialText != null ? initialText : "");
    }

    @Override
    public String defaultName() {
        return "Text";
    }

    @Override
    public Object getData() {
        return getText();
    }

    @Override
    public void setData(Json json, JsonValue dataValue) {
        setText(dataValue.asString());
    }

    public String getText() {
        return text.get();
    }

    public void setText(String newText) {
        text.set(newText != null ? newText : "");
    }

    @Override
    public void render() {
        var contentWidth = node.width + 16f;
        ImGuiLayout.beginColumn(contentWidth);
        {
            ImGui.pushFont(EditorUtil.Fonts.nodeContent);
            ImGui.text(name);
            ImGui.popFont();

            ImGui.setNextItemWidth(contentWidth);
            if (ImGui.inputText("##" + label(), text)) {
                setText(text.get());
            }
        }
        ImGuiLayout.endColumn();
        bounds.update();
    }

    @Override
    public void renderInfoPane(Editor editor) {
        var label = "%s##%s_%s".formatted(name, label(), "input_text");
        if (ImGui.inputText(label, text)) {
            setText(text.get());
        }
    }
}
