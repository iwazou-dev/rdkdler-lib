package net.iwazou.rdkdler.download;

import static java.util.Map.entry;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.iwazou.rdkdler.exception.RdkResponseException;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import net.iwazou.rdkdler.util.CommonUtils;

/**
 * ラジコの認証（ログイン/ログアウト、および auth1/auth2 による認証トークン取得）を行うクラスです。
 *
 * <p>主な役割：
 * <ul>
 *   <li>プレミアム会員ログインにより {@code radiko_session} を取得・保持する（必要な場合）</li>
 *   <li>{@code /v2/api/auth1} と {@code /v2/api/auth2} の 2 段階認証で {@code authtoken} を取得する</li>
 *   <li>{@code authtoken} / {@code areaId} をキャッシュし、一定時間ごとに再認証する</li>
 * </ul>
 *
 * <p><strong>スレッドセーフ</strong>：状態を持つため、公開メソッドは {@code synchronized} で直列化しています。
 * ただし {@link #setReauthenticationInterval(long)} や {@link #setAuthkeyValue(String)} は
 * 利用中に変更すると挙動が変わるため、基本的には初期化時に設定してください。
 */
@RequiredArgsConstructor
@Slf4j
public class RdkAuthenticator {

    /**
     * HTTP 通信を行うクライアントです。
     */
    private final RdkHttpClient rdkHttpClient;

    private static final String X_RADIKO_APP_VERSION = "X-Radiko-App-Version";
    private static final String X_RADIKO_APP = "X-Radiko-App";
    private static final String X_RADIKO_DEVICE = "X-Radiko-Device";
    private static final String X_RADIKO_USER = "X-Radiko-User";
    private static final String X_RADIKO_PARTIALKEY = "X-Radiko-PartialKey";
    private static final String X_RADIKO_AUTHTOKEN = "X-Radiko-AuthToken";
    private static final String X_RADIKO_KEYLENGTH = "X-Radiko-Keylength";
    private static final String X_RADIKO_KEYOFFSET = "X-Radiko-Keyoffset";
    private static final String RADIKO_SESSION = "radiko_session";

    /**
     * -- GETTER --
     * 認証トークン（authtoken）の再取得間隔（ミリ秒）を取得します。
     * <p>デフォルトは 1 時間です。
     *
     * @return 再認証間隔（ミリ秒）
     *
     * -- SETTER --
     * 認証トークン（authtoken）の再取得間隔（ミリ秒）を設定します。
     * <p>0 以下を設定した場合、トークン取得後の呼び出しごとに再認証される可能性があります。
     *
     * @param reauthenticationInterval 再認証間隔（ミリ秒）
     */
    @Getter @Setter private long reauthenticationInterval = 60L * 60L * 1000L; // 再認証間隔（ミリ秒）

    /**
     * 認証キー（full key）を設定します。
     *
     * <p>auth1 で取得した {@code X-Radiko-Keyoffset} / {@code X-Radiko-Keylength} を用いて
     * partial key を生成するために使用します。
     *
     * <p>この値はラジコの JavaScript プレイヤー（例：{@code playerCommon.js}）に含まれる値を参照しており、
     * 仕様変更等で変わる可能性があります。その場合は最新の値に差し替えてください。
     *
     * @param authkeyValue 認証キー（full key）
     */
    @Setter @NonNull private String authkeyValue = "bcd151073c03b352e1ef2fd66c32209da9ca0afa";

    private String radikoSession = null;
    private String authtoken = null;
    private long acquisitionTime = 0; // トークン取得時刻
    private String areaId = null;

    /**
     * プレミアム会員としてログインし、レスポンスから {@code radiko_session} を取得して保持します。
     *
     * <p>ログインに成功すると、保持している {@code authtoken} を無効化します（次回 {@link #auth()} で再認証）。
     *
     * @param mail メールアドレス（空不可）
     * @param password パスワード（空不可）
     * @throws IOException HTTP 通信に失敗した場合
     * @throws InterruptedException 通信が割り込まれた場合
     * @throws RdkResponseException レスポンスが不正（JSON でない/必須フィールド欠落など）の場合
     */
    public synchronized void login(String mail, String password)
            throws IOException, InterruptedException {
        CommonUtils.notEmpty(mail);
        CommonUtils.notEmpty(password);

        String url = "https://radiko.jp/v4/api/member/login";
        log.debug("login(String, String) : アクセスURL={}", url);
        Map<String, String> parameters =
                Map.ofEntries(entry("mail", mail), entry("pass", password));
        RdkHttpResponse response =
                rdkHttpClient.postForm(
                        RdkHttpRequest.builder().url(url).parameters(parameters).build());
        String body = CommonUtils.getBody(response);
        log.debug("login(String, String) : レスポンスボディ={}", body);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode radikoSessionNode;
        try {
            radikoSessionNode = mapper.readTree(body).get(RADIKO_SESSION);
        } catch (JsonParseException e) {
            throw new RdkResponseException("invalid JSON response. body=" + body, e);
        }
        if (radikoSessionNode == null) {
            throw new RdkResponseException(
                    "radiko_session does not exist in response. body=" + body);
        }
        this.radikoSession = radikoSessionNode.asText();
        this.authtoken = null; // 認証を一旦無効にする
        log.debug(
                "login(String, String) : radikoSession={}, authtoken={}",
                this.radikoSession,
                this.authtoken);
    }

