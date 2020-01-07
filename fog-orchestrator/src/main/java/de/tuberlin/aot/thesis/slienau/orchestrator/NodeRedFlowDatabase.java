package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.NodeRedFlow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class NodeRedFlowDatabase {
    private static final NodeRedController flowDatabaseInstance = new NodeRedController("flowDatabaseInstance", "raspi-02.lan", 2880);
    private static NodeRedFlowDatabase instance;
    private static HashMap<String, JsonNode> nodes;

    private NodeRedFlowDatabase() {
        nodes = new HashMap<>();
        NodeRedFlowDatabase.updatesNodes();
    }

    public static synchronized NodeRedFlowDatabase getInstance() {
        if (NodeRedFlowDatabase.instance == null)
            NodeRedFlowDatabase.instance = new NodeRedFlowDatabase();
        return NodeRedFlowDatabase.instance;
    }

    public static NodeRedFlow getFlowByName(String flowName) throws IOException {
        JsonNode flow = flowDatabaseInstance.getFlowByName(flowName);
        ((ObjectNode) flow).put("disabled", false);
        return new NodeRedFlow(flowName, flow);
    }

    private static synchronized void updatesNodes() {
        try {
            for (JsonNode node : flowDatabaseInstance.getAllNodes()) {
                nodes.put(node.path("id").asText(), node);
            }
            System.out.println("[Flow Database] Update nodes complete " + nodes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<JsonNode> getNodesByType(String nodeType) {
        ArrayList<JsonNode> result = new ArrayList<>();
        for (JsonNode node : nodes.values()) {
            if (node.path("type").textValue().equals(nodeType)) {
                result.add(node);
            }
        }
        return result;
    }

    public static List<String> getFlows() {
        return getNodesByType("tab").stream().map(jsonNode -> jsonNode.path("label").asText()).collect(Collectors.toList());
    }
}
