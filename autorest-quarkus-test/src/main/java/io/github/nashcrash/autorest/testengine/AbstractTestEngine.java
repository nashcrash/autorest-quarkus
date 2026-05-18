package io.github.nashcrash.autorest.testengine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public abstract class AbstractTestEngine {
    protected List<TestCasesProperties> testCasesProperties;

    @Inject
    protected ObjectMapper objectMapper;

    @Inject
    @Any
    @Connector("smallrye-in-memory")
    protected InMemoryConnector connector;

    public void init(TestCasesProperties... testCasesProperties) {
        this.testCasesProperties = List.of(testCasesProperties);
    }

    @Test
    public void runConfiguredTests() throws Exception {
        for (TestCasesProperties testCasesProperty : testCasesProperties) {
            System.out.println("##### " + testCasesProperty.getClass().getSimpleName() + " #####");
            for (TestCasesProperties.TestCase testCase : testCasesProperty.cases()) {
                runTest(testCase);
            }
        }
    }

    /**
     * This method allows you to perform a single test, calling it by name.
     * PAY-ATTENTION: Overraid Must be public, without parameters
     *
     * @param testCasesProperties
     * @param testName
     */
    protected void singleTest(TestCasesProperties testCasesProperties, String testName) throws Exception {
        TestCasesProperties.TestCase testCase = testCasesProperties.cases().stream().filter(e -> e.name().equals(testName)).findFirst().orElse(null);
        if (testCase != null) {
            runTest(testCase);
        } else {
            throw new IllegalArgumentException("Test not found: " + testName);
        }
    }

    /**
     * This method allows you to perform all tests by TestCasesProperties
     * PAY-ATTENTION: Overraid Must be public, without parameters
     *
     * @param testCasesProperties
     */
    protected void propertiesTest(TestCasesProperties testCasesProperties) throws Exception {
        for (TestCasesProperties.TestCase testCase : testCasesProperties.cases()) {
            runTest(testCase);
        }
    }

    protected void runTest(TestCasesProperties.TestCase testCase) throws Exception {
        prepareMocks(testCase);
        beforeTest(testCase);
        executeTest(testCase);
        afterTest(testCase);
    }

    /**
     * This method implement some generic mocks.
     * Override this method to add extra mock definitions.
     *
     * @param testCase
     */
    @SuppressWarnings("unchecked")
    protected void prepareMocks(TestCasesProperties.TestCase testCase) {
    }

    /**
     * Override this method to add extra operation before test:
     * default. Prepare channels for kafka tests...
     *
     * @param testCase
     */
    protected void beforeTest(TestCasesProperties.TestCase testCase) {
        if (testCase.inputChannels().isPresent()) {
            for (String topic : testCase.inputChannels().get()) {
                InMemoryConnector.switchIncomingChannelsToInMemory(topic);
            }
        }
        if (testCase.outputChannels().isPresent()) {
            for (String topic : testCase.outputChannels().get()) {
                InMemoryConnector.switchOutgoingChannelsToInMemory(topic);
            }
        }
    }

    /**
     * Override this method to add extra operation after test:
     * default: close kafka memory test and wait 0.1 second to avoid interference with next test.
     *
     * @param testCase
     */
    protected void afterTest(TestCasesProperties.TestCase testCase) {
        InMemoryConnector.clear();
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void executeTest(TestCasesProperties.TestCase testCase) throws Exception {
        System.out.println("************ " + testCase.name() + " ************");

        if ("KAFKA".equalsIgnoreCase(testCase.type())) {
            if (testCase.toSendMessages().isPresent()) {
                for (TestCasesProperties.TestCase.MockMessage mockMessage : testCase.toSendMessages().get()) {
                    InMemorySource source = connector.source(mockMessage.channel());
                    if (mockMessage.clazz().isPresent()) {
                        try {
                            Class<?> aClass = Class.forName(mockMessage.clazz().get(), true, Thread.currentThread().getContextClassLoader());
                            Object result = getResult(mockMessage.data().orElse(null), mockMessage.fileData().orElse(null), aClass);
                            source.send(result);
                            logOp("Sent", mockMessage.channel(), result);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        source.send(mockMessage.data());
                    }
                }
            }
            if (testCase.expectedMessages().isPresent()) {
                for (TestCasesProperties.TestCase.MockMessage mockMessage : testCase.expectedMessages().get()) {
                    InMemorySink<String> sink = connector.sink(mockMessage.channel());
                    if (mockMessage.clazz().isPresent()) {
                        try {
                            Class<?> aClass = Class.forName(mockMessage.clazz().get(), true, Thread.currentThread().getContextClassLoader());
                            List list = getResultList(mockMessage.data().orElse(null), mockMessage.fileData().orElse(null), aClass);
                            List<? extends Message<String>> received = waitFor(sink, list.size(), testCase.maxWaitingMillisecondForMessages());
                            Assertions.assertEquals(list.size(), received.size());
                            int i = 0;
                            for (Object o : list) {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                                String expected = mapper.writeValueAsString(o);
                                String current = objectMapper.writeValueAsString(received.get(i++).getPayload());
                                logOp("Received", mockMessage.channel(), current);
                                MatcherAssert.assertThat(current, jsonMatcher(expected));
                            }
                            sink.clear();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        String[] split = mockMessage.data().orElse("").split(",");
                        if (split.length > 0) {
                            List<? extends Message<String>> received = waitFor(sink, split.length, testCase.maxWaitingMillisecondForMessages());
                            Assertions.assertEquals(split.length, received.size());
                            for (int i = 0; i < split.length; i++) {
                                String current = received.get(i).getPayload();
                                logOp("Received", mockMessage.channel(), current);
                                Assertions.assertEquals(split[i], current);
                            }
                        }
                    }
                }
            }
        } else {
            RequestSpecification given = io.restassured.RestAssured.given()
                    .headers(toMap(testCase.headers().orElse(null)))
                    .queryParams(toMap(testCase.query().orElse(null)));
            if (testCase.contentType().isPresent()) {
                given = given.contentType(testCase.contentType().get());
            }
            if (testCase.body().isPresent()) {
                given = given.body(testCase.body().get());
            }
            if (testCase.fileBody().isPresent()) {
                given = given.body(readFile(testCase.fileBody().get()));
            }

            // Costruzione e invocazione della richiesta
            ValidatableResponse response = given
                    .log().all()
                    .when()
                    .request(testCase.type(), testCase.url().get())
                    .then()
                    .log().all();

            if (testCase.expectedStatus().isPresent()) {
                response.statusCode(org.hamcrest.Matchers.equalTo(testCase.expectedStatus().get()));
            }
            if (testCase.expectedContentType().isPresent()) {
                response.contentType(org.hamcrest.Matchers.equalTo(testCase.expectedContentType().get()));
            }
            if (testCase.expectedBody().isPresent()) {
                response.body(jsonMatcher(testCase.expectedBody().get()));
            }
            if (testCase.expectedFileBody().isPresent()) {
                try {
                    response.body(jsonMatcher(new String(readFile(testCase.expectedFileBody().get()).readAllBytes(), StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (testCase.expectedHeaders().isPresent()) {
                Map<String, List<String>> map = toMap(testCase.expectedHeaders().orElse(null));
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    response.header(entry.getKey(), org.hamcrest.Matchers.equalTo(entry.getValue()));
                }
            }
        }
    }

    /* Get Result */
    protected <T> T getResult(Map<String, String> map, String bodyKey, String fileKey, Class<T> aClass) throws IOException {
        String getResult = map.get(bodyKey);
        String getFileResult = map.get(fileKey);
        return getResult(getResult, getFileResult, aClass);
    }

    protected <T> T getResult(String body, String file, Class<T> aClass) throws IOException {
        Object result;
        if (StringUtils.isNotEmpty(body)) {
            result = objectMapper.readValue(body, aClass);
        } else if (StringUtils.isNotEmpty(file)) {
            result = objectMapper.readValue(readFile(file), aClass);
        } else {
            result = null;
        }
        return (T) result;
    }

    protected List<?> getResultList(Map<String, String> map, String bodyKey, String fileKey, Class<?> aClass) throws IOException {
        List<?> list;
        String findResult = map.get(bodyKey);
        String findFileResult = map.get(fileKey);
        return getResultList(findResult, findFileResult, aClass);
    }

    protected List<?> getResultList(String body, String file, Class<?> aClass) throws IOException {
        List<?> list;
        if (StringUtils.isNotEmpty(body)) {
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, aClass);
            list = objectMapper.readValue(body, listType);
        } else if (StringUtils.isNotEmpty(file)) {
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, aClass);
            list = objectMapper.readValue(readFile(file), listType);
        } else {
            list = null;
        }
        return list;
    }

    /* Utilities */
    protected void logOp(String operation, String channel, Object result) {
        System.out.println("Direction: " + operation);
        System.out.println("Channel: " + channel);
        System.out.println("Message: " + result);
        System.out.println();
    }

    protected List<? extends Message<String>> waitFor(InMemorySink<String> sink, int expectedSize, Long maxtime) throws Exception {
        //Wait for receiving
        List<? extends Message<String>> received = null;
        Instant deadline = Instant.now().plus(Duration.ofMillis(maxtime));
        while (Instant.now().isBefore(deadline)) {
            // Controlla se la coda contiene il messaggio atteso
            received = sink.received();
            if (received != null && received.size() >= expectedSize) break;
            // Aspetta un po' prima di riprovare (evita di saturare la CPU)
            Thread.sleep(100);
        }
        return received;
    }

    protected Matcher<String> jsonMatcher(final String expectedValue) {
        return new Matcher<String>() {

            @Override
            public void describeTo(Description description) {
                description.appendValue(expectedValue);
            }

            @Override
            public boolean matches(Object item) {
                try {
                    JsonNode jsonNodeExpected = objectMapper.readTree(String.valueOf(expectedValue));
                    JsonNode jsonNode = objectMapper.readTree(String.valueOf(item));
                    return checkNode(jsonNodeExpected, jsonNode);
                } catch (Exception ignore) {
                }
                return false;
            }

            protected boolean checkNode(JsonNode jsonNodeExpected, JsonNode jsonNode) {
                if ((jsonNodeExpected.isNull() && !jsonNode.isNull()) || (!jsonNodeExpected.isNull() && jsonNode.isNull())) {
                    return false;
                }
                if (jsonNodeExpected.isTextual() && !jsonNode.isTextual()) {
                    return false;
                }
                if (jsonNodeExpected.isTextual() && jsonNode.isTextual()) {
                    return jsonNodeExpected.asText().equals(jsonNode.asText());
                }
                if (jsonNodeExpected.isNumber() && !jsonNode.isNumber()) {
                    return false;
                }
                if (jsonNodeExpected.isNumber() && jsonNode.isNumber()) {
                    return jsonNodeExpected.asDouble() == jsonNode.asDouble();
                }
                if (jsonNodeExpected.isBoolean() && !jsonNode.isBoolean()) {
                    return false;
                }
                if (jsonNodeExpected.isBoolean() && jsonNode.isBoolean()) {
                    return jsonNodeExpected.asBoolean() == jsonNode.asBoolean();
                }
                if (jsonNodeExpected.isObject() && !jsonNode.isObject()) {
                    return false;
                }
                if (jsonNodeExpected.isObject() && jsonNode.isObject()) {
                    for (Iterator<Map.Entry<String, JsonNode>> it = jsonNodeExpected.fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> elExpected = it.next();
                        JsonNode el = jsonNode.get(elExpected.getKey());
                        if (el == null || !checkNode(elExpected.getValue(), el)) return false;
                    }
                }
                if (jsonNodeExpected.isArray() && !jsonNode.isArray()) {
                    return false;
                }
                if (jsonNodeExpected.isArray() && jsonNode.isArray()) {
                    Iterator<JsonNode> it = jsonNode.elements();
                    for (Iterator<JsonNode> ite = jsonNodeExpected.elements(); ite.hasNext(); ) {
                        JsonNode nodeExpected = ite.next();
                        if (it.hasNext()) {
                            JsonNode node = it.next();
                            if (!checkNode(nodeExpected, node)) return false;
                        } else {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                description.appendText("was ").appendValue(item);
            }

            public String toString() {
                return StringDescription.toString(this);
            }

            @Override
            public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {
            }
        };
    }

    // Metodo per trasformare la query string in una mappa
    protected Map<String, List<String>> toMap(String query) {
        Map<String, List<String>> result = new HashMap<>();
        if (query != null) {
            String[] split = query.split("&");
            for (int i = 0; i < split.length; i++) {
                String[] attribute = split[i].split("=");
                String key = attribute[0];
                String value = URLDecoder.decode(attribute[1], StandardCharsets.UTF_8);
                List<String> list = result.getOrDefault(key, new ArrayList<>());
                list.add(value);
                result.put(key, list);
            }
        }
        return result;
    }

    protected InputStream readFile(String fileName) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileName);
        if (inputStream == null) {
            inputStream = this.getClass().getResourceAsStream(fileName);
        }
        return inputStream;
    }
}
