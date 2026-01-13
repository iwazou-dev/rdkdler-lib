package net.iwazou.rdkdler.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RdkHttpResponseTests {

    @DisplayName("firstHeaderのテスト：正常系")
    @Test
    void test_firstHeader_01() {
        Map<String, List<String>> headers =
                Map.of(
                        "x-present", List.of("v1", "v2"),
                        "x-empty", List.of(),
                        "x-null", java.util.Arrays.asList((String) null));
        RdkHttpResponse response = new TestRdkHttpResponse(0, headers, null);

        assertThat(response.firstHeader("X-PRESENT").orElseThrow()).isEqualTo("v1");
        assertThat(response.firstHeader("x-empty")).isEmpty();
        assertThat(response.firstHeader("x-null")).isEmpty();
        assertThat(response.firstHeader("x-missing")).isEmpty();
    }
}
