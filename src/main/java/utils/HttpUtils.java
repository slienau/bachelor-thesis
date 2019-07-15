package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int REQUEST_TIMEOUT = 5000;

    public static JsonNode httpGetRequestAsJson(String urlIn) throws IOException {
        byte[] response = httpGetRequest(urlIn);
        return OBJECT_MAPPER.readTree(response);
    }

    public static byte[] httpGetRequest(String urlIn) throws IOException {
        return httpRequest(urlIn, "GET", null);
    }

    public static JsonNode httpPutRequest(String urlIn, JsonNode requestBody) throws IOException {
        byte[] response = httpPutRequest(urlIn, OBJECT_MAPPER.writeValueAsBytes(requestBody));
        return OBJECT_MAPPER.readTree(response);
    }

    public static byte[] httpPutRequest(String urlIn, byte[] requestBody) throws IOException {
        return httpRequest(urlIn, "PUT", requestBody);
    }

    public static JsonNode httpPostRequest(String urlIn, JsonNode requestBody) throws IOException {
        byte[] response = httpPostRequest(urlIn, OBJECT_MAPPER.writeValueAsBytes(requestBody));
        return OBJECT_MAPPER.readTree(response);
    }

    public static byte[] httpPostRequest(String urlIn, byte[] requestBody) throws IOException {
        return httpRequest(urlIn, "POST", requestBody);
    }

    private static byte[] httpRequest(String urlIn, String method, byte[] requestBody) throws IOException {
        URL url = new URL(urlIn);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(REQUEST_TIMEOUT);
        connection.setReadTimeout(REQUEST_TIMEOUT);

        if (method.equals("POST") || method.equals("PUT")) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(requestBody);
            os.flush();
            os.close();
        }

        // response
        int responseCode = connection.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        StringBuffer response = new StringBuffer();
        String lineIn = null;
        while ((lineIn = in.readLine()) != null) {
            response.append(lineIn);
        }
        in.close();
        connection.disconnect();

        if (responseCode < 400) {
            return response.toString().getBytes();
        } else {
            throw new IOException(String.format("HTTP %s request to %s failed.", method, url));
        }

    }
}
