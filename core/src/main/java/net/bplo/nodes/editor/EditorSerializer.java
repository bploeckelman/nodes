package net.bplo.nodes.editor;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import imgui.extension.nodeditor.NodeEditor;
import net.bplo.nodes.Util;
import net.bplo.nodes.editor.utils.PinKind;
import net.bplo.nodes.editor.utils.PinType;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditorSerializer implements Json.Serializer<EditorSerializer.NodeList> {

    private static final String TAG = EditorSerializer.class.getSimpleName();

    public static class NodeList extends ArrayList<Node> {
        @Serial
        private static final long serialVersionUID = -7384204805940694066L;

        public NodeList() {
            super();
        }

        public NodeList(List<Node> nodes) {
            super(nodes);
        }
    }

    @Override
    public void write(Json json, NodeList nodeList, Class knownType) {
        // Start a JSON array for all nodes
        json.writeArrayStart();

        for (var node : nodeList) {
            json.writeObjectStart();

            // Write node properties
            json.writeValue("id", node.id);
            json.writeValue("width", node.width);
            json.writeValue("headerText", node.headerText);
            json.writeValue("position", NodeEditor.getNodePosition(node.id));

            // Write node pins
            json.writeArrayStart("pins");
            for (var pin : node.pins) {
                json.writeObjectStart();
                json.writeValue("id", pin.id);
                json.writeValue("kind", pin.kind.name());
                json.writeValue("type", pin.type.name());
                json.writeObjectEnd();
            }
            json.writeArrayEnd();

            // Write node props
            json.writeArrayStart("props");
            for (Prop prop : node.props) {
                json.writeObjectStart();
                json.writeValue("id", prop.id);
                json.writeValue("name", prop.name);
                json.writeValue("class", prop.getClass().getName());
                // TODO(brian): verify if this will work for all prop types
                //  or if we need to take the 'data' type into account for serde
                json.writeValue("data", prop.getData());

                // Write prop pins
                json.writeArrayStart("pins");
                for (var pin : prop.pins) {
                    json.writeObjectStart();
                    json.writeValue("id", pin.id);
                    json.writeValue("kind", pin.kind.name());
                    json.writeValue("type", pin.type.name());
                    json.writeObjectEnd();
                }
                json.writeArrayEnd();

                json.writeObjectEnd();
            }
            json.writeArrayEnd();

            // Write node links - technically only need to write one set;
            // either incoming or outgoing, but writing both for simplicity
            // and easy debugging of json data
            // NOTE: requires care when recreating the links to avoid duplication
            json.writeArrayStart("incomingLinks");
            for (var link : node.incomingLinks) {
                json.writeObjectStart();
                json.writeValue("id", link.id);
                json.writeValue("srcPinId", link.src.id);
                json.writeValue("dstPinId", link.dst.id);
                json.writeObjectEnd();
            }
            json.writeArrayEnd();
            json.writeArrayStart("outgoingLinks");
            for (var link : node.outgoingLinks) {
                json.writeObjectStart();
                json.writeValue("id", link.id);
                json.writeValue("srcPinId", link.src.id);
                json.writeValue("dstPinId", link.dst.id);
                json.writeObjectEnd();
            }
            json.writeArrayEnd();

            json.writeObjectEnd();
        }

        json.writeArrayEnd();
    }

    @Override
    public NodeList read(Json json, JsonValue jsonData, Class clazz) {
        var nodes = new NodeList();

        // Maps to store objects by id for reference during reconstruction
        var nodesById = new HashMap<Long, Node>();
        var propsById = new HashMap<Long, Prop>();
        var pinsById  = new HashMap<Long, Pin>();
        var linksById = new HashMap<Long, Link>();

        // First pass: create all nodes, props and pins
        for (var nodeValue = jsonData.child; nodeValue != null; nodeValue = nodeValue.next) {
            var id = nodeValue.getLong("id");
            var node = new Node(id);
            Util.log(TAG, "Node created: %s".formatted(node.label()));

            // Set node metadata
            node.width = nodeValue.getFloat("width", Node.DEFAULT_WIDTH);
            node.headerText = nodeValue.getString("headerText", "");

            // Set node position
            // NOTE(brian): required to maintain saved node positions on load,
            //  otherwise node locations - which are saved automatically in the
            //  editor's settings file - can get cleared and the resulting positions
            //  can be clobbered, with all the nodes piled up on top of each other
            var positionValue = nodeValue.get("position");
            if (positionValue != null) {
                var x = positionValue.getFloat("x", 0);
                var y = positionValue.getFloat("y", 0);
                NodeEditor.setNodePosition(node.id, x, y);
            }

            // Recreate node pins
            var pinsArray = nodeValue.get("pins");
            if (pinsArray != null) {
                for (var pinValue = pinsArray.child; pinValue != null; pinValue = pinValue.next) {
                    var pinId = pinValue.getLong("id");
                    var kind = PinKind.valueOf(pinValue.getString("kind"));
                    var type = PinType.valueOf(pinValue.getString("type"));

                    var pin = new Pin(pinId, node, kind, type);
                    // Don't need to add to node.pins as constructor already does this
                    Util.log(TAG, "Node Pin created: %s".formatted(pin.label()));

                    pinsById.put(pin.id, pin);
                }
            }

            // Recreate props - more complicated than other objects because of inheritance
            var propsArray = nodeValue.get("props");
            if (propsArray != null) {
                for (var propValue = propsArray.child; propValue != null; propValue = propValue.next) {
                    var propId    = propValue.getLong("id");
                    var propName  = propValue.getString("name", "");
                    var propClassName = propValue.getString("class");
                    var propDataValue = propValue.get("data");

                    // Create prop instance based on type
                    Prop prop;
                    try {
                        var propClass   = Class.forName(propClassName);
                        var constructor = propClass.getDeclaredConstructor(long.class, Node.class);
                        prop = (Prop) constructor.newInstance(propId, node);
                        prop.name = propName;

                        // TODO(brian): return to this once we have more concrete prop types,
                        //  depending how they're setup it could be trickier than this to deserialize
                        prop.setData(json, propDataValue);

                        // Don't need to add to node as constructor already does this
                        Util.log(TAG, "Prop created: %s".formatted(prop.label()));

                        propsById.put(prop.id, prop);

                        // Recreate prop pins
                        var propPinsArray = propValue.get("pins");
                        if (propPinsArray != null) {
                            for (var pinValue = propPinsArray.child; pinValue != null; pinValue = pinValue.next) {
                                var pinId = pinValue.getLong("id");
                                var kind = PinKind.valueOf(pinValue.getString("kind"));
                                var type = PinType.valueOf(pinValue.getString("type"));

                                Pin pin;
                                if (PropTest.class == propClass) {
                                    pin = new Pin(pinId, prop, kind, type);
                                } else {
                                    pin = new Pin(pinId, prop, kind, type);
                                }

                                // Don't need to add to prop.pins as constructor already does this
                                Util.log(TAG, "Prop Pin created: %s".formatted(pin.label()));

                                pinsById.put(pin.id, pin);
                            }
                        }
                    } catch (Exception e) {
                        var message = "Failed to instantiate prop: %s, error: %s".formatted(propClassName, e);
                        Util.log(TAG, message);
                        throw new GdxRuntimeException(message, e);
                    }
                }
            }

            nodes.add(node);
            nodesById.put(node.id, node);
        }

        // Recreate links - separate pass so that all nodes, pins, and props
        // are already created and can be linked on Link construction as normal
        for (var nodeValue = jsonData.child; nodeValue != null; nodeValue = nodeValue.next) {
            var nodeId = nodeValue.getLong("id");
            var node = nodesById.get(nodeId);
            if (node == null) {
                throw new GdxRuntimeException("Failed to find node #%s for link creation".formatted(nodeId));
            }

            // NOTE: only recreate outgoing links from the current node
            //  even though both incoming and outgoing links are saved
            //  in the json data for a node. Both sets of links are saved
            //  in both the source and dest nodes for easier debugging of
            //  json data, but creating both sets here would result in
            //  duplicate links in the editor.

            var outgoingLinksArray = nodeValue.get("outgoingLinks");
            if (outgoingLinksArray != null) {
                for (var linkValue = outgoingLinksArray.child; linkValue != null; linkValue = linkValue.next) {
                    var linkId   = linkValue.getLong("id");
                    var srcPinId = linkValue.getLong("srcPinId");
                    var dstPinId = linkValue.getLong("dstPinId");

                    var srcPin = pinsById.get(srcPinId);
                    var dstPin = pinsById.get(dstPinId);

                    if (srcPin != null && dstPin != null) {
                        var link = new Link(linkId, srcPin, dstPin);
                        // Don't need to add to node as constructor already does this
                        Util.log(TAG, "Link (outgoing) created: %s(%s -> %s)"
                            .formatted(link.label(), srcPin.label(), dstPin.label()));

                        linksById.put(link.id, link);
                    }
                }
            }
        }

        return nodes;
    }
}
