package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {
    private static final int REQUEST_TIMEOUT = 5000;

    public static byte[] httpGetRequest(String urlIn) throws IOException {
        return httpRequest(urlIn, "GET", null);
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

        if (method.equals("POST")) {
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
