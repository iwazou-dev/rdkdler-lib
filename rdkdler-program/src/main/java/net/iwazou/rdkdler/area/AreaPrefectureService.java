package net.iwazou.rdkdler.area;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.iwazou.rdkdler.exception.RdkResponseException;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import net.iwazou.rdkdler.util.CommonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * ラジコのエリア情報（接続元から判定されたエリアID）を取得し、{@link AreaPrefecture} に変換して返すサービスです。
 *
 * <p>ラジコ側が（おそらく）クライアントの接続元情報に基づいて判定したエリアID（例：{@code JP14}）を取得し、
 * {@link AreaPrefecture#fromAreaId(String)} で都道府県 enum に変換します。
 *
 * <p><strong>注意</strong>：
 * ここで得られるエリア情報はラジコ側の判定結果であり、VPN/プロキシ/回線種別などにより
 * 物理的な現在地と一致しない場合があります。
 */
@RequiredArgsConstructor
@Slf4j
public class AreaPrefectureService {

    /**
     * HTTP 通信を行うクライアントです。
     */
    private final RdkHttpClient rdkHttpClient;

    /**
     * ラジコのサーバーにアクセスし、現在の接続元に対応する {@link AreaPrefecture} を取得します。
     *
     * <p>アクセス先：{@code https://api.radiko.jp/apparea/area}
     *
     * <p>レスポンス本文は、次のような {@code span} 要素を含む文字列（JavaScript 断片を含む HTML）であることを期待します。
     * <pre>{@code
     * document.write('<span class="JP14">KANAGAWA JAPAN</span>');
     * }</pre>
     *
     * <p>本メソッドは、本文を Jsoup でパースし、最初の {@code <span>} 要素の {@code class} 属性値
     * （例：{@code "JP14"}）をエリアIDとして取り出します。取り出したエリアIDが {@link AreaPrefecture} に
     * 定義されていればそれを返し、期待形式でない／未知のエリアIDの場合は {@link RdkResponseException} を送出します。
     *
     * @return ラジコが判定したエリアIDに対応する都道府県（{@link AreaPrefecture}）
     * @throws IOException 通信またはレスポンス取得・本文読み取りで入出力エラーが発生した場合
     * @throws InterruptedException 通信処理が割り込まれた場合
     * @throws RdkResponseException レスポンス本文が期待形式でない場合（{@code <span>} がない／{@code class} が空など）、
     *                             またはエリアIDが未知で {@link AreaPrefecture#fromAreaId(String)} が解決できない場合
     */
    public AreaPrefecture getCurrentAreaPrefecture() throws IOException, InterruptedException {
        String url = "https://api.radiko.jp/apparea/area";
        log.debug("getCurrentAreaPrefecture() : アクセスURL={}", url);
        RdkHttpResponse response = rdkHttpClient.get(RdkHttpRequest.builder().url(url).build());
        String body = CommonUtils.getBody(response);
        log.debug("getCurrentAreaPrefecture() : レスポンスボディ={}", body);
        Element firstSpan = Jsoup.parse(body).select("span").first();
        if (firstSpan == null) {
            // ここはサーバーが200を返しているが期待する形式ではないため、ResponseExceptionとする
            throw new RdkResponseException("<span> does not exist");
        }
        String areaId = firstSpan.attr("class");
        if (areaId.isBlank()) {
            // ここはサーバーが200を返しているが期待する形式ではないため、ResponseExceptionとする
            throw new RdkResponseException("class attribute of <span> does not exist");
        }
        AreaPrefecture areaPrefecture =
                AreaPrefecture.fromAreaId(areaId)
                        .orElseThrow(() -> new RdkResponseException("unknown area_id"));
        log.debug("getCurrentAreaPrefecture() : 都道府県コード={}", areaPrefecture);
        return areaPrefecture;
    }
}
