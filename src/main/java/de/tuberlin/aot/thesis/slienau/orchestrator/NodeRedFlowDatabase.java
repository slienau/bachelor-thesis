package de.tuberlin.aot.thesis.slienau.orchestrator;

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
        return new NodeRedFlow(flowName, flowDatabaseInstance.getFlowByName(flowName));
    }
}
