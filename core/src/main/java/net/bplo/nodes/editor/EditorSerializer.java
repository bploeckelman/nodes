package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import net.bplo.nodes.objects.Node;

import java.util.List;

public class EditorSerializer implements Json.Serializer<List<Node>> {

    private static final String TAG = EditorSerializer.class.getSimpleName();

    public static final EditorSerializer instance = new EditorSerializer();

    @Override
    public void write(Json json, List<Node> nodes, Class knownType) {

    }

    @Override
    public List<Node> read(Json json, JsonValue jsonData, Class type) {
        return null;
    }
}
