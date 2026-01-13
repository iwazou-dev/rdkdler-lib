package net.iwazou.rdkdler.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;
import java.util.stream.Stream;
import net.iwazou.rdkdler.TestPropKeys;
import net.iwazou.rdkdler.TestUtils;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class IT_RdkDownloadServiceTests {
    private RdkAuthenticator rdkAuthenticator;
    private RdkDownloadService rdkDownloadService;

    private static Path m4aDir = Paths.get("build", "m4a");

    @TempDir Path tempDir; // テストごとに空の一時ディレクトリが割り当てられ、終了時に削除される

    @BeforeAll
    static void initAll() throws IOException {
        Files.createDirectories(m4aDir);
    }

    @BeforeEach
    void setUp() throws IOException {
        Properties props = TestUtils.loadProperties();
        String ffmpegPath = props.getProperty(TestPropKeys.FFMPEG_PATH);
        rdkAuthenticator =
                new RdkAuthenticator(
                        new JdkRdkHttpClient(
                                HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(20))
                                        .build()));
        rdkDownloadService =
                new RdkDownloadService(
                        rdkAuthenticator, new DefaultFFmpegFactory(Paths.get(ffmpegPath)));
    }

    @DisplayName("downloadのテスト：正常系（今いるエリアの放送局、画像埋め込みなし）")
    @Test
    void test_download_01() throws IOException, InterruptedException {
        Properties props = TestUtils.loadProperties();
        String stationId = props.getProperty(TestPropKeys.RADIKO_STATION_CURRENT_ID);

        LocalDate dt = LocalDate.now().minusDays(1);
        LocalTime lt = LocalTime.parse("10:00:00");
        Path path = tempDir.resolve("テスト.m4a");
        assertThat(Files.exists(path)).isFalse();
        rdkDownloadService.download(
                stationId, LocalDateTime.of(dt, lt), LocalDateTime.of(dt, lt.plusMinutes(5)), path);

        assertThat(Files.exists(path)).isTrue();
    }

    @DisplayName("downloadのテスト：正常系（今いるエリアの放送局、画像埋め込みあり）")
    @ParameterizedTest(name = "No.{index} : 拡張子=[{0}], 画像=[{1}]")
    @MethodSource
    void test_download_02(String extension, String img) throws IOException, InterruptedException {
        Properties props = TestUtils.loadProperties();
        String stationId = props.getProperty(TestPropKeys.RADIKO_STATION_CURRENT_ID);

        Path out = m4aDir.resolve("テスト音声データ画像埋め込みあり_" + extension + ".m4a");
        // テスト音声データは一旦削除する。
        Files.deleteIfExists(out);

        LocalDate dt = LocalDate.now().minusDays(1);
        LocalTime lt = LocalTime.parse("10:00:00");
        Path path = Paths.get(img);
        String url = path.toUri().toString();
        rdkDownloadService.download(
                stationId,
                LocalDateTime.of(dt, lt),
                LocalDateTime.of(dt, lt.plusMinutes(5)),
                out,
                url);

        assertThat(Files.exists(path)).isTrue();
    }

    static Stream<Arguments> test_download_02() {
        return Stream.of(
                Arguments.of("jpeg", "src/integrationTest/resources/img/test_img.jpeg"),
                Arguments.of("jpg", "src/integrationTest/resources/img/test_img.jpg"),
                Arguments.of("png", "src/integrationTest/resources/img/test_img.png"));
    }

    @DisplayName("downloadのテスト：正常系（エリアフリー有効アカウントでログインあり、別エリアの放送局、画像埋め込みなし）")
    @Test
    void test_download_03() throws IOException, InterruptedException {
        Properties props = TestUtils.loadProperties();
        String mail = props.getProperty(TestPropKeys.RADIKO_LOGIN_MAIL);
        String password = props.getProperty(TestPropKeys.RADIKO_LOGIN_PASSWORD);
        assumeTrue(mail != null || password != null, "アカウントが設定されていないのでテストをスキップする");
        String areaFreeStationId = props.getProperty(TestPropKeys.RADIKO_STATION_AREAFREE_ID);
        assumeTrue(areaFreeStationId != null, "エリアフリーの放送局が設定されていないのでテストをスキップする");

        rdkAuthenticator.login(mail, password);

        LocalDate dt = LocalDate.now().minusDays(1);
        LocalTime lt = LocalTime.parse("10:00:00");
        Path path = tempDir.resolve("テスト.m4a");
        assertThat(Files.exists(path)).isFalse();
        rdkDownloadService.download(
                areaFreeStationId,
                LocalDateTime.of(dt, lt),
                LocalDateTime.of(dt, lt.plusMinutes(5)),
                path);

        assertThat(Files.exists(path)).isTrue();
    }

    @DisplayName("downloadのテスト：異常系（エリアフリー有効アカウントでログインなし、別エリアの放送局、画像埋め込みなし）")
    @Test
    void test_download_04() throws IOException {
        Properties props = TestUtils.loadProperties();
        String areaFreeStationId = props.getProperty(TestPropKeys.RADIKO_STATION_AREAFREE_ID);
        assumeTrue(areaFreeStationId != null, "エリアフリーの放送局が設定されていないのでテストをスキップする");

        LocalDate dt = LocalDate.now().minusDays(1);
        LocalTime lt = LocalTime.parse("10:00:00");
        Path path = tempDir.resolve("テスト.m4a");
        assertThat(Files.exists(path)).isFalse();

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkDownloadException.class)
                .isThrownBy(
                        () -> {
                            rdkDownloadService.download(
                                    areaFreeStationId,
                                    LocalDateTime.of(dt, lt),
                                    LocalDateTime.of(dt, lt.plusMinutes(5)),
                                    path);
                        })
                .withMessage(
                        "com.github.kokorin.jaffree.process.JaffreeAbnormalExitException: Process"
                                + " execution has ended with non-zero status: 8. Check logs for"
                                + " detailed error message.");
    }

    @DisplayName("downloadのテスト：異常系（出力ファイル名の拡張子が不正）")
    @Test
    void test_download_05() throws IOException {
        Properties props = TestUtils.loadProperties();
        String stationId = props.getProperty(TestPropKeys.RADIKO_STATION_CURRENT_ID);

        /*
         * 例外発生のテスト：
         */
        LocalDate dt = LocalDate.now().minusDays(1);
        LocalTime lt = LocalTime.parse("10:00:00");
        // 不正な拡張子
        Path path = tempDir.resolve("テスト.m4");
        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkDownloadException.class)
                .isThrownBy(
                        () -> {
                            rdkDownloadService.download(
                                    stationId,
                                    LocalDateTime.of(dt, lt),
                                    LocalDateTime.of(dt, lt.plusMinutes(5)),
                                    path);
                        })
                .withMessage(
                        "com.github.kokorin.jaffree.process.JaffreeAbnormalExitException: Process"
                                + " execution has ended with non-zero status: 234. Check logs for"
                                + " detailed error message.");
    }
}
