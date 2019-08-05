package de.tuberlin.aot.thesis.slienau.examples;

import static de.tuberlin.aot.thesis.slienau.utils.HttpUtils.httpGetRequest;
import static de.tuberlin.aot.thesis.slienau.utils.HttpUtils.httpPostRequest;

public class HttpExample {
    public static void main(String[] args) throws Exception {
        String DNR_ADDRESS = "http://127.0.0.1:1818";
        String DNR_FLOWS_GET_ENDPOINT = DNR_ADDRESS + "/flows";
        String DNR_FLOWS_POST_ENDPOINT = DNR_ADDRESS + "/dnr/flows/"; // + flowId, e.g. '18a1ac7b.4c8524'

        String dnrFlows = new String(httpGetRequest(DNR_FLOWS_GET_ENDPOINT));
        System.out.println(String.format("HTTP GET response: %s", dnrFlows));

        String postUrl = "https://jsonplaceholder.typicode.com/posts";
        String postBody = "{\"userId\":1,\"title\":\"qui est esse\",\"body\":\"est rerum tempore vitae\\nsequi sint nihil reprehenderit dolor beatae ea dolores neque\\nfugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis\\nqui aperiam non debitis possimus qui neque nisi nulla\",\"id\":101}";
        String httpPostTest = new String(httpPostRequest(postUrl, postBody.getBytes()));
        System.out.println(String.format("HTTP POST test response: %s", httpPostTest));
    }
}
