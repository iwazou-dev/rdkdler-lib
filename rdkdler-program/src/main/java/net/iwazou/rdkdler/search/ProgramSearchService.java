package net.iwazou.rdkdler.search;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import net.iwazou.rdkdler.model.ProgramSearchResult;
import net.iwazou.rdkdler.util.CommonUtils;

/**
 * ラジコの番組キーワード検索 API を呼び出すサービスクラスです。
 *
 * <p>指定したキーワード・期間・地域などの条件で番組を検索し、JSON レスポンスを
 * {@link ProgramSearchResult} にデシリアライズして返します。
 *
 * <h2>内部状態（スレッドセーフに関する注意）</h2>
 * <p>本クラスは検索条件（{@link #setFilter(ProgramTimeRangeFilter)}、
 * {@link #setAllRegions(boolean)}、{@link #setRowLimit(int)}）をフィールドとして保持します。
 * そのため、同一インスタンスを複数スレッドから同時に利用する場合は、呼び出し側で同期するか、
 * 「1 リクエスト 1 インスタンス」等の運用にしてください。
 *
 * <h2>API 仕様について</h2>
 * <p>ラジコは検索 API の公式仕様を公開していないため、パラメータ名・制約等は
 * Web クライアントの通信等を参考にしています。将来の仕様変更により動作しなくなる可能性があります。
 */
@RequiredArgsConstructor
@Slf4j
public class ProgramSearchService {

    /**
     * HTTP 通信を行うクライアントです。
     */
    private final RdkHttpClient rdkHttpClient;

    /**
     * 「現在地域」として扱う都道府県コードです。
     *
     * <p>検索リクエストの {@code area_id} / {@code cur_area_id} に使用します。
     */
    private final AreaPrefecture currentAreaPrefectureCode;

    /**
     * 番組の時間帯（過去／未来）を絞り込むためのフィルタです。
     *
     * <p>デフォルトは {@link ProgramTimeRangeFilter#ALL}（過去・未来ともに検索対象）です。
     * <p>実際に送信する値は {@link ProgramTimeRangeFilter#getValue()} に従います。
     *
     * @param filter 番組の時間帯（過去／未来）を絞り込むためのフィルタ
     */
    @Setter @NonNull private ProgramTimeRangeFilter filter = ProgramTimeRangeFilter.ALL;

    /**
     * 検索対象地域を制御するフラグです。
     *
     * <ul>
     *   <li>{@code false}（デフォルト）：現在地域のみを検索対象とします（{@code region_id} は空文字列）</li>
     *   <li>{@code true}：全国（全地域）を検索対象とします（{@code region_id=all}）</li>
     * </ul>
     *
     * @param isAllRegions 検索対象地域を制御するフラグ
     */
    @Setter private boolean isAllRegions = false;

    /**
     * 1 ページあたりの取得件数です（デフォルト 12 件）。
     *
     * <p>設定は {@link #setRowLimit(int)} を使用してください。
     *
     * @return 1 ページあたりの取得件数
     */
    @Getter private int rowLimit = 12; // デフォルト値（好みで）

    /**
     * 1 回の検索で取得できる最小件数です。
     */
    public static final int MIN_ROW_LIMIT = 1;

    /**
     * 1 回の検索で取得できる最大件数です。
     *
     * <p>観測上の API 制約に合わせて 50 件としています。
     */
    public static final int MAX_ROW_LIMIT = 50;

