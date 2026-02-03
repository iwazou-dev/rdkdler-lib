package net.iwazou.rdkdler.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.Input;
import com.github.kokorin.jaffree.ffmpeg.Output;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.process.JaffreeAbnormalExitException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import net.iwazou.rdkdler.download.RdkAuthenticator.AuthResult;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RdkDownloadServiceMockTests {
    @Mock RdkHttpClient mockRdkHttpClient;
    @Mock RdkAuthenticator mockRdkAuthenticator;
    @Mock FFmpegFactory mockFFmpegFactory;
    @Mock FFmpeg mockFFmpeg;
    private RdkDownloadService rdkdlerDownloader;

    @Captor ArgumentCaptor<UrlInput> inputCaptor;
    @Captor ArgumentCaptor<UrlOutput> outputCaptor;
    @Captor ArgumentCaptor<RdkHttpRequest> rdkHttpRequestCaptor;

    @TempDir Path tempDir; // テストごとに空の一時ディレクトリが割り当てられ、終了時に削除される

    @BeforeEach
    void setUp() {
        this.rdkdlerDownloader = new RdkDownloadService(mockRdkAuthenticator, mockFFmpegFactory);
    }

    @DisplayName("download(String, LocalDateTime, LocalDateTime, Path)のテスト（モック）：正常系）")
    @Test
    void test_download_01() throws IOException, InterruptedException {

        // モックの設定
        when(mockRdkAuthenticator.auth()).thenReturn(new AuthResult("authtoken", "areaId"));
        when(mockRdkAuthenticator.getRdkHttpClient()).thenReturn(mockRdkHttpClient);
        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                """
                                <?xml version="1.0" encoding="UTF-8" ?>
                                <urls>
                                    <url areafree="0" max_delay="60" timefree="0">
                                        <playlist_create_url>https://a.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="1">
                                        <playlist_create_url>https://b.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="0">
                                        <playlist_create_url>https://c.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="1">
                                        <playlist_create_url>https://d.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="1">
                                        <playlist_create_url>https://e.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="0">
                                        <playlist_create_url>https://f.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="0">
                                        <playlist_create_url>https://g.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="1">
                                        <playlist_create_url>https://h.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                </urls>
                                """));
        when(mockRdkHttpClient.options(any(RdkHttpRequest.class)))
                .thenReturn(new TestRdkHttpResponse(200, null, null));
        when(mockFFmpegFactory.create()).thenReturn(mockFFmpeg);
        when(mockFFmpeg.addInput(any(Input.class))).thenReturn(mockFFmpeg);
        when(mockFFmpeg.addOutput(any(Output.class))).thenReturn(mockFFmpeg);
        when(mockFFmpeg.setOverwriteOutput(anyBoolean())).thenReturn(mockFFmpeg);

        String stationId = "STATION";
        LocalDateTime from = LocalDateTime.parse("2025-12-22T10:00:00");
        LocalDateTime to = LocalDateTime.parse("2025-12-22T10:05:00");
        Path path = tempDir.resolve("テスト.m4a");

        // テスト対象の呼び出し
        rdkdlerDownloader.download(stationId, from, to, path);

        // 呼び出し回数のチェック
        verify(mockFFmpeg, times(1)).addInput(inputCaptor.capture());
        verify(mockFFmpeg, times(1)).addOutput(outputCaptor.capture());
        verify(mockFFmpeg, times(1)).setOverwriteOutput(anyBoolean());
        verify(mockFFmpeg, times(1)).execute();

        assertThat(inputCaptor.getValue().buildArguments()).hasSize(8);
        // 最初の７個のチェック
        assertThat(inputCaptor.getValue().buildArguments().subList(0, 7))
                .containsExactly(
                        "-fflags",
                        "+discardcorrupt",
                        "-headers",
                        "X-Radiko-AreaId: areaId\r\nX-Radiko-AuthToken: authtoken",
                        "-http_seekable",
                        "0",
                        "-i");
        // lsidはランダムなのでパターンマッチでチェック
        String urlPattern =
                "https://.+/playlist\\.m3u8\\?"
                        + "station_id=STATION&start_at=20251222100000&ft=20251222100000&"
                        + "end_at=20251222100500&to=20251222100500&seek=20251222100000&"
                        + "l=15&lsid=[a-f0-9]+&type=c";
        assertThat(inputCaptor.getValue().buildArguments().get(7)).matches(urlPattern);

        assertThat(outputCaptor.getValue().buildArguments())
                .hasSize(7)
                .containsExactly(
                        "-map", "0:a", "-c:a", "copy", "-bsf:a", "aac_adtstoasc", path.toString());
    }

    @DisplayName("download(String, LocalDateTime, LocalDateTime, Path, String)のテスト（モック）：正常系）")
    @Test
    void test_download_02() throws IOException, InterruptedException {

        // モックの設定
        when(mockRdkAuthenticator.auth()).thenReturn(new AuthResult("authtoken", "areaId"));
        when(mockRdkAuthenticator.getRdkHttpClient()).thenReturn(mockRdkHttpClient);
        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                """
                                <?xml version="1.0" encoding="UTF-8" ?>
                                <urls>
                                    <url areafree="0" max_delay="60" timefree="0">
                                        <playlist_create_url>https://a.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="1">
                                        <playlist_create_url>https://b.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="0">
                                        <playlist_create_url>https://c.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="1">
                                        <playlist_create_url>https://d.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="1">
                                        <playlist_create_url>https://e.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="0">
                                        <playlist_create_url>https://f.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="0">
                                        <playlist_create_url>https://g.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="1">
                                        <playlist_create_url>https://h.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                </urls>
                                """));
        when(mockRdkHttpClient.options(any(RdkHttpRequest.class)))
                .thenReturn(new TestRdkHttpResponse(200, null, null));
        when(mockFFmpegFactory.create()).thenReturn(mockFFmpeg);
        when(mockFFmpeg.addInput(any(Input.class))).thenReturn(mockFFmpeg);
        when(mockFFmpeg.addOutput(any(Output.class))).thenReturn(mockFFmpeg);
        when(mockFFmpeg.setOverwriteOutput(anyBoolean())).thenReturn(mockFFmpeg);

        String stationId = "STATION";
        LocalDateTime from = LocalDateTime.parse("2025-12-22T10:00:00");
        LocalDateTime to = LocalDateTime.parse("2025-12-22T10:05:00");
        Path path = tempDir.resolve("テスト.m4a");
        String coverUrl = "xxxxx.jpg";

        // テスト対象の呼び出し
        rdkdlerDownloader.download(stationId, from, to, path, coverUrl);

        // 呼び出し回数のチェック
        verify(mockFFmpeg, times(2)).addInput(inputCaptor.capture());
        verify(mockFFmpeg, times(1)).addOutput(outputCaptor.capture());
        verify(mockFFmpeg, times(1)).setOverwriteOutput(anyBoolean());
        verify(mockFFmpeg, times(1)).execute();

        var input1 = inputCaptor.getAllValues().get(0);
        assertThat(input1.buildArguments()).hasSize(8);
        // 最初の７個のチェック
        assertThat(input1.buildArguments().subList(0, 7))
                .containsExactly(
                        "-fflags",
                        "+discardcorrupt",
                        "-headers",
                        "X-Radiko-AreaId: areaId\r\nX-Radiko-AuthToken: authtoken",
                        "-http_seekable",
                        "0",
                        "-i");
        // lsidはランダムなのでパターンマッチでチェック
        String urlPattern =
                "https://.+/playlist\\.m3u8\\?"
                        + "station_id=STATION&start_at=20251222100000&ft=20251222100000&"
                        + "end_at=20251222100500&to=20251222100500&seek=20251222100000&"
                        + "l=15&lsid=[a-f0-9]+&type=c";
        assertThat(input1.buildArguments().get(7)).matches(urlPattern);
        var input2 = inputCaptor.getAllValues().get(1);
        assertThat(input2.buildArguments()).hasSize(2).containsExactly("-i", "xxxxx.jpg");

        assertThat(outputCaptor.getValue().buildArguments())
                .hasSize(13)
                .containsExactly(
                        "-map",
                        "0:a",
                        "-c:a",
                        "copy",
                        "-bsf:a",
                        "aac_adtstoasc",
                        "-map",
                        "1:v",
                        "-c:v",
                        "mjpeg",
                        "-disposition:v:0",
                        "attached_pic",
                        path.toString());
    }

    @DisplayName("download(String, LocalDateTime, LocalDateTime, Path)のテスト（モック）：異常系（例外発生時））")
    @Test
    void test_download_03() throws IOException, InterruptedException {

        // モックの設定
        when(mockRdkAuthenticator.auth()).thenReturn(new AuthResult("authtoken", "areaId"));
        when(mockRdkAuthenticator.getRdkHttpClient()).thenReturn(mockRdkHttpClient);
        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                """
                                <?xml version="1.0" encoding="UTF-8" ?>
                                <urls>
                                    <url areafree="0" max_delay="60" timefree="0">
                                        <playlist_create_url>https://a.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="1">
                                        <playlist_create_url>https://b.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="0">
                                        <playlist_create_url>https://c.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="1">
                                        <playlist_create_url>https://d.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="1">
                                        <playlist_create_url>https://e.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="0">
                                        <playlist_create_url>https://f.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="1" max_delay="60" timefree="0">
                                        <playlist_create_url>https://g.test/so/playlist.m3u8</playlist_create_url>
                                    </url>
                                    <url areafree="0" max_delay="60" timefree="1">
                                        <playlist_create_url>https://h.test/tf/playlist.m3u8</playlist_create_url>
                                    </url>
                                </urls>
                                """));
        when(mockRdkHttpClient.options(any(RdkHttpRequest.class)))
                .thenReturn(new TestRdkHttpResponse(200, null, null));
        when(mockFFmpegFactory.create()).thenReturn(mockFFmpeg);
        when(mockFFmpeg.addInput(any(Input.class))).thenReturn(mockFFmpeg);
        when(mockFFmpeg.addOutput(any(Output.class))).thenReturn(mockFFmpeg);
        when(mockFFmpeg.setOverwriteOutput(anyBoolean())).thenReturn(mockFFmpeg);
        when(mockFFmpeg.execute())
                .thenThrow(new JaffreeAbnormalExitException("test exception!", null));

        String stationId = "STATION";
        LocalDateTime from = LocalDateTime.parse("2025-12-22T10:00:00");
        LocalDateTime to = LocalDateTime.parse("2025-12-22T10:05:00");
        Path path = tempDir.resolve("テスト.m4a");

        // テスト対象の呼び出し

        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkDownloadException.class)
                .isThrownBy(
                        () -> {
                            rdkdlerDownloader.download(stationId, from, to, path);
                        })
                .withMessage(
                        "com.github.kokorin.jaffree.process.JaffreeAbnormalExitException: test"
                                + " exception!");

        // 呼び出し回数のチェック
        verify(mockFFmpeg, times(1)).execute();
    }

    @DisplayName("getImageFormatのテスト：正常系")
    @ParameterizedTest(name = "No.{index} : coverUrl=[{0}], ret=[{1}]")
    @CsvSource(
            value = {
                "xxx.jpg, mjpeg",
                "xxx.JPG, mjpeg",
                "xxx.jpeg, mjpeg",
                "xxx.JPEG, mjpeg",
                "xxx.png, png",
                "xxx.PNG, png",
                "xxx.xxx, null",
            },
            nullValues = "null")
    void test_getImageFormat(String coverUrl, String ret) {

        assertThat(rdkdlerDownloader.getImageFormat(coverUrl)).isEqualTo(ret);
    }
}
