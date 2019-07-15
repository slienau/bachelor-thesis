package examples;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JacksonExample {

    public static void main(String[] args) throws Exception {
        FogNode fogNode = new FogNode();
        fogNode.setName("fog-node-12345");
        fogNode.setPing(30);
        fogNode.setIp4address("192.168.7.30");
        System.out.println("FogNode object before serialization:\n" + fogNode + "\n");

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // not for production
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        String jsonString = mapper.writeValueAsString(fogNode);
        System.out.println("JSON String:\n~~~~~~~~~\n" + jsonString + "\n");

        FogNode deserializedFogNode = mapper.readValue(jsonString, FogNode.class);
        System.out.println("deserializedFogNode:\n" + deserializedFogNode);
    }

}

class FogNode {

    @JsonProperty
    private String name;

    @JsonProperty
    private String ip4address;

    @JsonProperty
    private Integer ping;

    @Override
    public String toString() {
        return "FogNode{" +
                "name='" + name + '\'' +
                ", ip4address='" + ip4address + '\'' +
                ", ping=" + ping +
                '}';
    }

    //    @JsonGetter
    public String getName() {
        return name;
    }

    //    @JsonSetter
    public void setName(String name) {
        this.name = name;
    }

    //    @JsonGetter
    public String getIp4address() {
        return ip4address;
    }

    //    @JsonSetter
    public void setIp4address(String ip4address) {
        this.ip4address = ip4address;
    }

    //    @JsonGetter
    public Integer getPing() {
        return ping;
    }

    //    @JsonSetter
    public void setPing(Integer ping) {
        this.ping = ping;
    }
}