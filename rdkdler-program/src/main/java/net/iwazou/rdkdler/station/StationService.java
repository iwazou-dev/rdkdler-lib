package net.iwazou.rdkdler.station;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.iwazou.rdkdler.area.AreaPrefecture;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import net.iwazou.rdkdler.model.AreaStations;
import net.iwazou.rdkdler.util.CommonUtils;

/**
 * ラジコのエリアごとのラジオ局一覧（ステーションリスト）を取得するサービスクラスです。
 *
 * <p>ステーションリスト API（XML）にアクセスし、取得した XML を
 * Jackson（{@link XmlMapper}）で {@link AreaStations} にデシリアライズして返します。
 *
 * <p>本クラスでは XML のプロパティ名を {@link PropertyNamingStrategies#SNAKE_CASE} として扱う前提です。
 *
 * <p><strong>注意</strong>：
 * 取得元 URL や XML 形式はラジコ側の仕様変更により変わる可能性があります。
 */
@RequiredArgsConstructor
@Slf4j
public class StationService {

    /**
     * HTTP 通信を行うクライアントです。
     */
    private final RdkHttpClient rdkHttpClient;

    /**
     * 指定したエリア（都道府県）に対応するラジオ局一覧を取得します。
     *
     * <p>アクセス URL:
     * {@code https://radiko.jp/v3/station/list/[areaId].xml}
     * <br>
     * {@code [areaId]} には {@link AreaPrefecture#getAreaId()}（例：{@code "JP13"}）を使用します。
     *
     * <p>取得した XML レスポンスは {@link AreaStations} にデシリアライズされ、
     * 指定エリア内のラジオ局情報一覧として返されます。
     *
     * @param area ラジコのエリア（都道府県）コード（{@code null} 不可）
     * @return 指定エリアのラジオ局一覧を表す {@link AreaStations}
     * @throws IOException 通信・レスポンス読み取り・XML デシリアライズで入出力エラーが発生した場合
     * @throws InterruptedException HTTP 通信処理中などにスレッドが割り込まれた場合
     * @throws NullPointerException {@code area} が {@code null} の場合（本メソッド内で明示チェックはしていません）
     */
    public AreaStations getStations(AreaPrefecture area) throws IOException, InterruptedException {
        String url = String.format("https://radiko.jp/v3/station/list/%s.xml", area.getAreaId());
        log.debug("getStations(AreaPrefecture) : アクセスURL={}", url);
        RdkHttpResponse response = rdkHttpClient.get(RdkHttpRequest.builder().url(url).build());
        String body = CommonUtils.getBody(response);
        log.debug("getStations(AreaPrefecture) : レスポンスボディ={}", body);
        ObjectMapper objectMapper =
                new XmlMapper()
                        .registerModule(new JavaTimeModule())
                        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        AreaStations data = objectMapper.readValue(body, AreaStations.class);
        log.debug("getStations(AreaPrefecture) : 取得結果={}", data);
        return data;
    }
}