    /**
     * 検索 API の {@code start_day} パラメータに使用する日付フォーマット（{@code yyyy-MM-dd}）です。
     */
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 1 ページあたりの取得件数を設定します。
     *
     * @param rowLimit 1 ページあたりの取得件数（{@link #MIN_ROW_LIMIT} ～ {@link #MAX_ROW_LIMIT} の範囲）
     * @throws IllegalArgumentException {@code rowLimit} が許可された範囲外の場合
     */
    public void setRowLimit(int rowLimit) {
        if (rowLimit < MIN_ROW_LIMIT || rowLimit > MAX_ROW_LIMIT) {
            throw new IllegalArgumentException(
                    "rowLimit must be between "
                            + MIN_ROW_LIMIT
                            + " and "
                            + MAX_ROW_LIMIT
                            + " (value="
                            + rowLimit
                            + ")");
        }
        this.rowLimit = rowLimit;
    }

    /**
     * キーワードで番組を検索します（ページインデックスは {@code 0} 固定）。
     *
     * <p>このメソッドは {@link #search(String, int)} を {@code pageIndex=0} で呼び出すショートカットです。
     *
     * @param keyword 検索キーワード（空文字列や空白のみは不可）
     * @return 検索結果を表す {@link ProgramSearchResult}
     * @throws IllegalArgumentException {@code keyword} が空または空白のみの場合
     * @throws IOException 通信エラー、レスポンス読み取りエラー、JSON デシリアライズエラーが発生した場合
     * @throws InterruptedException 呼び出しスレッドが割り込まれた場合
     */
    public ProgramSearchResult search(String keyword) throws IOException, InterruptedException {
        return search(keyword, 0);
    }

    /**
     * キーワードで番組を検索します。
     *
     * <p>本実装が送信する主なクエリパラメータは以下の通りです。
     * <ul>
     *   <li>{@code key}：検索キーワード</li>
     *   <li>{@code filter}：時間帯フィルタ（{@link #setFilter(ProgramTimeRangeFilter)} の設定値）</li>
     *   <li>{@code start_day}：実行日から 30 日前の日付（{@code yyyy-MM-dd}）</li>
     *   <li>{@code end_day}：空文字列（未指定扱い）</li>
     *   <li>{@code area_id / cur_area_id}：{@link #currentAreaPrefectureCode} の {@code areaId}</li>
     *   <li>{@code region_id}：全国検索の場合 {@code all}、現在地域のみの場合は空文字列</li>
     *   <li>{@code page_idx}：ページインデックス（0 始まり）</li>
     *   <li>{@code row_limit}：1 ページあたりの取得件数（{@link #setRowLimit(int)} の設定値）</li>
     *   <li>{@code app_id=pc}, {@code action_id=0}</li>
     * </ul>
     *
     * <p><strong>注意:</strong> 一部の実装例では {@code uid}（Cookie の {@code rdk_uid} 相当）を付与しますが、
     * 本実装では付与していません。将来サーバー側が必須化した場合はパラメータ追加が必要になる可能性があります。
     *
     * @param keyword 検索キーワード（空文字列や空白のみは不可）
     * @param pageIndex ページインデックス（0 始まり）
     * @return 検索結果を表す {@link ProgramSearchResult}
     * @throws IllegalArgumentException {@code keyword} が空または空白のみの場合、または {@code pageIndex < 0} の場合
     * @throws IOException 通信エラー、レスポンス読み取りエラー、JSON デシリアライズエラーが発生した場合
     * @throws InterruptedException 呼び出しスレッドが割り込まれた場合
     */
    public ProgramSearchResult search(String keyword, int pageIndex)
            throws IOException, InterruptedException {
        CommonUtils.notBlank(keyword);
        CommonUtils.isTrue(pageIndex >= 0);

        String url = "https://api.annex-cf.radiko.jp/v1/programs/legacy/perl/program/search";
        log.debug("search(String, int) : アクセスURL={}", url);
        String startDay = LocalDate.now().minusDays(30).format(fmt); // 30日前の日付を設定
        Map<String, String> parameters =
                Map.ofEntries(
                        Map.entry("key", keyword),
                        // "past":タイムフリーのみ、"future":ライブのみ、""（空文字列）:ライブ&タイムフリー
                        Map.entry("filter", filter.getValue()),
                        // 実行日から30日引いた日付 10/30 -> 9/30, 11/1 -> 10/2
                        Map.entry("start_day", startDay),
                        Map.entry("end_day", ""),
                        // 検索対象地域
                        Map.entry("area_id", currentAreaPrefectureCode.getAreaId()),
                        // "all":全国、""（空文字列）:現在地域
                        Map.entry("region_id", isAllRegions ? "all" : ""),
                        // 現在地域
                        Map.entry("cur_area_id", currentAreaPrefectureCode.getAreaId()),
                        // ページインデックス（0から）
                        Map.entry("page_idx", String.valueOf(pageIndex)),
                        // 1 ページあたりの取得件数（MAX:"50"）
                        Map.entry("row_limit", String.valueOf(this.rowLimit)),
                        Map.entry("app_id", "pc"),
                        Map.entry("action_id", "0"));
        RdkHttpResponse response =
                rdkHttpClient.get(RdkHttpRequest.builder().url(url).parameters(parameters).build());
        String body = CommonUtils.getBody(response);
        log.debug("search(String, int) : レスポンスボディ={}", body);
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        // 空文字→nullを許容
                        .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        ProgramSearchResult data = objectMapper.readValue(body, ProgramSearchResult.class);
        log.debug("search(String, int) : 検索結果={}", data);
        return data;
    }
}
