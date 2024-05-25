import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GenerateComposeFile {

    private static class SensorTopic {
        String publishTopic;
        String subscribeTopic;
        String delay;

        SensorTopic(String publishTopic, String subscribeTopic, String delay) {
            this.publishTopic = publishTopic;
            this.subscribeTopic = subscribeTopic;
            this.delay = delay;
        }
    }

    public static void main(String[] args) {
        List<SensorTopic> sensorTopics = new ArrayList<>();
        sensorTopics.add(new SensorTopic("sensor/1/publish", "sensor/1/subscribe", "1000"));
        sensorTopics.add(new SensorTopic("sensor/2/publish", "sensor/2/subscribe", "2000"));
        sensorTopics.add(new SensorTopic("sensor/3/publish", "sensor/3/subscribe", "3000"));
        sensorTopics.add(new SensorTopic("sensor/4/publish", "sensor/4/subscribe", "4000"));
        // Add more topics as needed

        try {
            String template = new String(Files.readAllBytes(Paths.get("docker-compose-template.yml")));

            StringBuilder sensorsConfig = new StringBuilder();
            for (int i = 0; i < sensorTopics.size(); i++) {
                SensorTopic sensor = sensorTopics.get(i);
                sensorsConfig.append(String.format(
                    "  my-app-%d:\n" +
                    "    image: openjdk:latest\n" +
                    "    container_name: javasensor-%d\n" +
                    "    build:\n" +
                    "      context: ./my-app\n" +
                    "      dockerfile: Dockerfile\n" +
                    "    environment:\n" +
                    "      - PUBLISH_TOPIC=%s\n" +
                    "      - SUBSCRIBE_TOPIC=%s\n" +
                    "      - DELAY=%s\n" +
                    "      - MQTT_BROKER_HOST=mqtt-broker\n" +
                    "    networks:\n" +
                    "      - mqtt_network\n" +
                    "    command: [\"java\", \"-jar\", \"my-app-1.0-SNAPSHOT-jar-with-dependencies.jar\", \"%s\", \"%s\", \"%s\", \"mqtt-broker\"]\n",
                    i + 1, i + 1, sensor.publishTopic, sensor.subscribeTopic, sensor.delay, sensor.publishTopic, sensor.subscribeTopic, sensor.delay
                ));
            }

            String finalCompose = template.replace("# Sensor containers will be added here by the script", sensorsConfig.toString());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("docker-compose.yml"))) {
                writer.write(finalCompose);
            }

            System.out.println("docker-compose.yml generated successfully.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
