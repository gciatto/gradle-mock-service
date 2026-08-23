package example;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TestLifecycle {
    @Test
    public void testStatus() throws IOException {
        var url = new URL("http://localhost:8083/status");
        try (var it = new BufferedReader(new InputStreamReader(url.openStream()))) {
            var response = it.readLine();
            assertEquals("ok", response);
        }
    }
}
