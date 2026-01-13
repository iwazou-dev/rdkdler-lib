package net.iwazou.rdkdler.station;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IT_StationServiceTests {
    private StationService stationService;

    @BeforeEach
    void setUp() {
        stationService =
                new StationService(
                        new JdkRdkHttpClient(
                                HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(20))
                                        .build()));
    }

    @DisplayName("getStationsのテスト：正常系")
    @Test
    void test_getStations_01() throws IOException, InterruptedException {

        var stations = stationService.getStations(AreaPrefecture.KANAGAWA);

        assertThat(stations.getAreaId()).isEqualTo("JP14");
    }
}
