package example;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TestCustomPort {
    @Test
    public void testCustomPort() throws IOException {
        var url = new URL("http://localhost:9090/greet");
        try (var it = new BufferedReader(new InputStreamReader(url.openStream()))) {
            var response = it.readLine();
            assertEquals("hi", response);
        }
    }
}
