package net.iwazou.rdkdler.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import net.iwazou.rdkdler.model.ProgramSchedule;
import net.iwazou.rdkdler.util.CommonUtils;

/**
 * ラジコの番組スケジュール情報（番組表 XML）を取得するサービスクラスです。
 *
 * <p>HTTP クライアントを用いてラジコが提供する番組表 XML を取得し、
 * Jackson（{@link XmlMapper}）で {@link ProgramSchedule} にマッピングして返します。
 *
 * <p>本クラスは状態を保持しない（依存する {@link RdkHttpClient} を除く）想定のため、
 * スレッドセーフに利用できる設計が望ましいです（呼び出しごとに {@link ObjectMapper} を生成しています）。
 */
@RequiredArgsConstructor
@Slf4j
public class ProgramScheduleService {

    /**
     * HTTP 通信を行うクライアントです。
     */
    private final RdkHttpClient rdkHttpClient;

    /**
     * ラジコの API で使用する日付フォーマット（{@code yyyyMMdd}）です。
     *
     * <p>例: 2026年1月1日 → {@code "20260101"}
     */
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * ラジコのサーバーにアクセスして、指定したラジオ局の週間番組表（前後1週間程度）を取得します。
     *
     * <p>アクセス URL:
     * {@code https://radiko.jp/v3/program/station/weekly/[stationId].xml}
     *
     * <p>取得できる期間（前後何日分か）はラジコ側の仕様に依存し、将来変更される可能性があります。
     *
     * @param stationId ラジオ局の局 ID（例: {@code "TBS"}）
     * @return 指定ラジオ局の番組スケジュール情報（XML を {@link ProgramSchedule} にマッピングしたもの）
     * @throws IOException
     *         通信・レスポンス読み取り・XML 解析（マッピング）で入出力エラーが発生した場合
     * @throws InterruptedException
     *         HTTP 通信処理中などにスレッドが割り込まれた場合
     */
    public ProgramSchedule getProgramSchedule(String stationId)
            throws IOException, InterruptedException {
        String url = String.format("https://radiko.jp/v3/program/station/weekly/%s.xml", stationId);
        log.debug("getProgramSchedule(String) : アクセスURL={}", url);
        RdkHttpResponse response = rdkHttpClient.get(RdkHttpRequest.builder().url(url).build());
        String body = CommonUtils.getBody(response);
        log.debug("getProgramSchedule(String) : レスポンスボディ={}", body);
        ObjectMapper objectMapper =
                new XmlMapper()
                        .registerModule(new JavaTimeModule())
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        ProgramSchedule programSchedule = objectMapper.readValue(body, ProgramSchedule.class);
        log.debug("getProgramSchedule(String) : ラジオ局のスケジュール情報={}", programSchedule);
        return programSchedule;
    }

    /**
     * ラジコのサーバーにアクセスして、指定都道府県エリア内の放送局の指定日付の番組表を取得します。
     *
     * <p>アクセス URL:
     * {@code https://api.radiko.jp/program/v3/date/[yyyyMMdd]/area/[areaId].xml}
     *
     * <p>{@code [yyyyMMdd]} は {@link LocalDate} を {@link #fmt}（{@code yyyyMMdd}）で整形した値です。
     * {@code [areaId]} は {@link AreaPrefecture#getAreaId()}（例: {@code "JP13"}）です。
     *
     * @param area ラジコのエリア（都道府県）コード（null 不可）
     * @param date 番組情報を取得する日付（null 不可）
     * @return 指定エリア・指定日付の番組スケジュール情報（XML を {@link ProgramSchedule} にマッピングしたもの）
     * @throws IOException
     *         通信・レスポンス読み取り・XML 解析（マッピング）で入出力エラーが発生した場合
     * @throws InterruptedException
     *         HTTP 通信処理中などにスレッドが割り込まれた場合
     * @throws NullPointerException
     *         {@code area} または {@code date} が {@code null} の場合（本メソッド内で明示チェックはしていません）
     */
    public ProgramSchedule getProgramSchedule(AreaPrefecture area, LocalDate date)
            throws IOException, InterruptedException {
        String url =
                String.format(
                        "https://api.radiko.jp/program/v3/date/%s/area/%s.xml",
                        date.format(fmt), area.getAreaId());
        log.debug("getProgramSchedule(AreaPrefecture, LocalDate) : アクセスURL={}", url);
        RdkHttpResponse response = rdkHttpClient.get(RdkHttpRequest.builder().url(url).build());
        String body = CommonUtils.getBody(response);
        log.debug("getProgramSchedule(AreaPrefecture, LocalDate) : レスポンスボディ={}", body);
        ObjectMapper objectMapper =
                new XmlMapper()
                        .registerModule(new JavaTimeModule())
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        ProgramSchedule programSchedule = objectMapper.readValue(body, ProgramSchedule.class);
        log.debug(
                "getProgramSchedule(AreaPrefecture, LocalDate) : ラジオ局のスケジュール情報={}",
                programSchedule);
        return programSchedule;
    }
}
