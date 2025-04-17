package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiStyleVar;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiLayout;

import java.util.ArrayList;
import java.util.List;

public class PropSelect extends Prop {

    private static final String TAG = PropEditableText.class.getSimpleName();

    private final Data data;

    private boolean showPopup = false;

    public PropSelect(Node node) {
        this(node, new Data());
    }

    public PropSelect(Node node, Data initialData) {
        super(node);
        this.data = initialData;
        this.name = "Select";
    }

    PropSelect(long savedId, Node node) {
        super(savedId, node);
        this.data = new Data();
        this.name = "Select";
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public void setData(Json json, JsonValue dataValue) {
        var savedData = json.readValue(Data.class, dataValue);
        this.data.set(savedData);
    }

    @Override
    public void render() {
        var contentWidth = node.width + 16f;
        ImGuiLayout.beginColumn(contentWidth);
        {
            var selectBoxWidth    = contentWidth * (2f / 3f);
            var selectButtonWidth = contentWidth * (1f / 3f);
            var contentHeight     = ImGui.getTextLineHeightWithSpacing() + 2;

            var selectBoxMin = ImGui.getCursorScreenPos();
            ImGui.dummy(selectBoxWidth, contentHeight);
            var selectBoxSize = ImGui.getItemRectSize();

            ImGui.sameLine();

            var selectButtonPos = ImGui.getCursorScreenPos();
            var selectButtonId = "%s##%s-%s-%s-arrow".formatted(name, TAG.toLowerCase(), label(), node.label());
//            if (ImGui.arrowButton(selectButtonId, ImGuiDir.Down)) {
            if (ImGui.button(selectButtonId, selectBoxWidth, 0)) {
                showPopup = true;
            }
            var selectButtonSize = ImGui.getItemRectSize();

            var drawList = ImGui.getWindowDrawList();
            drawList.addRectFilled(
                selectBoxMin.x, selectBoxMin.y,
                selectBoxMin.x + selectBoxSize.x,
                selectBoxMin.y + selectBoxSize.y,
                ImGuiColors.imBlueDark.asInt());

            drawList.addText(
                selectBoxMin.x + 10,
                selectBoxMin.y,
                ImGuiColors.white.asInt(),
                previewValue());
        }
        ImGuiLayout.endColumn();
    }

    @Override
    public void renderAfterNode() {
        // the 'show' flag needs to be toggled off after the first frame
        // and ImGui.closeCurrentPopup() is used afterwards to close it,
        // this isn't the normal pattern for popups, it's a workaround popups 'in' a node
        if (showPopup) {
            ImGui.openPopup(popupId());
            showPopup = false;
        }

        if (ImGui.beginPopup(popupId())) {
            ImGui.pushStyleColor(ImGuiCol.FrameBg, ImGuiColors.darkGray.asInt(0.2f));
            ImGui.pushStyleColor(ImGuiCol.Border, ImGuiColors.medGray.asInt(0.8f));
            ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1f);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 4f);

            // Create the visual frame
            ImGui.beginGroup();
            {
                // Draw the frame background
                var contentWidth = node.width + 16f;
                var boxHeight = ImGui.calcTextSizeY(previewValue()) * data.options.size();
                var startPos = ImGui.getCursorScreenPos();
                ImGui.dummy(contentWidth, boxHeight);
                var endPos = new ImVec2(startPos.x + contentWidth, startPos.y + boxHeight);

                // Draw background
                var drawList = ImGui.getWindowDrawList();
                drawList.addRectFilled(
                    startPos.x, startPos.y,
                    endPos.x, endPos.y,
                    ImGuiColors.darkerGray.asInt(), 2f);

                // Draw border
                drawList.addRect(
                    startPos.x, startPos.y,
                    endPos.x, endPos.y,
                    ImGuiColors.medGray.asInt(),
                    2f, 0, 1f);

                for (int i = 0; i < data.options.size(); i++) {
                    var option = data.options.get(i);
                    var selected = data.selectedIndex == i;
                    var width = node.width;
                    if (ImGui.selectable(option, selected, width, 0)) {
                        data.selectedIndex = i;
                        ImGui.closeCurrentPopup();
                    }
                }
            }
            ImGui.endGroup();

            ImGui.popStyleVar(2);
            ImGui.popStyleColor(2);

            // NOTE: doesn't do anything to the popup's open/close state, just finalizes content
            ImGui.endPopup();
        }
    }

    @Override
    public void renderInfoPane() {
        if (ImGui.beginCombo(label(), previewValue())) {
            // draw items
            for (int i = 0; i < data.options.size(); i++) {
                var option   = data.options.get(i);
                var selected = data.selectedIndex == i;
                if (ImGui.selectable(option, selected)) {
                    data.selectedIndex = i;
                }
            }

            ImGui.endCombo();
        }
    }

    private String previewValue() {
        return data.getSelectedOption();
    }

    private String popupId() {
        var niceName = name.toLowerCase()
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_|_$", "");
        return "##%s-popup-%s-%s".formatted(TAG.toLowerCase(), niceName, node.label());
    }

    public static class Data {
        public List<String> options = new ArrayList<>();
        public int selectedIndex = -1;

        public Data() {}

        public Data(int selectedIndex, List<String> options) {
            this.selectedIndex = selectedIndex;
            this.options = options;
        }

        public void set(Data newData) {
            this.selectedIndex = newData.selectedIndex;
            this.options = newData.options;
        }

        public String getSelectedOption() {
            if (!options.isEmpty() && selectedIndex >= 0 && selectedIndex < options.size()) {
                return options.get(selectedIndex);
            }
            return "";
        }
    }
}
