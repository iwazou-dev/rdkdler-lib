package net.iwazou.rdkdler;

import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.area.AreaPrefectureService;
import net.iwazou.rdkdler.download.DefaultFFmpegFactory;
import net.iwazou.rdkdler.download.RdkAuthenticator;
import net.iwazou.rdkdler.download.RdkDownloadService;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import net.iwazou.rdkdler.model.AreaStations;
import net.iwazou.rdkdler.model.DailyProgramSchedule;
import net.iwazou.rdkdler.model.ProgramEntry;
import net.iwazou.rdkdler.model.ProgramSchedule;
import net.iwazou.rdkdler.model.ProgramSearchResult;
import net.iwazou.rdkdler.model.ProgramSearchResult.ResultData;
import net.iwazou.rdkdler.model.StationProgramSchedule;
import net.iwazou.rdkdler.schedule.ProgramScheduleService;
import net.iwazou.rdkdler.search.ProgramSearchService;
import net.iwazou.rdkdler.search.ProgramTimeRangeFilter;
import net.iwazou.rdkdler.station.StationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ST_Tests {
    private RdkAuthenticator rdkAuthenticator;
    private RdkDownloadService rdkDownloadService;
    private AreaPrefectureService areaPrefectureService;

    private RdkHttpClient client;

    private static Path m4aDir = Paths.get("build", "m4a");

    @BeforeAll
    static void initAll() throws IOException {
        Files.createDirectories(m4aDir);
    }

    @BeforeEach
    void setUp() {
        client =
                new JdkRdkHttpClient(
                        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build());
        areaPrefectureService = new AreaPrefectureService(client);
        rdkAuthenticator = new RdkAuthenticator(client);
        rdkDownloadService = new RdkDownloadService(rdkAuthenticator, new DefaultFFmpegFactory());
    }

    @Test
    void test01() {
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            AreaPrefecture area = areaPrefectureService.getCurrentAreaPrefecture();
                            ProgramSearchService programSearchService =
                                    new ProgramSearchService(client, area);
                            programSearchService.setFilter(ProgramTimeRangeFilter.PAST);
                            ProgramSearchResult result = programSearchService.search("ニュース");
                            ResultData data = result.getResultDatas().get(0);
                            String stationId = data.getStationId();
                            String title = data.getTitle();
                            LocalDateTime startTime = data.getStartTime();
                            LocalDateTime endTime = data.getEndTime();
                            LocalDate programDate = data.getProgramDate();
                            String img = data.getImg();
                            Path out =
                                    m4aDir.resolve(
                                            String.format("ST1-%s %s.m4a", programDate, title));
                            rdkDownloadService.download(stationId, startTime, endTime, out, img);
                        });
    }

    @Test
    void test02() {
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            AreaPrefecture area = areaPrefectureService.getCurrentAreaPrefecture();
                            ProgramScheduleService programScheduleService =
                                    new ProgramScheduleService(client);
                            ProgramSchedule schedule =
                                    programScheduleService.getProgramSchedule(
                                            area, LocalDate.now().minusDays(2));

                            StationProgramSchedule sps =
                                    schedule.getStationProgramSchedules().get(0);
                            String stationId = sps.getStationId();
                            DailyProgramSchedule daily = sps.getDailyProgramSchedules().get(0);
                            LocalDate programDate = daily.getDate();
                            ProgramEntry entry = daily.getProgramEntrys().get(0);
                            String title = entry.getTitle();
                            LocalDateTime startTime = entry.getFt();
                            LocalDateTime endTime = entry.getTo();
                            String img = entry.getImg();

                            Path out =
                                    m4aDir.resolve(
                                            String.format("ST2-%s %s.m4a", programDate, title));
                            rdkDownloadService.download(stationId, startTime, endTime, out, img);
                        });
    }

    @Test
    void test03() {
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            AreaPrefecture area = areaPrefectureService.getCurrentAreaPrefecture();
                            ProgramScheduleService programScheduleService =
                                    new ProgramScheduleService(client);
                            StationService stationService = new StationService(client);
                            AreaStations areaRadioStations = stationService.getStations(area);
                            String stationId =
                                    areaRadioStations.getStations().get(0).getStationId();
                            ProgramSchedule schedule =
                                    programScheduleService.getProgramSchedule(stationId);
                            StationProgramSchedule sps =
                                    schedule.getStationProgramSchedules().get(0);
                            DailyProgramSchedule daily = sps.getDailyProgramSchedules().get(0);
                            LocalDate programDate = daily.getDate();
                            ProgramEntry entry = daily.getProgramEntrys().get(0);
                            String title = entry.getTitle();
                            LocalDateTime startTime = entry.getFt();
                            LocalDateTime endTime = entry.getTo();
                            String img = entry.getImg();

                            Path out =
                                    m4aDir.resolve(
                                            String.format("ST3-%s %s.m4a", programDate, title));
                            rdkDownloadService.download(stationId, startTime, endTime, out, img);
                        });
    }
}
