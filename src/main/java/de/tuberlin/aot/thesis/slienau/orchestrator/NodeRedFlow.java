package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;

public class NodeRedFlow {
    private final String name;
    private final JsonNode flow;

    public NodeRedFlow(String name, JsonNode flow) {
        this.name = name;
        this.flow = flow;
    }

    public String getName() {
        return name;
    }

    public JsonNode getFlow() {
        return flow;
    }
}
