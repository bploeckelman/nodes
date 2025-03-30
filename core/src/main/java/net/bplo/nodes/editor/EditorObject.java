package net.bplo.nodes.editor;

public abstract class EditorObject {

    public enum Type { NODE, PIN, LINK, PROP }

    private static long NEXT_ID = 1L;

    public final Type objectType;
    public final long id;

    public EditorObject(Type objectType) {
        this.objectType = objectType;
        this.id = NEXT_ID++;
    }

    public String label() {
        return "%s-%d".formatted(objectType.name().toLowerCase(), id);
    }

    public abstract void render();
    public void renderAfterNode() {}
}
