package orchestrator.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

public class FogNode {

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
