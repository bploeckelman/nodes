package net.bplo.nodes.editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.bplo.nodes.imgui.ImGuiColors;
import net.bplo.nodes.imgui.ImGuiLayout;
import net.bplo.nodes.imgui.ImGuiWidgetBounds;

import java.util.ArrayList;

public class PropEditableText extends Prop {

    private static final int MAX_VISIBLE_LINES = 5;

    private final ImString text = new ImString(4096);
    private final ImGuiWidgetBounds bounds = new ImGuiWidgetBounds();
    private final ImInt scrollPos = new ImInt(0);

    private String[] previewLines = new String[0];
    private int totalLines = 0;
    private boolean firstRender = true;

    public PropEditableText(Node node) {
        this(node, "");
    }

    public PropEditableText(Node node, String initialText) {
        super(node);
        init(initialText);
    }

    PropEditableText(long savedId, Node node, String initialText) {
        super(savedId, node);
        init(initialText);
    }

    private void init(String initialText) {
        this.text.set(initialText != null ? initialText : "");
        this.text.inputData.isResizable = true;
        updatePreviewLines();
    }

    public String getText() {
        return text.get();
    }

    public void setText(String newText) {
        this.text.set(newText != null ? newText : "");
        updatePreviewLines();
    }

    @Override
    public void render() {
        if (firstRender) {
            firstRender = false;
            updatePreviewLines();
        }

        ImGui.beginGroup();
        {
            var contentWidth = node.width + 16f;
            ImGuiLayout.beginColumn(contentWidth);
            {
                // Draw text box background with border
                var lineHeight = ImGui.getTextLineHeightWithSpacing();
                var visibleLines = Math.min(totalLines, MAX_VISIBLE_LINES);
                if (visibleLines < 1) visibleLines = 1;
                var boxHeight = visibleLines * lineHeight;

                ImGui.pushStyleColor(ImGuiCol.FrameBg, ImGuiColors.darkGray.asInt(0.2f));
                ImGui.pushStyleColor(ImGuiCol.Border, ImGuiColors.medGray.asInt(0.8f));
                ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1f);
                ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 4f);

                // Create the visual frame
                ImGui.beginGroup();
                {
                    // Draw the frame background
                    var startPos = ImGui.getCursorScreenPos();
                    ImGui.dummy(contentWidth, boxHeight);
                    var endPos = new ImVec2(startPos.x + contentWidth, startPos.y + boxHeight);

                    // We'll use the draw list to draw both the background and the text
                    var drawList = ImGui.getWindowDrawList();

                    // Draw background
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

                    // Handle scrolling with mouse wheel
                    if (ImGui.isItemHovered()) {
                        var wheel = ImGui.getIO().getMouseWheel();
                        if (wheel != 0) {
                            var maxScroll = Math.max(0, totalLines - MAX_VISIBLE_LINES);
                            scrollPos.set(Math.max(0, Math.min(maxScroll, scrollPos.get() - (int) wheel)));

                            // consume the scroll event to prevent it from zooming the node graph
                            ImGui.getIO().setMouseWheel(0);
                        }
                    }

                    // Draw visible text lines
                    var startLine = scrollPos.get();
                    var endLine = Math.min(startLine + MAX_VISIBLE_LINES, previewLines.length);
                    var textY = startPos.y + 5f; // Initial padding

                    ImGui.pushFont(EditorUtil.Fonts.small);
                    var textColor = ImGuiColors.lightGray.asInt();
                    var textLineHeight = ImGui.getTextLineHeightWithSpacing();
                    for (int i = startLine; i < endLine; i++) {
                        var lineText = previewLines[i];
                        drawList.addText(startPos.x + 5f, textY, textColor, lineText);
                        textY += textLineHeight;
                    }
                    ImGui.popFont();

                    // Draw scrollbar if needed
                    if (totalLines > MAX_VISIBLE_LINES) {
                        var scrollbarWidth = 8f;
                        var scrollbarX = endPos.x - scrollbarWidth - 3f;
                        var scrollStart = startLine / (float) totalLines;
                        var scrollSize = Math.min(1f, (float) MAX_VISIBLE_LINES / totalLines);

                        // Draw scrollbar background
                        drawList.addRectFilled(
                            scrollbarX, startPos.y,
                            scrollbarX + scrollbarWidth, endPos.y,
                            ImGuiColors.darkGray.asInt(0.3f),
                            2f
                        );

                        // Draw scrollbar thumb
                        var thumbY = startPos.y + (boxHeight * scrollStart);
                        var thumbHeight = boxHeight * scrollSize;

                        drawList.addRectFilled(
                            scrollbarX, thumbY,
                            scrollbarX + scrollbarWidth, thumbY + thumbHeight,
                            ImGuiColors.medGray.asInt(0.8f),
                            2f
                        );
                    }
                }
                ImGui.endGroup();

                ImGui.popStyleVar(2);
                ImGui.popStyleColor(2);
            }
            ImGuiLayout.endColumn();
        }
        ImGui.endGroup();
        bounds.update();
    }

    @Override
    public void renderInfoPane() {
        ImGui.pushID("editabletext_" + id);
        ImGui.separator();
        ImGui.text("Edit Text");

        // Full-sized multiline text editor in info pane
        var avail = ImGui.getContentRegionAvail();
        var flags = ImGuiInputTextFlags.AllowTabInput;
        if (ImGui.inputTextMultiline("##content", text, avail.x, avail.y - 30, flags)) {
            setText(text.get());
        }
        ImGui.popID();
    }

    private void updatePreviewLines() {
        var width = node.width;

        // Get the string contents
        var str = text.get();
        if (str.isEmpty()) {
            previewLines = new String[]{"..."};
            totalLines = 1;
            return;
        }

        // Split by existing line breaks
        var lines = str.split("\n", -1);

        previewLines = applyWordWrapping(lines, width);
        totalLines = previewLines.length;

        // Reset scroll position if needed
        if (scrollPos.get() > Math.max(0, totalLines - MAX_VISIBLE_LINES)) {
            scrollPos.set(Math.max(0, totalLines - MAX_VISIBLE_LINES));
        }
    }

    private String[] applyWordWrapping(String[] rawLines, float maxWidth) {
        // Estimate the number of lines after wrapping
        var wrappedLines = new ArrayList<String>();
        var count = 0;

        for (String line : rawLines) {
            // Skip empty line processing
            if (line.isEmpty()) {
                wrappedLines.add("");
                continue;
            }

            // Process line wrapping
            int start = 0;
            while (start < line.length()) {
                // Find how much text fits on this line
                int end = findBreakPoint(line, start, maxWidth);

                // Add the wrapped text
                wrappedLines.add(line.substring(start, end));

                // Move to next segment
                start = end;

                // Skip whitespace at the beginning of continued lines
                while (start < line.length() && Character.isWhitespace(line.charAt(start))) {
                    start++;
                }

                // Safety check - limit total lines
                count++;
                if (count >= 100) {
                    wrappedLines.add("...");
                    return wrappedLines.toArray(new String[0]);
                }
            }
        }

        return wrappedLines.toArray(new String[0]);
    }

    private int findBreakPoint(String text, int start, float maxWidth) {
        // Similar implementation to your existing method
        int textLength = text.length();
        int end = start;

        // Try to fit as much text as possible
        while (end < textLength) {
            int nextEnd = end + 1;

            // Make sure calcTextSize uses the correct font
            ImGui.pushFont(EditorUtil.Fonts.small);
            float textWidth = ImGui.calcTextSize(text.substring(start, nextEnd)).x;
            ImGui.popFont();

            if (textWidth > maxWidth) {
                // Find a good break point
                if (end > start) {
                    int breakPoint = end;
                    while (breakPoint > start && !Character.isWhitespace(text.charAt(breakPoint - 1))) {
                        breakPoint--;
                    }

                    if (breakPoint > start) {
                        return breakPoint;
                    }
                }

                return Math.max(start + 1, end);
            }

            end = nextEnd;
        }

        return textLength;
    }
}
