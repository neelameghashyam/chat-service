package com.zotly.zotlychatservice;

import com.zotly.zotlychatservice.config.TestMqttConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMqttConfig.class) // Import the test configuration
class ZotlychatserviceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the application context loads successfully
    }

    @Test
    void main_shouldStartApplication() {
        // Invoke the main method with an empty args array
        ZotlychatserviceApplication.main(new String[]{});
    }
}