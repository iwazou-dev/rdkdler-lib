package net.iwazou.rdkdler.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RdkResponseExceptionTests {
    @DisplayName("RdkResponseException(String)のテスト：正常系")
    @Test
    void test01() {
        RdkException rdkException = new RdkResponseException("test");
        assertThat(rdkException.getMessage()).isEqualTo("test");
        assertThat(rdkException.getCause()).isNull();
    }

    @DisplayName("RdkResponseException(String, Throwable)のテスト：正常系")
    @Test
    void test02() {
        Exception exception = new Exception("test2");
        RdkException rdkException = new RdkResponseException("test1", exception);
        assertThat(rdkException.getMessage()).isEqualTo("test1");
        assertThat(rdkException.getCause()).isEqualTo(exception);
    }
}
