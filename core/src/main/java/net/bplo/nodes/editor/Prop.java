package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class Prop extends EditorObject {

    public static boolean SHOW_INFO_PANE_LABELS = false;

    @FunctionalInterface
    public interface OnChange {
        void changed(Object newValue);
    }
    private static final OnChange NO_OP_ON_CHANGE = newValue -> {};

    public String propTypeId;
    public String dependsOn;
    public OnChange onChange = NO_OP_ON_CHANGE;

    public final ImGuiWidgetBounds bounds = new ImGuiWidgetBounds();

    public final Node node;
    public final List<Pin> pins;

    public String name = "";

    public Prop(Node node) {
        super(Type.PROP);
        this.node = node;
        this.pins = new ArrayList<>();
        node.props.add(this);
    }

    /**
     * Limited access constructor intended for use by {@link EditorSerializer}
     * to create {@link Prop} instances from saved json data.
     */
    Prop(long savedId, Node node) {
        super(Type.PROP, savedId);
        this.node = node;
        this.pins = new ArrayList<>();
        node.props.add(this);
    }

    public abstract String defaultName();

    public abstract Object getData();
    public abstract void setData(Json json, JsonValue dataValue);

    public Stream<Pin> inputPins() {
        return getPins(Pin::isInput);
    }

    public Stream<Pin> outputPins() {
        return getPins(Pin::isOutput);
    }

    public Stream<Pin> getPins(Predicate<? super Pin> filter) {
        return pins.stream().filter(filter);
    }

    public abstract void render();

    public void renderPopup() {/* override if a popup is needed */}

    public void renderInfoPane(Editor editor) {
        if (editor.nodePane.showIds) {
            ImGui.textDisabled("no data");
        }
    }
}
