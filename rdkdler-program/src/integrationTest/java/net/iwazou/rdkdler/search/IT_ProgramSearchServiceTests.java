package net.iwazou.rdkdler.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IT_ProgramSearchServiceTests {
    private ProgramSearchService programSearchService;

    @BeforeEach
    void setUp() {
        programSearchService =
                new ProgramSearchService(
                        new JdkRdkHttpClient(
                                HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(20))
                                        .build()),
                        AreaPrefecture.KANAGAWA);
    }

    @DisplayName("search(String)のテスト：正常系")
    @Test
    void test_search_01() throws IOException, InterruptedException {

        var result = programSearchService.search("ニュース");

        assertThat(result.getMeta().getRowLimit()).isEqualTo(12);
        assertThat(result.getMeta().getPageIdx()).isZero();
        assertThat(result.getResultDatas()).hasSizeGreaterThan(0);
    }

    @DisplayName("search(String, int)のテスト：正常系")
    @Test
    void test_search_02() throws IOException, InterruptedException {

        programSearchService.setRowLimit(1);
        var result = programSearchService.search("ニュース", 1);

        assertThat(result.getMeta().getRowLimit()).isEqualTo(1);
        assertThat(result.getMeta().getPageIdx()).isEqualTo(1);
        assertThat(result.getResultDatas()).hasSizeGreaterThan(0);
    }
}
