package net.iwazou.rdkdler.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.model.DailyProgramSchedule;
import net.iwazou.rdkdler.model.ProgramEntry;
import net.iwazou.rdkdler.model.ProgramEntry.Genre;
import net.iwazou.rdkdler.model.ProgramEntry.Item;
import net.iwazou.rdkdler.model.ProgramEntry.Meta;
import net.iwazou.rdkdler.model.ProgramEntry.Personality;
import net.iwazou.rdkdler.model.ProgramEntry.Program;
import net.iwazou.rdkdler.model.ProgramSchedule;
import net.iwazou.rdkdler.model.StationProgramSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgramScheduleServiceMockTests {
    @Mock RdkHttpClient mockRdkHttpClient;
    private ProgramScheduleService programScheduleService;

    @BeforeEach
    void setUp() {
        programScheduleService = new ProgramScheduleService(mockRdkHttpClient);
    }

    @DisplayName("getProgramSchedule(String stationId)のテスト（モック）：正常系")
    @Test
    void test_mock_getProgramSchedule_02() throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                Files.readString(
                                        Path.of(
                                                "src/test/resources/xml/program_station_weekly_sample.xml"))));

        ProgramSchedule sch = programScheduleService.getProgramSchedule("OC2");

        // 期待値
        var dps1 =
                createDailyProgramSchedule(
                        d("2025-10-22"),
                        List.of(
                                createProgramEntry(
                                        "12369134",
                                        "MASTER_ID11",
                                        t("2025-10-22T05:00:00"),
                                        t("2025-10-22T06:30:00"),
                                        "0500",
                                        "0630",
                                        "5400",
                                        "タイトル１１",
                                        "URL11",
                                        "URL_LINK11",
                                        "FAILED_RECORD11",
                                        111,
                                        211,
                                        311,
                                        411,
                                        "デスク１１",
                                        "インフォ１１",
                                        "PFM11",
                                        "IMG11",
                                        List.of(
                                                createItem("アイテム１１１"),
                                                createItem("アイテム１１２"),
                                                createItem("アイテム１１３")),
                                        createGenre(
                                                List.of(
                                                        createPersonality("C111", "パーソナリティー１１１"),
                                                        createPersonality("C112", "パーソナリティー１１２"),
                                                        createPersonality("C113", "パーソナリティー１１３")),
                                                List.of(
                                                        createProgram("P111", "プログラム１１１"),
                                                        createProgram("P112", "プログラム１１２"),
                                                        createProgram("P113", "プログラム１１３"))),
                                        List.of(
                                                createMeta("メタ名前１１１", "メタ値１１１"),
                                                createMeta("メタ名前１１２", "メタ値１１２"),
                                                createMeta("メタ名前１１３", "メタ値１１３"))),
                                createProgramEntry(
                                        "12369135",
                                        "MASTER_ID12",
                                        t("2025-10-22T06:30:00"),
                                        t("2025-10-22T08:30:00"),
                                        "0630",
                                        "0830",
                                        "7200",
                                        "タイトル１２",
                                        "URL12",
                                        "URL_LINK12",
                                        "FAILED_RECORD12",
                                        112,
                                        212,
                                        312,
                                        412,
                                        "デスク１２",
                                        "インフォ１２",
                                        "PFM12",
                                        "IMG12",
                                        List.of(createItem("アイテム１２１")),
                                        createGenre(
                                                List.of(createPersonality("C121", "パーソナリティー１２１")),
                                                List.of(createProgram("P121", "プログラム１２１"))),
                                        List.of(createMeta("メタ名前１２１", "メタ値１２１"))),
                                createProgramEntry(
                                        "",
                                        "",
                                        null,
                                        null,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        null,
                                        null,
                                        null,
                                        null,
                                        "",
                                        "",
                                        "",
                                        "",
                                        List.of(),
                                        createGenre(null, null),
                                        List.of())));
        var dps2 =
                createDailyProgramSchedule(
                        d("2025-10-23"),
                        List.of(
                                createProgramEntry(
                                        "12374176",
                                        "MASTER_ID21",
                                        t("2025-10-24T00:00:00"),
                                        t("2025-10-24T01:00:00"),
                                        "2400",
                                        "2500",
                                        "3600",
                                        "タイトル２１"),
                                createProgramEntry(
                                        "12374177",
                                        "MASTER_ID22",
                                        t("2025-10-24T01:00:00"),
                                        t("2025-10-24T03:00:00"),
                                        "2500",
                                        "2700",
                                        "7200",
                                        "タイトル２２")));
        var dps3 =
                createDailyProgramSchedule(
                        d("2025-10-24"),
                        List.of(
                                createProgramEntry(
                                        "12374192",
                                        "MASTER_ID31",
                                        t("2025-10-24T23:55:00"),
                                        t("2025-10-25T00:00:00"),
                                        "2355",
                                        "2400",
                                        "300",
                                        "タイトル３１")));
        var expected =
                createProgramSchedule(
                        "1800",
                        "1761721844",
                        List.of(
                                createStationProgramSchedule(
                                        "STATION01", "ステーション１", List.of(dps1, dps2, dps3))));

        assertThat(sch)
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expected);
    }

    @DisplayName("getProgramSchedule(AreaPrefecture, LocalDate)のテスト（モック）：正常系")
    @Test
    void test_mock_getProgramSchedule_01() throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                Files.readString(
                                        Path.of(
                                                "src/test/resources/xml/program_date_area_sample.xml"))));

        ProgramSchedule sch =
                programScheduleService.getProgramSchedule(AreaPrefecture.KANAGAWA, LocalDate.now());

        // 期待値
        var dps11 =
                createDailyProgramSchedule(
                        d("2025-10-22"),
                        List.of(
                                createProgramEntry(
                                        "12369134",
                                        "MASTER_ID11",
                                        t("2025-10-22T05:00:00"),
                                        t("2025-10-22T06:30:00"),
                                        "0500",
                                        "0630",
                                        "5400",
                                        "タイトル１１",
                                        "URL11",
                                        "URL_LINK11",
                                        "FAILED_RECORD11",
                                        111,
                                        211,
                                        311,
                                        411,
                                        "デスク１１",
                                        "インフォ１１",
                                        "PFM11",
                                        "IMG11",
                                        List.of(
                                                createItem("アイテム１１１"),
                                                createItem("アイテム１１２"),
                                                createItem("アイテム１１３")),
                                        createGenre(
                                                List.of(
                                                        createPersonality("C111", "パーソナリティー１１１"),
                                                        createPersonality("C112", "パーソナリティー１１２"),
                                                        createPersonality("C113", "パーソナリティー１１３")),
                                                List.of(
                                                        createProgram("P111", "プログラム１１１"),
                                                        createProgram("P112", "プログラム１１２"),
                                                        createProgram("P113", "プログラム１１３"))),
                                        List.of(
                                                createMeta("メタ名前１１１", "メタ値１１１"),
                                                createMeta("メタ名前１１２", "メタ値１１２"),
                                                createMeta("メタ名前１１３", "メタ値１１３"))),
                                createProgramEntry(
                                        "12369135",
                                        "MASTER_ID12",
                                        t("2025-10-22T06:30:00"),
                                        t("2025-10-22T08:30:00"),
                                        "0630",
                                        "0830",
                                        "7200",
                                        "タイトル１２",
                                        "URL12",
                                        "URL_LINK12",
                                        "FAILED_RECORD12",
                                        112,
                                        212,
                                        312,
                                        412,
                                        "デスク１２",
                                        "インフォ１２",
                                        "PFM12",
                                        "IMG12",
                                        List.of(createItem("アイテム１２１")),
                                        createGenre(
                                                List.of(createPersonality("C121", "パーソナリティー１２１")),
                                                List.of(createProgram("P121", "プログラム１２１"))),
                                        List.of(createMeta("メタ名前１２１", "メタ値１２１"))),
                                createProgramEntry(
                                        "",
                                        "",
                                        null,
                                        null,
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        null,
                                        null,
                                        null,
                                        null,
                                        "",
                                        "",
                                        "",
                                        "",
                                        List.of(),
                                        createGenre(null, null),
                                        List.of())));
        var dps21 =
                createDailyProgramSchedule(
                        d("2025-10-22"),
                        List.of(
                                createProgramEntry(
                                        "12369134",
                                        "MASTER_ID21",
                                        t("2025-10-22T05:00:00"),
                                        t("2025-10-22T06:30:00"),
                                        "0500",
                                        "0630",
                                        "5400",
                                        "タイトル２１",
                                        "URL21",
                                        "URL_LINK21",
                                        "FAILED_RECORD21",
                                        121,
                                        221,
                                        321,
                                        421,
                                        "デスク２１",
                                        "インフォ２１",
                                        "PFM21",
                                        "IMG21",
                                        List.of(createItem("アイテム２１１")),
                                        createGenre(
                                                List.of(createPersonality("C211", "パーソナリティー２１１")),
                                                List.of(createProgram("P211", "プログラム２１１"))),
                                        List.of(createMeta("メタ名前２１１", "メタ値２１１"))),
                                createProgramEntry(
                                        "12369135",
                                        "MASTER_ID22",
                                        t("2025-10-22T06:30:00"),
                                        t("2025-10-22T08:30:00"),
                                        "0630",
                                        "0830",
                                        "7200",
                                        "タイトル２２",
                                        "URL22",
                                        "URL_LINK22",
                                        "FAILED_RECORD22",
                                        122,
                                        222,
                                        322,
                                        422,
                                        "デスク２２",
                                        "インフォ２２",
                                        "PFM22",
                                        "IMG22",
                                        List.of(createItem("アイテム２２１")),
                                        createGenre(
                                                List.of(createPersonality("C221", "パーソナリティー２２１")),
                                                List.of(createProgram("P221", "プログラム２２１"))),
                                        List.of(createMeta("メタ名前２２１", "メタ値２２１")))));
        var dps31 =
                createDailyProgramSchedule(
                        d("2025-10-22"),
                        List.of(
                                createProgramEntry(
                                        "12369134",
                                        "MASTER_ID31",
                                        t("2025-10-22T05:00:00"),
                                        t("2025-10-22T06:30:00"),
                                        "0500",
                                        "0630",
                                        "5400",
                                        "タイトル３１",
                                        "URL31",
                                        "URL_LINK31",
                                        "FAILED_RECORD31",
                                        131,
                                        231,
                                        331,
                                        431,
                                        "デスク３１",
                                        "インフォ３１",
                                        "PFM31",
                                        "IMG31",
                                        List.of(createItem("アイテム３１１")),
                                        createGenre(
                                                List.of(createPersonality("C311", "パーソナリティー３１１")),
                                                List.of(createProgram("P311", "プログラム３１１"))),
                                        List.of(createMeta("メタ名前３１１", "メタ値３１１")))));

        var expected =
                createProgramSchedule(
                        "1800",
                        "1761721844",
                        List.of(
                                createStationProgramSchedule(
                                        "STATION01", "ステーション１", List.of(dps11)),
                                createStationProgramSchedule(
                                        "STATION02", "ステーション２", List.of(dps21)),
                                createStationProgramSchedule(
                                        "STATION03", "ステーション３", List.of(dps31))));

        assertThat(sch)
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expected);
    }

    private LocalDate d(String text) {
        return LocalDate.parse(text);
    }

    private LocalDateTime t(String text) {
        return LocalDateTime.parse(text);
    }

    private ProgramSchedule createProgramSchedule(
            String ttl, String srvtime, List<StationProgramSchedule> stationProgramSchedules) {
        ProgramSchedule p = new ProgramSchedule();
        p.setTtl(ttl);
        p.setSrvtime(srvtime);
        p.setStationProgramSchedules(stationProgramSchedules);
        return p;
    }

    private StationProgramSchedule createStationProgramSchedule(
            String stationId,
            String stationName,
            List<DailyProgramSchedule> dailyProgramSchedules) {
        StationProgramSchedule sch = new StationProgramSchedule();
        sch.setStationId(stationId);
        sch.setStationName(stationName);
        sch.setDailyProgramSchedules(dailyProgramSchedules);
        return sch;
    }

    private DailyProgramSchedule createDailyProgramSchedule(
            LocalDate date, List<ProgramEntry> programEntrys) {
        DailyProgramSchedule d = new DailyProgramSchedule();
        d.setDate(date);
        d.setProgramEntrys(programEntrys);
        return d;
    }

    private ProgramEntry createProgramEntry(
            String id,
            String masterId,
            LocalDateTime ft,
            LocalDateTime to,
            String ftl,
            String tol,
            String dur,
            String title) {
        return createProgramEntry(
                id, masterId, ft, to, ftl, tol, dur, title, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    private ProgramEntry createProgramEntry(
            String id,
            String masterId,
            LocalDateTime ft,
            LocalDateTime to,
            String ftl,
            String tol,
            String dur,
            String title,
            String url,
            String urlLink,
            String failedRecord,
            Integer tsInNg,
            Integer tsplusInNg,
            Integer tsOutNg,
            Integer tsplusOutNg,
            String desc,
            String info,
            String pfm,
            String img,
            List<Item> items,
            Genre genre,
            List<Meta> metas) {
        ProgramEntry p = new ProgramEntry();
        p.setId(id);
        p.setMasterId(masterId);
        p.setFt(ft);
        p.setTo(to);
        p.setFtl(ftl);
        p.setTol(tol);
        p.setDur(dur);
        p.setTitle(title);
        p.setUrl(url);
        p.setUrlLink(urlLink);
        p.setFailedRecord(failedRecord);
        p.setTsInNg(tsInNg);
        p.setTsplusInNg(tsplusInNg);
        p.setTsOutNg(tsOutNg);
        p.setTsplusOutNg(tsplusOutNg);
        p.setDesc(desc);
        p.setInfo(info);
        p.setPfm(pfm);
        p.setImg(img);
        p.setItems(items);
        p.setGenre(genre);
        p.setMetas(metas);
        return p;
    }

    private Item createItem(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }

    private Genre createGenre(List<Personality> personalitys, List<Program> programs) {
        Genre genre = new Genre();
        genre.setPersonalitys(personalitys);
        genre.setPrograms(programs);
        return genre;
    }

    private Personality createPersonality(String id, String name) {
        Personality personality = new Personality();
        personality.setId(id);
        personality.setName(name);
        return personality;
    }

    private Program createProgram(String id, String name) {
        Program program = new Program();
        program.setId(id);
        program.setName(name);
        return program;
    }

    private Meta createMeta(String name, String value) {
        Meta meta = new Meta();
        meta.setName(name);
        meta.setValue(value);
        return meta;
    }
}
