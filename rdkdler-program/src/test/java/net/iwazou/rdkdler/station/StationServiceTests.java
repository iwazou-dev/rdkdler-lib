package net.iwazou.rdkdler.station;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.model.AreaStations;
import net.iwazou.rdkdler.model.Station;
import net.iwazou.rdkdler.model.Station.Logo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StationServiceTests {
    @Mock RdkHttpClient mockRdkHttpClient;
    private StationService stationService;

    @BeforeEach
    void setUp() {
        stationService = new StationService(mockRdkHttpClient);
    }

    @DisplayName("getStationsのテスト（モック）：正常系")
    @Test
    void test_mock_getStations_01() throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                Files.readString(
                                        Path.of(
                                                "src/test/resources/xml/station_list_sample.xml"))));

        var stations = stationService.getStations(AreaPrefecture.KANAGAWA);

        // 期待値
        AreaStations expected =
                createAreaStations(
                        "AREA_ID",
                        "AREA_NAME",
                        List.of(
                                createStation(
                                        "STATION_ID1",
                                        "NAME1",
                                        "ASCII_NAME1",
                                        "RUBY1",
                                        11,
                                        12,
                                        List.of(
                                                createLogo("111", "121", "ALIGN11", "LOGO_TEXT11"),
                                                createLogo("112", "122", "ALIGN12", "LOGO_TEXT12"),
                                                createLogo("113", "123", "ALIGN13", "LOGO_TEXT13")),
                                        "BANNER1",
                                        "HREF1",
                                        13,
                                        14),
                                createStation(
                                        "STATION_ID2",
                                        "NAME2",
                                        "ASCII_NAME2",
                                        "RUBY2",
                                        21,
                                        22,
                                        List.of(createLogo("211", "221", "ALIGN21", "LOGO_TEXT21")),
                                        "BANNER2",
                                        "HREF2",
                                        23,
                                        24),
                                createStation(
                                        "STATION_ID3",
                                        "NAME3",
                                        "ASCII_NAME3",
                                        "RUBY3",
                                        31,
                                        32,
                                        null,
                                        "BANNER3",
                                        "HREF3",
                                        33,
                                        34)));

        assertThat(stations)
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expected);
    }

    private AreaStations createAreaStations(
            String areaId, String areaName, List<Station> stations) {
        AreaStations a = new AreaStations();
        a.setAreaId(areaId);
        a.setAreaName(areaName);
        a.setStations(stations);
        return a;
    }

    private Station createStation(
            String stationId,
            String stationName,
            String asciiName,
            String ruby,
            Integer areafree,
            Integer timefree,
            List<Logo> logos,
            String banner,
            String href,
            Integer simulMaxDelay,
            Integer tfMaxDelay) {
        Station station = new Station();
        station.setStationId(stationId);
        station.setStationName(stationName);
        station.setAsciiName(asciiName);
        station.setRuby(ruby);
        station.setAreafree(areafree);
        station.setTimefree(timefree);
        station.setLogos(logos);
        station.setBanner(banner);
        station.setHref(href);
        station.setSimulMaxDelay(simulMaxDelay);
        station.setTfMaxDelay(tfMaxDelay);
        return station;
    }

    private Logo createLogo(String width, String height, String align, String value) {
        Logo logo = new Logo();
        logo.setWidth(width);
        logo.setHeight(height);
        logo.setAlign(align);
        logo.setValue(value);
        return logo;
    }
}
