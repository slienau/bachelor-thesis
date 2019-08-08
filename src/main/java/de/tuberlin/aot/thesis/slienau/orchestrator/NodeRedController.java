package de.tuberlin.aot.thesis.slienau.orchestrator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.tuberlin.aot.thesis.slienau.utils.HttpUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.tuberlin.aot.thesis.slienau.utils.HttpUtils.*;

public class NodeRedController {

    private final String id;
    private String nodeRedAddress;
    private int nodeRedPort;

    // Constructors
    public NodeRedController(String id, String nodeRedAddress) {
        this(id, nodeRedAddress, 1880);
    }


    public NodeRedController(String id, String nodeRedAddress, int nodeRedPort) {
        this.id = id;
        this.nodeRedAddress = nodeRedAddress;
        this.nodeRedPort = nodeRedPort;
        System.out.println(String.format("[NodeRedController] Created new instance %s", this));
    }

    public void changeHeartbeatFrequency(String newFrequency) throws IOException {
        String flowName = "Heartbeat";

        // get flow
        JsonNode heartbeatFlow = this.getFlowByName(flowName);

        // change value
        JsonNode injectNode = heartbeatFlow.path("nodes").path(0);
        ((ObjectNode) injectNode).put("repeat", newFrequency);

        // send HTTP request containing updated flow
        this.updateFlowByName(flowName, heartbeatFlow);
    }


    /**
     * Returns the node-RED flow named like 'flowName'
     *
     * @param flowName
     * @return
     * @throws IOException
     */
    public JsonNode getFlowByName(String flowName) throws IOException {
        String flowId = this.getFlowIdByName(flowName);
        JsonNode flow = this.getFlowById(flowId);
        return flow;
    }

    public boolean deployFlow(NodeRedFlow nodeRedFlow) throws IOException {
        String flowName = nodeRedFlow.getName();
        JsonNode flow = nodeRedFlow.getFlow();
        boolean updateFlow = this.checkIfFlowExists(flowName);
        if (updateFlow) {
            // flow exists on node --> update
            this.updateFlowByName(flowName, flow);
        } else {
            // create new flow
            this.createFlow(flowName, flow);
        }
        return true;
    }

    private boolean createFlow(String flowName, JsonNode flow) throws IOException {
        if (checkIfFlowExists(flowName)) {
            System.out.println(String.format("[NodeRedController][%s] Failed to create flow '%s' because it already exists", this.getId(), flowName));
            return false;
        }
        String endpoint = getEndpointUrlForSuffix("flow/");

        httpPostRequest(endpoint, flow);
        System.out.println(String.format("[NodeRedController][%s] Created flow '%s'", this.getId(), flowName));
        return true;
    }

    public boolean deleteFlowByName(String flowName) {
        try {
            String flowId = this.getFlowIdByName(flowName);
            String endpoint = getEndpointUrlForSuffix("flow/" + flowId);
            httpDeleteRequest(endpoint);
            System.out.println(String.format("[NodeRedController][%s] Deleted flow '%s'", this.getId(), flowName));
            return true;
        } catch (Exception e) {
            System.out.println(String.format("[NodeRedController][%s] Failed to delete flow '%s'", this.getId(), flowName));
            return false;
        }
    }


    /**
     * Fetch all nodes from node-RED instance and filter them by type.
     *
     * @param nodeType
     * @return An array of node-RED node objects
     * @throws IOException
     */
    public List<JsonNode> getNodesByType(String nodeType) throws IOException {
        ArrayList<JsonNode> result = new ArrayList<>();
        Iterator<JsonNode> nodeIterator = this.getAllNodes().iterator();
        while (nodeIterator.hasNext()) {
            JsonNode node = nodeIterator.next();
            if (node.path("type").textValue().equals(nodeType)) {
                result.add(node);
            }
        }
        return result;
    }


