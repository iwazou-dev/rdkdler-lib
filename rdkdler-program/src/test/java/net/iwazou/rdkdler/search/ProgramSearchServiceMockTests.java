package net.iwazou.rdkdler.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.iwazou.rdkdler.TestRdkHttpResponse;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.model.ProgramSearchResult;
import net.iwazou.rdkdler.model.ProgramSearchResult.Category;
import net.iwazou.rdkdler.model.ProgramSearchResult.Genre;
import net.iwazou.rdkdler.model.ProgramSearchResult.Meta;
import net.iwazou.rdkdler.model.ProgramSearchResult.ResultData;
import net.iwazou.rdkdler.model.ProgramSearchResult.ResultMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgramSearchServiceMockTests {
    @Mock RdkHttpClient mockRdkHttpClient;
    private ProgramSearchService programSearchService;
    private Map<String, String> expectedParameters = null;

    @Captor ArgumentCaptor<RdkHttpRequest> rdkHttpRequestCaptor;

    @BeforeEach
    void setUp() {
        programSearchService = new ProgramSearchService(mockRdkHttpClient, AreaPrefecture.KANAGAWA);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        expectedParameters = new HashMap<>();
        expectedParameters.put("key", "");
        expectedParameters.put("filter", "");
        expectedParameters.put("start_day", LocalDate.now().minusDays(30).format(formatter));
        expectedParameters.put("end_day", "");
        expectedParameters.put("area_id", "JP14");
        expectedParameters.put("region_id", "");
        expectedParameters.put("cur_area_id", "JP14");
        expectedParameters.put("page_idx", "0");
        expectedParameters.put("row_limit", "12");
        expectedParameters.put("app_id", "pc");
        expectedParameters.put("action_id", "0");
    }

    @DisplayName("searchのテスト（モック）：正常系")
    @Test
    void test_mock_search_01() throws IOException, InterruptedException {

        // モックの設定
        when_RdkHttpClient_get_ok(mockRdkHttpClient);

        // テスト対象メソッドの実行
        var result = programSearchService.search("ニュース");

        // モック呼び出し回数のチェックとパラメーターのキャプチャー
        verify(mockRdkHttpClient, times(1)).get(rdkHttpRequestCaptor.capture());

        // 期待値の設定
        expectedParameters.put("key", "ニュース");

        assertThat(rdkHttpRequestCaptor.getValue().getParameters())
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expectedParameters);

        // 期待値
        ProgramSearchResult expected =
                createProgramSearchResult(
                        createResultMeta(
                                List.of("KEY1", "KEY2", "KEY3"),
                                List.of(),
                                List.of("JP14"),
                                "JP14",
                                "",
                                d("2025-10-02"),
                                null,
                                "",
                                100,
                                0,
                                50,
                                List.of(),
                                "",
                                List.of()),
                        List.of(
                                createResultData(
                                        t("2025-10-31T17:30:00"),
                                        t("2025-10-31T18:00:00"),
                                        "1730",
                                        "1800",
                                        d("2025-10-31"),
                                        "PROGRAM_URL1",
                                        "STATION_ID1",
                                        "PERFORMER1",
                                        "TITLE1",
                                        "INFO1",
                                        "DESCRIPTION1",
                                        "STATUS1",
                                        "IMG1",
                                        createGenre(
                                                createCategory("C001", "PERSONALITY1"),
                                                createCategory("P001", "PROGRAM1")),
                                        11,
                                        12,
                                        13,
                                        14,
                                        List.of(
                                                createMeta("NAME11", "VALUE11"),
                                                createMeta("NAME12", "VALUE12"),
                                                createMeta("NAME13", "VALUE13"))),
                                createResultData(
                                        t("2025-10-31T11:20:00"),
                                        t("2025-10-31T11:30:00"),
                                        "1120",
                                        "1130",
                                        d("2025-10-31"),
                                        "PROGRAM_URL2",
                                        "STATION_ID2",
                                        "PERFORMER2",
                                        "TITLE2",
                                        "INFO2",
                                        "DESCRIPTION2",
                                        "STATUS2",
                                        "IMG2",
                                        createGenre(
                                                createCategory("C002", "PERSONALITY2"),
                                                createCategory("P002", "PROGRAM2")),
                                        21,
                                        22,
                                        23,
                                        24,
                                        List.of(createMeta("NAME21", "VALUE21"))),
                                createResultData(
                                        t("2025-10-30T17:30:00"),
                                        t("2025-10-30T18:00:00"),
                                        "1730",
                                        "1800",
                                        d("2025-10-30"),
                                        "PROGRAM_URL3",
                                        "STATION_ID3",
                                        "PERFORMER3",
                                        "TITLE3",
                                        "INFO3",
                                        "DESCRIPTION3",
                                        "STATUS3",
                                        "IMG3",
                                        createGenre(
                                                createCategory("C003", "PERSONALITY3"),
                                                createCategory("P003", "PROGRAM3")),
                                        31,
                                        32,
                                        33,
                                        34,
                                        List.of())));

        assertThat(result)
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expected);
    }

    @DisplayName("setRowLimitのテスト（モック）：正常系")
    @ParameterizedTest(name = "No.{index} : row_limit=[{0}], 期待値の差分=[{1}]")
    @MethodSource
    void test_mock_setRowLimit_01(Integer rowLimit, Map<String, String> expDiff)
            throws IOException, InterruptedException {

        // モックの設定
        when_RdkHttpClient_get_ok(mockRdkHttpClient);

        // テスト対象メソッドの実行
        if (rowLimit != null) programSearchService.setRowLimit(rowLimit);
        programSearchService.search("検索");

        // モック呼び出し回数のチェックとパラメーターのキャプチャー
        verify(mockRdkHttpClient, times(1)).get(rdkHttpRequestCaptor.capture());

        // 期待値の設定
        expectedParameters.put("key", "検索");
        expectedParameters.putAll(expDiff);

        // チェック
        assertThat(rdkHttpRequestCaptor.getValue().getParameters())
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expectedParameters);
    }

    static Stream<Arguments> test_mock_setRowLimit_01() {
        return Stream.of(
                Arguments.of(null, Map.of("row_limit", "12")),
                Arguments.of(ProgramSearchService.MAX_ROW_LIMIT, Map.of("row_limit", "50")),
                Arguments.of(ProgramSearchService.MIN_ROW_LIMIT, Map.of("row_limit", "1")));
    }

    @DisplayName("setRowLimitのテスト（モック）：異常系")
    @Test
    void test_mock_setRowLimit_02() {

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(java.lang.IllegalArgumentException.class)
                .isThrownBy(
                        () ->
                                programSearchService.setRowLimit(
                                        ProgramSearchService.MIN_ROW_LIMIT - 1))
                .withMessage("rowLimit must be between 1 and 50 (value=0)");

        assertThatExceptionOfType(java.lang.IllegalArgumentException.class)
                .isThrownBy(
                        () ->
                                programSearchService.setRowLimit(
                                        ProgramSearchService.MAX_ROW_LIMIT + 1))
                .withMessage("rowLimit must be between 1 and 50 (value=51)");
    }

    @DisplayName("setAllRegionsのテスト（モック）：正常系")
    @ParameterizedTest(name = "No.{index} : allRegions=[{0}], 期待値の差分=[{1}]")
    @MethodSource
    void test_mock_setAllRegions_01(Boolean allRegions, Map<String, String> expDiff)
            throws IOException, InterruptedException {

        // モックの設定
        when_RdkHttpClient_get_ok(mockRdkHttpClient);

        // テスト対象メソッドの実行
        if (allRegions != null) programSearchService.setAllRegions(allRegions);
        programSearchService.search("検索");

        // モック呼び出し回数のチェックとパラメーターのキャプチャー
        verify(mockRdkHttpClient, times(1)).get(rdkHttpRequestCaptor.capture());

        // 期待値の設定
        expectedParameters.put("key", "検索");
        expectedParameters.putAll(expDiff);

        // チェック
        assertThat(rdkHttpRequestCaptor.getValue().getParameters())
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expectedParameters);
    }

    static Stream<Arguments> test_mock_setAllRegions_01() {
        return Stream.of(
                Arguments.of(null, Map.of("region_id", "")),
                Arguments.of(true, Map.of("region_id", "all")),
                Arguments.of(false, Map.of("region_id", "")));
    }

    @DisplayName("setFilterのテスト（モック）：正常系")
    @ParameterizedTest(name = "No.{index} : filter=[{0}], 期待値の差分=[{1}]")
    @MethodSource
    void test_mock_setFilter_01(ProgramTimeRangeFilter filter, Map<String, String> expDiff)
            throws IOException, InterruptedException {

        // モックの設定
        when_RdkHttpClient_get_ok(mockRdkHttpClient);

        // テスト対象メソッドの実行
        if (filter != null) programSearchService.setFilter(filter);
        programSearchService.search("検索");

        // モック呼び出し回数のチェックとパラメーターのキャプチャー
        verify(mockRdkHttpClient, times(1)).get(rdkHttpRequestCaptor.capture());

        // 期待値の設定
        expectedParameters.put("key", "検索");
        expectedParameters.putAll(expDiff);

        // チェック
        assertThat(rdkHttpRequestCaptor.getValue().getParameters())
                .usingRecursiveComparison() // オブジェクトの階層をすべてチェックする
                .isEqualTo(expectedParameters);
    }

    static Stream<Arguments> test_mock_setFilter_01() {
        return Stream.of(
                Arguments.of(null, Map.of("filter", "")),
                Arguments.of(ProgramTimeRangeFilter.FUTURE, Map.of("filter", "future")),
                Arguments.of(ProgramTimeRangeFilter.PAST, Map.of("filter", "past")),
                Arguments.of(ProgramTimeRangeFilter.ALL, Map.of("filter", "")));
    }

    @DisplayName("setFilterのテスト（モック）：異常系")
    @Test
    void test_mock_setFilter_02() {

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(java.lang.NullPointerException.class)
                .isThrownBy(() -> programSearchService.setFilter(null))
                .withMessage("filter is marked non-null but is null");
    }

    @DisplayName("search(String keyword)のテスト（モック）：異常系")
    @Test
    void test_mock_search_02() {

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(java.lang.NullPointerException.class)
                .isThrownBy(() -> programSearchService.search(null))
                .withMessage("Validation failed");

        assertThatExceptionOfType(java.lang.IllegalArgumentException.class)
                .isThrownBy(() -> programSearchService.search(""))
                .withMessage("Validation failed");

        assertThatExceptionOfType(java.lang.IllegalArgumentException.class)
                .isThrownBy(() -> programSearchService.search(" "))
                .withMessage("Validation failed");
    }

    @DisplayName("search(String keyword, int pageIndex)のテスト（モック）：異常系")
    @Test
    void test_mock_search_03() {

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(java.lang.IllegalArgumentException.class)
                .isThrownBy(() -> programSearchService.search("検索", -1))
                .withMessage("Validation failed");
    }

    private void when_RdkHttpClient_get_ok(RdkHttpClient mockRdkHttpClient)
            throws IOException, InterruptedException {
        when(mockRdkHttpClient.get(any(RdkHttpRequest.class)))
                .thenReturn(
                        new TestRdkHttpResponse(
                                200,
                                null,
                                Files.readString(
                                        Path.of(
                                                "src/test/resources/json/search_result_sample.json"))));
    }

    private LocalDate d(String text) {
        return LocalDate.parse(text);
    }

    private LocalDateTime t(String text) {
        return LocalDateTime.parse(text);
    }

    private ProgramSearchResult createProgramSearchResult(
            ResultMeta meta, List<ResultData> resultDatas) {
        ProgramSearchResult p = new ProgramSearchResult();
        p.setMeta(meta);
        p.setResultDatas(resultDatas);
        return p;
    }

    private ResultMeta createResultMeta(
            List<String> key,
            List<String> stationId,
            List<String> areaId,
            String curAreaId,
            String regionId,
            LocalDate startDay,
            LocalDate endDay,
            String filter,
            int resultCount,
            int pageIdx,
            int rowLimit,
            List<String> kakuchou,
            String suisengo,
            List<String> genreId) {
        ResultMeta r = new ResultMeta();
        r.setKey(key);
        r.setStationId(stationId);
        r.setAreaId(areaId);
        r.setCurAreaId(curAreaId);
        r.setRegionId(regionId);
        r.setStartDay(startDay);
        r.setEndDay(endDay);
        r.setFilter(filter);
        r.setResultCount(resultCount);
        r.setPageIdx(pageIdx);
        r.setRowLimit(rowLimit);
        r.setKakuchou(kakuchou);
        r.setSuisengo(suisengo);
        r.setGenreId(genreId);
        return r;
    }

    private ResultData createResultData(
            LocalDateTime startTime,
            LocalDateTime endTime,
            String startTimeS,
            String endTimeS,
            LocalDate programDate,
            String programUrl,
            String stationId,
            String performer,
            String title,
            String info,
            String description,
            String status,
            String img,
            Genre genre,
            Integer tsInNg,
            Integer tsOutNg,
            Integer tsplusInNg,
            Integer tsplusOutNg,
            List<Meta> metas) {
        ResultData r = new ResultData();
        r.setStartTime(startTime);
        r.setEndTime(endTime);
        r.setStartTimeS(startTimeS);
        r.setEndTimeS(endTimeS);
        r.setProgramDate(programDate);
        r.setProgramUrl(programUrl);
        r.setStationId(stationId);
        r.setPerformer(performer);
        r.setTitle(title);
        r.setInfo(info);
        r.setDescription(description);
        r.setStatus(status);
        r.setImg(img);
        r.setGenre(genre);
        r.setTsInNg(tsInNg);
        r.setTsOutNg(tsOutNg);
        r.setTsplusInNg(tsplusInNg);
        r.setTsplusOutNg(tsplusOutNg);
        r.setMetas(metas);
        return r;
    }

    private Genre createGenre(Category personality, Category program) {
        Genre genre = new Genre();
        genre.setPersonality(personality);
        genre.setProgram(program);
        return genre;
    }

    private Category createCategory(String id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    private Meta createMeta(String name, String value) {
        Meta meta = new Meta();
        meta.setName(name);
        meta.setValue(value);
        return meta;
    }
}
