package net.iwazou.rdkdler.area;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.Stream;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AreaPrefectureServiceMockTests {
    @Mock RdkHttpClient mockRdkHttpClient;
    private AreaPrefectureService areaPrefectureService;

    @BeforeEach
    void setUp() {
        areaPrefectureService = new AreaPrefectureService(mockRdkHttpClient);
    }

    @DisplayName("getCurrentAreaPrefectureのテスト（モック）：正常系")
    @Test
    void test_mock_getCurrentAreaPrefecture_01() throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                """
                                document.write('<span class="JP47">XXXXX</span>');\
                                """));

        AreaPrefecture area = areaPrefectureService.getCurrentAreaPrefecture();
        assertThat(area).isEqualTo(AreaPrefecture.OKINAWA);
    }

    @DisplayName("getCurrentAreaPrefectureのテスト（モック）：異常系")
    @ParameterizedTest(name = "No.{index} : str=[{0}], suffix=[{1}], returns=[{2}]")
    @MethodSource
    void test_mock_getCurrentAreaPrefecture_02(
            String body, Class<? extends Exception> exceptionType, String errorMessage)
            throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(new TestRdkHttpResponse(200, null, body));

        assertThatExceptionOfType(exceptionType)
                .isThrownBy(() -> areaPrefectureService.getCurrentAreaPrefecture())
                // 期待値：メッセージ
                .withMessage(errorMessage);
    }

    static Stream<Arguments> test_mock_getCurrentAreaPrefecture_02() {
        return Stream.of(
                Arguments.of(
                        "document.write('<span>XXXXX</span>');",
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "class attribute of <span> does not exist"),
                Arguments.of(
                        "document.write('');",
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "<span> does not exist"),
                Arguments.of(
                        """
                        document.write('<span class="JP99">XXXXX</span>');\
                        """,
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "unknown area_id"));
    }
}
