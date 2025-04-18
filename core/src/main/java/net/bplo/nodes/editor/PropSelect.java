package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiStyleVar;
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
        // NOTE: this is a workaround for imgui-node-editor not supporting combo boxes in a node context,
        //  it emulates a normal combo box widget, matching the style as closely as possible.

        var contentWidth = node.width + 16f;
        ImGuiLayout.beginColumn(contentWidth);
        {
            var style = ImGui.getStyle();
            var frameHeight = ImGui.getFrameHeight();
            var itemInnerSpacing = style.getItemInnerSpacing();

            // Calculate sizes
            // Allocate portion of width to property name (similar to how ImGui lays out labels)
            var labelWidth = Math.min(ImGui.calcTextSizeX(name) + itemInnerSpacing.x, contentWidth * 0.3f);
            var arrowWidth = frameHeight; // arrow button is square
            var widgetAreaWidth = contentWidth - labelWidth;
            var previewWidth = widgetAreaWidth - arrowWidth * 2 - itemInnerSpacing.x / 2f; // NOTE: this is fiddly, but it lines things up nicely
            var comboTotalWidth = previewWidth + arrowWidth;

            // Use ImGui's style colors for combo boxes
            ImGui.pushStyleColor(ImGuiCol.FrameBg, style.getColor(ImGuiCol.FrameBg));
            ImGui.pushStyleColor(ImGuiCol.FrameBgHovered, style.getColor(ImGuiCol.FrameBgHovered));
            ImGui.pushStyleColor(ImGuiCol.FrameBgActive, style.getColor(ImGuiCol.FrameBgActive));

            // Draw the preview part to emulate the combo box
            var cursorPos = ImGui.getCursorPos();
            ImGui.beginGroup();
            {
                // Draw the frame background
                var drawList = ImGui.getWindowDrawList();
                var frameMin = ImGui.getCursorScreenPos();
                ImGui.dummy(previewWidth, frameHeight);
                var frameMax = new ImVec2(frameMin.x + previewWidth, frameMin.y + frameHeight);

                // Draw frame
                drawList.addRectFilled(
                    frameMin.x, frameMin.y,
                    frameMax.x, frameMax.y,
                    ImGui.getColorU32(ImGuiCol.FrameBg),
                    style.getFrameRounding());

                // Draw preview text
                var textPadding = style.getFramePadding();
                drawList.addText(
                    frameMin.x + textPadding.x,
                    frameMin.y + textPadding.y,
                    ImGui.getColorU32(ImGuiCol.Text),
                    previewValue());
            }
            ImGui.endGroup();

            // Position and draw the arrow button
            ImGui.sameLine(0, 0);
            if (ImGui.arrowButton("##combo_arrow" + label(), ImGuiDir.Down)) {
                showPopup = true;
            }

            // Add the property name after the combo preview and arrow button
            ImGui.sameLine(0, itemInnerSpacing.x);
            ImGui.text(name);

            // Create an invisible button over just the combo area for better clicking
            ImGui.setCursorPos(cursorPos);
            if (ImGui.invisibleButton("##combo_clickarea" + label(), comboTotalWidth, frameHeight)) {
                showPopup = true;
            }

            ImGui.popStyleColor(3);
        }
        ImGuiLayout.endColumn();
    }

    @Override
    public void renderPopup() {
        if (showPopup) {
            ImGui.openPopup(popupId());
            showPopup = false;
        }

        // Set width constraint to match combo width
        var contentWidth = node.width + 16f;
        ImGui.setNextWindowSizeConstraints(contentWidth, 0, contentWidth, 300);

        // Match standard combo popup styling
        var style = ImGui.getStyle();
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, style.getWindowPadding());
        ImGui.pushStyleVar(ImGuiStyleVar.FramePadding, style.getFramePadding());
        ImGui.pushStyleColor(ImGuiCol.PopupBg, style.getColor(ImGuiCol.PopupBg));

        if (ImGui.beginPopup(popupId())) {
            // Draw options
            for (int i = 0; i < data.options.size(); i++) {
                var option = data.options.get(i);

                // Don't pass selected=true to selectable to avoid permanent highlighting
                if (ImGui.selectable(option, data.selectedIndex == i)) {
                    data.selectedIndex = i;
                    ImGui.closeCurrentPopup();
                }

                // Set item focus but not selection
//                if (data.selectedIndex == i && ImGui.isWindowAppearing()) {
//                    ImGui.setItemDefaultFocus();
//                }
            }

            ImGui.endPopup();
        }

        ImGui.popStyleVar(2);
        ImGui.popStyleColor();
    }

//    @Override
//    public void renderPopup() {
//        if (showPopup) {
//            ImGui.openPopup(popupId());
//            showPopup = false;
//        }
//
//        // Set popup style to match the standard imgui combo box style
//        var style = ImGui.getStyle();
//        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.0f);
//        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8f, 8f);
//        ImGui.pushStyleColor(ImGuiCol.PopupBg, style.getColor(ImGuiCol.PopupBg));
//        ImGui.pushStyleColor(ImGuiCol.Border, style.getColor(ImGuiCol.Border));
//
//        // Set the popup width to match the widget width
//        var contentWidth = node.width + 16f;
//        ImGui.setNextWindowSizeConstraints(
//            contentWidth, 0,
//            contentWidth, 300);
//
//        if (ImGui.beginPopup(popupId())) {
//            // Draw combo box options as 'selectable' items
//            for (int i = 0; i < data.options.size(); i++) {
//                var option = data.options.get(i);
//                var selected = data.selectedIndex == i;
//
//                // NOTE: since this isn't a normal combo box, we pass 'false' for selected state
//                //  otherwise the selected item would always stay highlighted when the popup is open
//                if (ImGui.selectable(option, false, 0, contentWidth, 0)) {
//                    data.selectedIndex = i;
//                    ImGui.closeCurrentPopup();
//                }
//
//                // Manually set the initial focus to the selected item,
//                // allowing keyboard nav to start from the selected item
//                if (selected && ImGui.isWindowAppearing()) {
//                    ImGui.setItemDefaultFocus();
//                }
//            }
//
//            ImGui.endPopup();
//        }
//
//        ImGui.popStyleVar(2);
//        ImGui.popStyleColor(2);
//    }

    @Override
    public void renderInfoPane() {
        if (ImGui.beginCombo("%s##%s-info-pane".formatted(name, label()), previewValue())) {
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
