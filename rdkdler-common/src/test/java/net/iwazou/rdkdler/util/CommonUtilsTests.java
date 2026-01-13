package net.iwazou.rdkdler.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.stream.Stream;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import net.iwazou.rdkdler.exception.RdkHttpException;
import net.iwazou.rdkdler.exception.RdkResponseException;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CommonUtilsTests {

    @DisplayName("getBody(RdkHttpResponse)のテスト：正常系")
    @Test
    void test_getBody_01() throws RdkHttpException, RdkResponseException {
        assertThat(CommonUtils.getBody(new TestRdkHttpResponse(200, null, "body")))
                .isEqualTo("body");
    }

    @DisplayName("getBody(RdkHttpResponse)のテスト：異常系")
    @ParameterizedTest(name = "No.{index} : RdkHttpResponse=[{0}], RdkHttpResponse=[{1}]")
    @MethodSource
    void test_getBody_02(
            RdkHttpResponse response,
            Class<? extends Exception> exceptionType,
            String errorMessage) {
        assertThatExceptionOfType(exceptionType)
                .isThrownBy(() -> CommonUtils.getBody(response))
                .withMessage(errorMessage);
    }

    static Stream<Arguments> test_getBody_02() {
        return Stream.of(
                Arguments.of(
                        new TestRdkHttpResponse(500, null, "body"),
                        net.iwazou.rdkdler.exception.RdkHttpException.class,
                        "HTTP error code: 500"),
                Arguments.of(
                        new TestRdkHttpResponse(200, null, null),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "empty body"),
                Arguments.of(
                        new TestRdkHttpResponse(200, null, ""),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "empty body"),
                Arguments.of(
                        new TestRdkHttpResponse(200, null, "   "),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "empty body"));
    }

    @DisplayName("notEmptyのテスト：正常系")
    @Test
    void test_notEmpty_01() {
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            CommonUtils.notEmpty("abc");
                            CommonUtils.notEmpty(" ");
                        });
    }

    @DisplayName("notEmptyのテスト：正常系（例外スロー）")
    @ParameterizedTest(name = "No.{index} : chars=[{0}], 発生例外=[{1}], エラーメッセージ=[{2}]")
    @MethodSource
    void test_notEmpty_02(
            String chars, Class<? extends Exception> exceptionType, String errorMessage) {
        assertThatExceptionOfType(exceptionType)
                .isThrownBy(() -> CommonUtils.notEmpty(chars))
                .withMessage(errorMessage);
    }

    static Stream<Arguments> test_notEmpty_02() {
        return Stream.of(
                Arguments.of(null, java.lang.NullPointerException.class, "Validation failed"),
                Arguments.of("", java.lang.IllegalArgumentException.class, "Validation failed"));
    }

    @DisplayName("notBlankのテスト：正常系")
    @Test
    void test_notBlank_01() {
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            CommonUtils.notBlank("abc");
                        });
    }

    @DisplayName("notBlankのテスト：正常系（例外スロー）")
    @ParameterizedTest(name = "No.{index} : chars=[{0}], 発生例外=[{1}], エラーメッセージ=[{2}]")
    @MethodSource
    void test_notBlank_02(
            String chars, Class<? extends Exception> exceptionType, String errorMessage) {
        assertThatExceptionOfType(exceptionType)
                .isThrownBy(() -> CommonUtils.notBlank(chars))
                .withMessage(errorMessage);
    }

    static Stream<Arguments> test_notBlank_02() {
        return Stream.of(
                Arguments.of(null, java.lang.NullPointerException.class, "Validation failed"),
                Arguments.of("", java.lang.IllegalArgumentException.class, "Validation failed"),
                Arguments.of("   ", java.lang.IllegalArgumentException.class, "Validation failed"));
    }

    @DisplayName("isTrueのテスト：正常系")
    @Test
    void test_isTrue_01() {
        CommonUtils.isTrue(true);

        assertThatExceptionOfType(java.lang.IllegalArgumentException.class)
                .isThrownBy(() -> CommonUtils.isTrue(false))
                .withMessage("Validation failed");
    }

    @DisplayName("isBlankのテスト：正常系")
    @Test
    void test_isBlank_01() {
        assertThat(CommonUtils.isBlank(null)).isTrue();
        assertThat(CommonUtils.isBlank("")).isTrue();
        assertThat(CommonUtils.isBlank(" ")).isTrue();
        assertThat(CommonUtils.isBlank("x")).isFalse();
    }

    @DisplayName("isNotBlankのテスト：正常系")
    @Test
    void test_isNotBlank_01() {
        assertThat(CommonUtils.isNotBlank(null)).isFalse();
        assertThat(CommonUtils.isNotBlank("")).isFalse();
        assertThat(CommonUtils.isNotBlank(" ")).isFalse();
        assertThat(CommonUtils.isNotBlank("x")).isTrue();
    }

    @DisplayName("endsWithIgnoreCaseのテスト：正常系")
    @ParameterizedTest(name = "No.{index} : str=[{0}], suffix=[{1}], returns=[{2}]")
    @CsvSource(
            value = {
                "null, null, true",
                "null, def, false",
                "abcdef, null, false",
                "abcdef, def, true",
                "ABCDEF, def, true",
                "ABCDEF, cde, false",
            },
            nullValues = "null")
    void test_endsWithIgnoreCase_01(String str, String suffix, boolean returns) {
        assertThat(CommonUtils.endsWithIgnoreCase(str, suffix)).isEqualTo(returns);
    }
}
