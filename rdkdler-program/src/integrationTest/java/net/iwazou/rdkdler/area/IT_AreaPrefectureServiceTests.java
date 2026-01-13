package net.iwazou.rdkdler.area;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import net.iwazou.rdkdler.TestPropKeys;
import net.iwazou.rdkdler.TestUtils;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IT_AreaPrefectureServiceTests {
    private AreaPrefectureService areaPrefectureService;

    @BeforeEach
    void setUp() {
        areaPrefectureService =
                new AreaPrefectureService(
                        new JdkRdkHttpClient(
                                HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(20))
                                        .build()));
    }

    @DisplayName("getCurrentAreaPrefectureのテスト：正常系")
    @Test
    void test_getCurrentAreaPrefecture_01() throws IOException, InterruptedException {
        Properties props = TestUtils.loadProperties();
        String areaId = props.getProperty(TestPropKeys.RADIKO_AREA_CURRENT_ID);

        AreaPrefecture area = areaPrefectureService.getCurrentAreaPrefecture();
        assertThat(area).isEqualTo(AreaPrefecture.fromAreaId(areaId).orElseThrow());
    }
}
