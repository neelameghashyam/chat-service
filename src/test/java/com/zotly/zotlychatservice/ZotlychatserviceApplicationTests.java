package com.zotly.zotlychatservice;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Activates test-specific configurations
class ZotlychatserviceApplicationTests {

    @MockBean // Mocks the MQTT client bean to avoid real connections
    private Mqtt5AsyncClient mqttClient;

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