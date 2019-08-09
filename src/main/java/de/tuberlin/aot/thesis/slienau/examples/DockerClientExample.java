package de.tuberlin.aot.thesis.slienau.examples;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.List;

public class DockerClientExample {
    public static void main(String[] args) {
        DefaultDockerClientConfig config
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://raspi-01:52376").build();

        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        List<Container> containers = dockerClient.listContainersCmd()
                .withShowSize(true)
                .withShowAll(true)
                .exec();
        System.out.println(containers.get(0).getImage());
    }
}
