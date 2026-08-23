package example;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class TestMultipleRoutes {
    @Test
    public void testGetPing() throws IOException {
        var url = new URL("http://localhost:8082/ping");
        try (var it = new BufferedReader(new InputStreamReader(url.openStream()))) {
            var response = it.readLine();
            assertEquals("pong", response);
        }
    }

    @Test
    public void testPostEcho() throws IOException {
        var url = new URL("http://localhost:8082/echo");
        var connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        var body = "hello echo";
        try (OutputStream os = connection.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
        try (var it = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            var response = it.readLine();
            assertEquals(body, response);
        }
    }
}