    /**
     * ログアウトを行い、保持しているセッション情報を破棄します。
     *
     * <p>{@code radiko_session} が未設定の場合は何もしません。
     *
     * @throws IOException HTTP 通信に失敗した場合
     * @throws InterruptedException 通信が割り込まれた場合
     */
    public synchronized void logout() throws IOException, InterruptedException {
        if (CommonUtils.isBlank(this.radikoSession)) {
            // radikoSessionが未設定ならなにもしない
            return;
        }

        String url = "https://radiko.jp/v4/api/member/logout";
        log.debug("logout() : アクセスURL={}", url);
        Map<String, String> parameters = Map.of(RADIKO_SESSION, this.radikoSession);
        RdkHttpResponse response =
                rdkHttpClient.postForm(
                        RdkHttpRequest.builder().url(url).parameters(parameters).build());
        String body = CommonUtils.getBody(response);
        log.debug("logout() : レスポンスボディ={}", body);
        this.radikoSession = null;
        this.authtoken = null; // 認証を一旦無効にする
    }

    /**
     * ログイン済みかどうかを返します（{@code radiko_session} の有無で判定）。
     *
     * @return true: ログイン済み / false: 未ログイン
     */
    public synchronized boolean isLoggedIn() {
        return CommonUtils.isNotBlank(this.radikoSession);
    }

    /**
     * 認証（auth1/auth2）を実行し、認証トークン（authtoken）とエリアIDを取得します。
     *
     * <p>すでに {@code authtoken} を保持しており、取得時刻から {@link #getReauthenticationInterval()} 未満の場合は
     * キャッシュを返します。未取得、または有効期限切れ相当の場合は再認証を行います。
     *
     * @return 認証結果（authtoken と areaId）
     * @throws IOException HTTP 通信に失敗した場合
     * @throws InterruptedException 通信が割り込まれた場合
     * @throws RdkResponseException 必須ヘッダー欠落などにより認証処理が成立しない場合
     */
    synchronized AuthResult auth() throws IOException, InterruptedException {
        long now = System.currentTimeMillis();
        log.debug(
                "auth() : authtoken={}, acquisitionTime={}, now={}",
                authtoken,
                acquisitionTime,
                now);
        // 認証が必要か否かを判定する。
        if (this.authtoken == null
                || (now - this.acquisitionTime) >= this.reauthenticationInterval) {
            /*
             * authtokenが未取得の場合、または
             * 前回authtokenを取得してからreauthenticationIntervalミリ秒以上時間が経っている場合は認証を行う
             */
            String[] result1 = auth1();
            this.acquisitionTime = System.currentTimeMillis(); // トークン取得時刻の設定
            this.authtoken = result1[0];
            String[] result2 = auth2(result1[0], result1[1]);
            this.areaId = result2[0];
        }
        return new AuthResult(this.authtoken, this.areaId);
    }

    /**
     * {@link #auth()} の戻り値です。
     *
     * @param authtoken 音声データ取得などに使用する認証トークン
     * @param areaId 認証されたエリアID（例：JP13）
     */
    static record AuthResult(String authtoken, String areaId) {}

