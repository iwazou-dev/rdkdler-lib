package net.iwazou.rdkdler.http.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import net.iwazou.rdkdler.util.CommonUtils;

/**
 * JDK 標準の {@link java.net.http.HttpClient} を利用した {@link RdkHttpClient} 実装。
 *
 * <p>本クラスは {@link RdkHttpClient} の「実装詳細を隠蔽する」目的の内部実装（internal）です。
 * 呼び出し側は {@link RdkHttpClient} だけに依存し、JDK の HTTP API へ直接依存しないようにします。
 *
 * <p>対応する通信方式：
 * <ul>
 *   <li>GET（パラメータはクエリ文字列として付与）</li>
 *   <li>POST（{@code application/x-www-form-urlencoded}）</li>
 * </ul>
 *
 * <p><b>ヘッダ運用</b><br>
 * レスポンスヘッダはキーを小文字化して {@link RdkHttpResponse#headers()} に格納します。
 * {@link RdkHttpResponse#firstHeader(String)} と同じ運用方針です。
 */
public class JdkRdkHttpClient implements RdkHttpClient {

    /**
     * 実際の HTTP 通信に使用する JDK 標準 HttpClient。
     */
    private final HttpClient httpClient;

    /**
     * -- GETTER --
     * リクエストのタイムアウト時間（秒）を取得します。
     * <p>JDK {@link HttpRequest.Builder#timeout(Duration)} に渡されます。
     * デフォルトは 30 秒です。
     *
     * @return タイムアウト時間（秒）
     *
     * -- SETTER --
     * リクエストのタイムアウト時間（秒）を設定します。
     *
     * @param timeout タイムアウト時間（秒）
     */
    @Getter @Setter @NonNull private Duration timeout = Duration.ofSeconds(30);

    /**
     * 指定した {@link HttpClient} を用いて生成します。
     *
     * @param httpClient JDK 標準 HttpClient（null 不可）
     * @throws NullPointerException httpClient が {@code null} の場合
     */
    public JdkRdkHttpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * GET リクエストを送信します。
     *
     * <p>{@link RdkHttpRequest#getParameters()} が指定されている場合は、
     * URL の末尾に {@code ?key=value&...} 形式のクエリとして付与します（UTF-8 で URL エンコード）。
     *
     * @param rdkHttpRequest リクエスト情報（null 不可、URL 必須）
     * @return レスポンス
     * @throws NullPointerException rdkHttpRequest または URL が {@code null} の場合
     * @throws IOException 通信エラー等が発生した場合
     * @throws InterruptedException スレッド割り込みにより中断された場合
     */
    @Override
    public RdkHttpResponse get(RdkHttpRequest rdkHttpRequest)
            throws IOException, InterruptedException {
        Objects.requireNonNull(rdkHttpRequest);
        Objects.requireNonNull(rdkHttpRequest.getUrl());
        String para = buildFormDataString(rdkHttpRequest.getParameters(), StandardCharsets.UTF_8);
        Builder builder =
                HttpRequest.newBuilder()
                        .timeout(timeout)
                        .GET()
                        .uri(
                                URI.create(
                                        rdkHttpRequest.getUrl()
                                                + (CommonUtils.isNotBlank(para)
                                                        ? "?" + para
                                                        : "")));
        return execute(builder, rdkHttpRequest.getHeaders());
    }

    /**
     * フォーム（{@code application/x-www-form-urlencoded}）形式の POST リクエストを送信します。
     *
     * <p>{@link RdkHttpRequest#getParameters()} を {@code key=value&...} 形式にして body として送信します
     * （UTF-8 で URL エンコード）。
     *
     * @param rdkHttpRequest リクエスト情報（null 不可、URL 必須）
     * @return レスポンス
     * @throws NullPointerException rdkHttpRequest または URL が {@code null} の場合
     * @throws IOException 通信エラー等が発生した場合
     * @throws InterruptedException スレッド割り込みにより中断された場合
     */
    @Override
    public RdkHttpResponse postForm(RdkHttpRequest rdkHttpRequest)
            throws IOException, InterruptedException {
        Objects.requireNonNull(rdkHttpRequest);
        Objects.requireNonNull(rdkHttpRequest.getUrl());
        String formBody =
                buildFormDataString(rdkHttpRequest.getParameters(), StandardCharsets.UTF_8);
        Builder builder =
                HttpRequest.newBuilder()
                        .timeout(timeout)
                        .uri(URI.create(rdkHttpRequest.getUrl()))
                        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                        .POST(
                                HttpRequest.BodyPublishers.ofString(
                                        formBody, StandardCharsets.UTF_8));
        return execute(builder, rdkHttpRequest.getHeaders());
    }

    /**
     * {@link HttpRequest.Builder} を最終的な {@link HttpRequest} に組み立て、送信してレスポンスを変換します。
     *
     * <p>追加ヘッダ（{@code headers}）が指定されている場合、{@link Builder#setHeader(String, String)} で設定します。
     *
     * @param builder 送信するリクエストのビルダー
     * @param headers 追加ヘッダ（null 可）
     * @return {@link RdkHttpResponse} 実装（内部 record）
     * @throws IOException 通信エラー等が発生した場合
     * @throws InterruptedException スレッド割り込みにより中断された場合
     */
    private JdkRdkHttpResponse execute(Builder builder, Map<String, String> headers)
            throws IOException, InterruptedException {
        if (headers != null) headers.forEach(builder::setHeader);
        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());
        return new JdkRdkHttpResponse(
                response.statusCode(), toHeaderMap(response.headers()), response.body());
    }

