package net.iwazou.rdkdler.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RdkDownloadExceptionTests {
    @DisplayName("RdkDownloadException(Throwable)のテスト：正常系")
    @Test
    void test01() {
        Exception exception = new Exception("test");
        RdkException rdkException = new RdkDownloadException(exception);
        assertThat(rdkException.getMessage()).isEqualTo("java.lang.Exception: test");
        assertThat(rdkException.getCause()).isEqualTo(exception);
    }
}
