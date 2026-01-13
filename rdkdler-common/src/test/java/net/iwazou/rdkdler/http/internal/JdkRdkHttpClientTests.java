package net.iwazou.rdkdler.http.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import mockwebserver3.junit5.StartStop;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import okio.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JdkRdkHttpClientTests {
    @StartStop private MockWebServer server = new MockWebServer();
    private JdkRdkHttpClient jdkRdkHttpClient;

    @BeforeEach
    void setUp() {
        jdkRdkHttpClient =
                new JdkRdkHttpClient(
                        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build());
    }

    @DisplayName("getのテスト：正常系")
    @ParameterizedTest(
            name = "No.{index} : headers=[{0}], parameters=[{1}], reqQuery=[{2}], reqHeaders=[{3}]")
    @MethodSource
    void test_get_01(
            Map<String, String> headers,
            Map<String, String> parameters,
            String reqQuery,
            Map<String, List<String>> reqHeaders)
            throws IOException, InterruptedException {
        // レスポンスの準備
        server.enqueue(res_ok());

        RdkHttpRequest rdkHttpRequest =
                RdkHttpRequest.builder()
                        .url(server.url("/test").toString()) // URLはMockWebServerから取得
                        .headers(headers)
                        .parameters(parameters)
                        .build();
        RdkHttpResponse res = jdkRdkHttpClient.get(rdkHttpRequest);

        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.headers())
                .containsEntry("key1", List.of("value1"))
                .containsEntry("key2", List.of("value2"))
                .containsEntry("key3", List.of("value3"));
        assertThat(res.body())
                .isEqualTo(
                        """
                        {"message": "Hello from v3"}\
                        """);

        // 4. 送信されたリクエストの詳細検証
        RecordedRequest recordedRequest = server.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getTarget()).isEqualTo("/test" + reqQuery);
        assertThat(recordedRequest.getHeaders().toMultimap()).containsAllEntriesOf(reqHeaders);
        assertThat(recordedRequest.getBody()).isNull();
    }

    static Stream<Arguments> test_get_01() {
        return Stream.of(
                Arguments.of(null, null, "", Map.of()),
                Arguments.of(Map.of(), Map.of(), "", Map.of()),
                Arguments.of(
                        Map.of("hk1", "hv1", "hk2", "hv2", "hk3", "hv3"),
                        Map.of("pk1", "pv1", "pk2", "pv2", "pk3", "pv3"),
                        "?pk1=pv1&pk2=pv2&pk3=pv3",
                        Map.of(
                                "hk1", List.of("hv1"),
                                "hk2", List.of("hv2"),
                                "hk3", List.of("hv3"))));
    }

    @DisplayName("postFormのテスト：正常系")
    @ParameterizedTest(
            name = "No.{index} : headers=[{0}], parameters=[{1}], reqHeaders=[{2}], reqBody=[{3}]")
    @MethodSource
    void test_postForm_01(
            Map<String, String> headers,
            Map<String, String> parameters,
            Map<String, List<String>> reqHeaders,
            String reqBody)
            throws IOException, InterruptedException {
        // レスポンスの準備
        server.enqueue(res_ok());

        RdkHttpRequest rdkHttpRequest =
                RdkHttpRequest.builder()
                        .url(server.url("/test").toString()) // URLはMockWebServerから取得
                        .headers(headers)
                        .parameters(parameters)
                        .build();
        RdkHttpResponse res = jdkRdkHttpClient.postForm(rdkHttpRequest);

        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.headers())
                .containsEntry("key1", List.of("value1"))
                .containsEntry("key2", List.of("value2"))
                .containsEntry("key3", List.of("value3"));
        assertThat(res.body())
                .isEqualTo(
                        """
                        {"message": "Hello from v3"}\
                        """);

        // 4. 送信されたリクエストの詳細検証
        RecordedRequest recordedRequest = server.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getTarget()).isEqualTo("/test");
        assertThat(recordedRequest.getHeaders().toMultimap()).containsAllEntriesOf(reqHeaders);
        assertThat(recordedRequest.getBody()).isEqualTo(new ByteString(reqBody.getBytes()));
    }

    static Stream<Arguments> test_postForm_01() {
        return Stream.of(
                Arguments.of(null, null, Map.of(), ""),
                Arguments.of(Map.of(), Map.of(), Map.of(), ""),
                Arguments.of(
                        Map.of("hk1", "hv1", "hk2", "hv2", "hk3", "hv3"),
                        Map.of("pk1", "pv1", "pk2", "pv2", "pk3", "pv3"),
                        Map.of(
                                "hk1", List.of("hv1"),
                                "hk2", List.of("hv2"),
                                "hk3", List.of("hv3")),
                        "pk1=pv1&pk2=pv2&pk3=pv3"));
    }

    private MockResponse res_ok() {
        return new MockResponse.Builder()
                .code(200)
                .setHeader("key1", "value1")
                .setHeader("key2", "value2")
                .setHeader("key3", "value3")
                .body(
                        """
                        {"message": "Hello from v3"}\
                        """)
                .build();
    }
}