    /**
     * JDK の {@link HttpHeaders} を、キー小文字化済みの {@code Map<String, List<String>>} に変換します。
     *
     * <p>ヘッダ名は大文字小文字を区別しない運用が一般的なため、
     * {@link Locale#ROOT} で小文字化して格納します。
     *
     * @param headers JDK の HttpHeaders
     * @return 変換後ヘッダマップ（キーは小文字、値は不変リスト）
     */
    private Map<String, List<String>> toHeaderMap(HttpHeaders headers) {
        Map<String, List<String>> map = new LinkedHashMap<>();
        headers.map().forEach((k, v) -> map.put(k.toLowerCase(Locale.ROOT), List.copyOf(v)));
        return map;
    }

    /**
     * パラメータの Map を {@code application/x-www-form-urlencoded} 形式の文字列に変換します。
     *
     * <p>変換ルール：
     * <ul>
     *   <li>{@code data} が {@code null} または空の場合は空文字を返す</li>
     *   <li>キーでソートして安定した順序にする（テスト容易性・再現性のため）</li>
     *   <li>キーと値を {@link URLEncoder} により指定 charset で URL エンコードする</li>
     *   <li>{@code key=value} を {@code &} で連結する</li>
     * </ul>
     *
     * @param data パラメータ
     * @param charset URL エンコードに使用する文字コード
     * @return {@code key=value&...} 形式の文字列（空の可能性あり）
     */
    private String buildFormDataString(Map<String, String> data, Charset charset) {
        if (data == null || data.size() == 0) {
            return "";
        }
        return data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // keyでソート
                .map(
                        entry -> {
                            String key = URLEncoder.encode(entry.getKey(), charset);
                            String value = URLEncoder.encode(entry.getValue(), charset);
                            return key + "=" + value;
                        })
                .collect(Collectors.joining("&"));
    }

    /**
     * {@link RdkHttpResponse} の JDK 実装。
     *
     * <p>JDK {@link HttpResponse} の情報を {@link RdkHttpResponse} へ投影するための軽量な内部 record です。
     *
     * @param statusCode HTTP ステータスコード
     * @param headers レスポンスヘッダ（キーは小文字化済み）
     * @param body レスポンス本文（null の可能性あり）
     */
    private record JdkRdkHttpResponse(
            int statusCode, Map<String, List<String>> headers, String body)
            implements RdkHttpResponse {}
}
