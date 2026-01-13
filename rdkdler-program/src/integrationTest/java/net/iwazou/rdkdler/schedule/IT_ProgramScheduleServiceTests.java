package net.iwazou.rdkdler.schedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import net.iwazou.rdkdler.model.ProgramSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IT_ProgramScheduleServiceTests {
    private ProgramScheduleService programScheduleService;

    @BeforeEach
    void setUp() {
        programScheduleService =
                new ProgramScheduleService(
                        new JdkRdkHttpClient(
                                HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(20))
                                        .build()));
    }

    @DisplayName("getProgramSchedule(String)のテスト：正常系")
    @Test
    void test_getProgramSchedule_01() throws IOException, InterruptedException {

        ProgramSchedule sch = programScheduleService.getProgramSchedule("OC2");
        assertThat(sch).isNotNull();
    }

    @DisplayName("getProgramSchedule(AreaPrefecture, LocalDate)のテスト：正常系")
    @Test
    void test_getProgramSchedule_02() throws IOException, InterruptedException {

        ProgramSchedule sch =
                programScheduleService.getProgramSchedule(AreaPrefecture.KANAGAWA, LocalDate.now());
        assertThat(sch).isNotNull();
    }
}
