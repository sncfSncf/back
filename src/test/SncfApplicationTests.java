import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SncfApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    private TestRestTemplate restTemplate;



}
