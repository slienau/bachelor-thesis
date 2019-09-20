package de.tuberlin.aot.thesis.slienau.orchestrator.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

public class NodeRedFlow {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String name;
    private JsonNode flow;

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

    public NodeRedFlow setDestinations(List<String> destinations) {
        if (destinations == null)
            return this;
        try {
            for (int i = 0; i < destinations.size(); i++) {
                String jsonString = OBJECT_MAPPER.writeValueAsString(flow);
                String searchString = String.format("<###DESTINATION-%s###>", i + 1);
                String replacementString = destinations.get(i);
                String replacedString = jsonString.replace(searchString, replacementString);
                this.flow = OBJECT_MAPPER.readTree(replacedString);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to set destinations");
        }
        return this;
    }

    public NodeRedFlow replaceMqttBrokerNodes(String mqttBrokerNodeId) {
        if (mqttBrokerNodeId == null)
            mqttBrokerNodeId = "9d2b2e9.fa765d"; // mqtt broker id from monitoring flow
        for (JsonNode node : flow.path("nodes")) {
            String nodeType = node.path("type").textValue();
            if (nodeType.equals("mqtt out")) {
                ((ObjectNode) node).put("broker", mqttBrokerNodeId);
            }
        }
        return this;
    }
}
