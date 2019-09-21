package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.aot.thesis.slienau.orchestrator.models.NodeRedFlow;

import java.io.IOException;

public class NodeRedFlowDatabase {
    private static final NodeRedController flowDatabaseInstance = new NodeRedController("flowDatabaseInstance", "localhost", 2880);
    private static NodeRedFlowDatabase instance;

    private NodeRedFlowDatabase() {
    }

    public static synchronized NodeRedFlowDatabase getInstance() {
        if (NodeRedFlowDatabase.instance == null)
            NodeRedFlowDatabase.instance = new NodeRedFlowDatabase();
        return NodeRedFlowDatabase.instance;
    }

    public NodeRedFlow getFlowByName(String flowName) throws IOException {
        JsonNode flow = flowDatabaseInstance.getFlowByName(flowName);
        ((ObjectNode) flow).put("disabled", false);
        return new NodeRedFlow(flowName, flow);
    }
}
