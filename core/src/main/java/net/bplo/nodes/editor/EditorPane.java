package net.bplo.nodes.editor;

public abstract class EditorPane {

    public final Editor editor;

    public EditorPane(Editor editor) {
        this.editor = editor;
    }

    public void init() {}
    public void update(float delta) {}
    public abstract void render();
}
