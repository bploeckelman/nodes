package net.bplo.nodes.editor.utils;

import imgui.extension.nodeditor.flag.NodeEditorPinKind;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PinKind {
    INPUT(NodeEditorPinKind.Input), OUTPUT(NodeEditorPinKind.Output);
    public final int value;
}
