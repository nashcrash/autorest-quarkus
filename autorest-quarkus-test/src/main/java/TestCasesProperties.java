import io.smallrye.config.WithDefault;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TestCasesProperties {
    List<TestCase> cases();

    interface TestCase {
        String name();

        @WithDefault("GET")
        String type();

        Optional<String> url();

        Optional<String> query();

        Optional<String> body();

        Optional<String> fileBody();

        Optional<String> headers();

        Optional<String> contentType();

        Optional<Integer> expectedStatus();

        Optional<String> expectedContentType();

        Optional<String> expectedBody();

        Optional<String> expectedFileBody();

        Optional<String> expectedHeaders();

        Optional<List<Mock>> mocks();

        Optional<List<String>> inputChannels();

        Optional<List<String>> outputChannels();

        Optional<List<MockMessage>> toSendMessages();

        Optional<List<MockMessage>> expectedMessages();

        @WithDefault("5000")
        Long maxWaitingMillisecondForMessages();

        interface Mock {
            String type();

            Optional<String> name();

            Map<String, String> config();
        }

        interface MockMessage {
            String channel();

            Optional<String> data();

            Optional<String> fileData();

            Optional<String> clazz();
        }
    }
}
