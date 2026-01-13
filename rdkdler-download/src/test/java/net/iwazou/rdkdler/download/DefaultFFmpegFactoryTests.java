package net.iwazou.rdkdler.download;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultFFmpegFactoryTests {
    @DisplayName("create()のテスト：正常系")
    @Test
    void test_create_01() {
        DefaultFFmpegFactory factory = new DefaultFFmpegFactory();
        FFmpeg fFmpeg = factory.create();
        assertThat(fFmpeg).isNotNull();
    }
}
