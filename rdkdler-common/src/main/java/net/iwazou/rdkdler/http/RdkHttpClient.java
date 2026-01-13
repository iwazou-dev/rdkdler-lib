package net.iwazou.rdkdler.http;

import java.io.IOException;

/**
 * rdkdler ライブラリにおける HTTP クライアントの抽象化インターフェース。
 *
 * <p>実装（例: JDK {@code java.net.http.HttpClient}、Apache HttpClient など）を隠蔽し、
 * 呼び出し側が特定ライブラリに依存しない形で HTTP 通信を行えるようにします。
 *
 * <p>本インターフェースは、主に以下の点を目的とします。
 * <ul>
 *   <li>実装差し替え（本番/テスト、将来のHTTP実装変更）</li>
 *   <li>ユニットテスト容易性（モックしやすい）</li>
 *   <li>リクエスト/レスポンス型の統一（{@link RdkHttpRequest}, {@link RdkHttpResponse}）</li>
 * </ul>
 */
public interface RdkHttpClient {

    /**
     * HTTP GET リクエストを送信します。
     *
     * <p>{@link RdkHttpRequest#getParameters()} が指定されている場合の扱い（URLへのクエリ付与など）は
     * 実装側の責務とします。
     *
     * @param rdkHttpRequest 送信するリクエスト情報
     * @return HTTP レスポンス
     * @throws IOException 通信エラー等が発生した場合
     * @throws InterruptedException スレッド割り込みにより中断された場合
     */
    RdkHttpResponse get(RdkHttpRequest rdkHttpRequest) throws IOException, InterruptedException;

    /**
     * {@code application/x-www-form-urlencoded} 形式の HTTP POST リクエストを送信します。
     *
     * <p>通常 {@link RdkHttpRequest#getParameters()} の内容をフォームパラメータとして送信します。
     * 具体的なエンコード（UTF-8 など）や Content-Type 設定は実装側の責務とします。
     *
     * @param rdkHttpRequest 送信するリクエスト情報
     * @return HTTP レスポンス
     * @throws IOException 通信エラー等が発生した場合
     * @throws InterruptedException スレッド割り込みにより中断された場合
     */
    RdkHttpResponse postForm(RdkHttpRequest rdkHttpRequest)
            throws IOException, InterruptedException;
}