    /**
     * 認証 1（auth1）を実行します。
     *
     * <p>{@code https://radiko.jp/v2/api/auth1} にアクセスし、レスポンスヘッダーから
     * 認証に必要な情報（authtoken / keyoffset / keylength）を取得します。
     * 取得した offset/length と {@link #authkeyValue} から partial key を生成して返します。
     *
     * @return {@code String[]}（{@code [0]=authtoken}, {@code [1]=partialKey}）
     * @throws IOException HTTP 通信に失敗した場合
     * @throws InterruptedException 通信が割り込まれた場合
     * @throws NumberFormatException ヘッダー値が数値に変換できない場合
     */
    private String[] auth1() throws IOException, InterruptedException {
        String url = "https://radiko.jp/v2/api/auth1";
        log.debug("auth1() : アクセスURL={}", url);
        Map<String, String> headers =
                Map.ofEntries(
                        entry(X_RADIKO_APP, "pc_html5"),
                        entry(X_RADIKO_APP_VERSION, "0.0.1"),
                        entry(X_RADIKO_DEVICE, "pc"),
                        entry(X_RADIKO_USER, "dummy_user"));
        RdkHttpResponse response =
                rdkHttpClient.get(RdkHttpRequest.builder().url(url).headers(headers).build());
        String body = CommonUtils.getBody(response);
        log.debug("auth1() : レスポンスボディ={}", body);
        String token = getFirstHeader(response, X_RADIKO_AUTHTOKEN);
        int keyoffset = Integer.parseInt(getFirstHeader(response, X_RADIKO_KEYOFFSET));
        int keylength = Integer.parseInt(getFirstHeader(response, X_RADIKO_KEYLENGTH));
        log.debug(
                "auth1() : authtoken={}, keyoffset={}, keylength={}", token, keyoffset, keylength);
        String partialKey = partialKey(keyoffset, keylength);
        log.debug("auth1() : partialKey={}", partialKey);
        return new String[] {token, partialKey};
    }

    /**
     * 指定したレスポンスヘッダーの先頭要素を取得します。
     *
     * @param response HTTP レスポンス
     * @param headerKey ヘッダー名
     * @return ヘッダーの先頭値
     * @throws RdkResponseException ヘッダーが存在しない場合
     */
    private String getFirstHeader(RdkHttpResponse response, String headerKey)
            throws RdkResponseException {
        return response.firstHeader(headerKey)
                .orElseThrow(
                        () -> new RdkResponseException(headerKey + " is not present in header"));
    }

    /**
     * 認証 2（auth2）を実行します。
     *
     * <p>{@code https://radiko.jp/v2/api/auth2} に対し、auth1 で得た {@code authtoken} と
     * {@code partialkey} をヘッダーに付与してアクセスします。
     * レスポンス本文はカンマ区切りの文字列でエリア情報を返します（例：{@code JP14,神奈川県,kanagawa Japan}）。
     *
     * @param authtoken auth1 で取得した {@code X-Radiko-AuthToken}
     * @param partialkey auth1 の情報と {@link #authkeyValue} から生成した partial key
     * @return レスポンス本文を {@code ","} で分割した配列（{@code [0]=areaId}、以降はサーバー返却値の順）
     * @throws IOException HTTP 通信に失敗した場合
     * @throws InterruptedException 通信が割り込まれた場合
     * @throws RdkResponseException 引数が不正（空文字等）の場合
     */
    private String[] auth2(String authtoken, String partialkey)
            throws IOException, InterruptedException {
        CommonUtils.notBlank(authtoken);
        CommonUtils.notBlank(partialkey);

        String url = "https://radiko.jp/v2/api/auth2";
        log.debug("auth2(String, String) : アクセスURL={}", url);
        Map<String, String> headers =
                Map.ofEntries(
                        entry(X_RADIKO_DEVICE, "pc"),
                        entry(X_RADIKO_USER, "dummy_user"),
                        entry(X_RADIKO_AUTHTOKEN, authtoken),
                        entry(X_RADIKO_PARTIALKEY, partialkey));
        RdkHttpRequest.RdkHttpRequestBuilder builder =
                RdkHttpRequest.builder().url(url).headers(headers);
        if (CommonUtils.isNotBlank(this.radikoSession)) {
            builder.parameters(Map.of(RADIKO_SESSION, this.radikoSession));
        }
        RdkHttpResponse response = rdkHttpClient.get(builder.build());
        String body = CommonUtils.getBody(response);
        log.debug("auth2(String, String) : レスポンスボディ={}", body);
        return body.split(",", -1);
    }

    /**
     * auth1 で得た offset/length を用いて partial key を生成します。
     *
     * <p>{@link #authkeyValue} を UTF-8 バイト列にし、{@code keyoffset} から {@code keylength} バイトを切り出して
     * Base64 エンコードしたものを返します。
     * <p>範囲外が指定された場合は終端側を切り詰めます（例：{@code keyoffset + keylength} が末尾を超える場合）。
     *
     * @param keyoffset skip に相当（0 始まりのバイトオフセット）
     * @param keylength count に相当（取り出すバイト数）
     * @return Base64 エンコードした partial key
     */
    private String partialKey(int keyoffset, int keylength) {
        byte[] src = authkeyValue.getBytes(StandardCharsets.UTF_8);
        int end = Math.min(src.length, keyoffset + keylength);
        byte[] slice = Arrays.copyOfRange(src, keyoffset, end);
        return Base64.getEncoder().encodeToString(slice);
    }
}
