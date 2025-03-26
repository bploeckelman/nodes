package net.bplo.nodes.objects;

public abstract class EditorObject {

    public enum Type { NODE, PIN, LINK }

    private static long NEXT_ID = 1L;

    public final Type objectType;
    public final long id;

    public EditorObject(Type objectType) {
        this.objectType = objectType;
        this.id = NEXT_ID++;
    }

    public abstract void render();
}