    /**
     * Filter all flows by flowName and return the corresponding flowId if there is a flow named like flowName.
     * Throw an Exception if 0 or more than 1 flows are named like flowName.
     *
     * @param flowName
     * @return
     * @throws IOException
     */
    private String getFlowIdByName(String flowName) throws IOException {
        Map<String, String> flowIds = this.getFlowIds();

        if (!flowIds.containsValue(flowName))
            throw new IOException(String.format("Flow with name '%s' could not be found.", flowName));

        Map<String, String> filteredMap = flowIds.entrySet().stream()
                .filter(element -> element.getValue() != null)
                .filter(element -> element.getValue().equals(flowName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredMap.size() != 1)
            throw new IOException(String.format(
                    "flowName is not unique. There are %s flows named '%s'.", filteredMap.size(), flowName)
            );

        return filteredMap.keySet().stream().findFirst().get();
    }

    /**
     * Checks if a flow with 'flowName' exists on nodeRED instance
     *
     * @param flowName
     * @return true if flow exists, false otherwise
     */
    public boolean checkIfFlowExists(String flowName) {
        try {
            this.getFlowIdByName(flowName);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * Get all flow IDs and their corresponding names (label).
     *
     * @return Map with Key-Value-Pairs (flow ID, flow label)
     * @throws IOException
     */
    private Map<String, String> getFlowIds() throws IOException {
        Map<String, String> result = new HashMap<>();
        for (JsonNode tabNode : this.getNodesByType("tab")) {
            String flowId = tabNode.path("id").textValue();
            String flowLabel = tabNode.path("label").textValue();
            result.put(flowId, flowLabel);
        }
        return result;
    }


    /**
     * Send a HTTP GET request to /flows and return the result as JsonNode.
     *
     * @return An array of node-RED node objects
     * @throws IOException
     */
    private JsonNode getAllNodes() throws IOException {
        String endpoint = getEndpointUrlForSuffix("/flows");
        return httpGetRequestAsJson(endpoint);
    }


    /**
     * Send a HTTP GET request to /flow/:id and return the result as JsonNode.
     *
     * @param flowId
     * @return A node-RED flow object.
     * @throws IOException
     */
    private JsonNode getFlowById(String flowId) throws IOException {
        String endpoint = getEndpointUrlForSuffix("/flow/" + flowId);
        return httpGetRequestAsJson(endpoint);
    }


    /**
     * Update a flow with given flowName.
     *
     * @param flowName
     * @param updatedFlow
     * @return
     * @throws IOException
     */
    private JsonNode updateFlowByName(String flowName, JsonNode updatedFlow) throws IOException {
        String flowId = this.getFlowIdByName(flowName);
        JsonNode response = this.updateFlowById(flowId, updatedFlow);
        System.out.println(String.format("[NodeRedController][%s] Updated flow '%s'", this.getId(), flowName));
        return response;
    }


    /**
     * Update a flow with given flowId.
     *
     * @param flowId
     * @param updatedFlow
     * @return
     * @throws IOException
     */
    private JsonNode updateFlowById(String flowId, JsonNode updatedFlow) throws IOException {
        String endpoint = this.getEndpointUrlForSuffix("/flow/" + flowId);
        return HttpUtils.httpPutRequest(endpoint, updatedFlow);
    }


    private String getEndpointUrlForSuffix(String suffix) {
        if (suffix.startsWith("/"))
            suffix = suffix.substring(1);
        return String.format("http://%s:%s/%s", this.nodeRedAddress, this.nodeRedPort, suffix);
    }

    public String getId() {
        return id;
    }

    public String getNodeRedAddress() {
        return nodeRedAddress;
    }

    public void setNodeRedAddress(String nodeRedAddress) {
        this.nodeRedAddress = nodeRedAddress;
    }

    public int getNodeRedPort() {
        return nodeRedPort;
    }

    public void setNodeRedPort(int nodeRedPort) {
        this.nodeRedPort = nodeRedPort;
    }

    @Override
    public String toString() {
        return "NodeRedController{" +
                "id='" + id + '\'' +
                ", nodeRedAddress='" + nodeRedAddress + '\'' +
                ", nodeRedPort=" + nodeRedPort +
                '}';
    }
}
