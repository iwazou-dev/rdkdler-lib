package net.iwazou.rdkdler.http;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * {@link RdkHttpClient} に渡す HTTP リクエスト情報を表す DTO（データ保持クラス）。
 *
 * <p>シンプルな GET / フォーム POST を想定し、以下の情報を保持します。
 * <ul>
 *   <li>{@code url}: 送信先 URL（必須想定）</li>
 *   <li>{@code headers}: リクエストヘッダ（任意）</li>
 *   <li>{@code parameters}: クエリ文字列またはフォームパラメータ（任意）</li>
 * </ul>
 *
 * <p>実際にどのように {@code parameters} を利用するか（GET のクエリに付与するか、
 * POST body に入れるか等）は {@link RdkHttpClient} の実装に委ねます。
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@Getter
@Builder
public class RdkHttpRequest {

    /**
     * 送信先 URL を取得します。
     *
     * @return 送信先 URL
     */
    private String url;

    /**
     * リクエストヘッダ を取得します。キーはヘッダ名、値はヘッダ値。
     *
     * @return リクエストヘッダ
     */
    private Map<String, String> headers;

    /**
     * パラメータ を取得します。GET のクエリ、または POST のフォームパラメータとして利用される想定。
     *
     * @return パラメータ
     */
    private Map<String, String> parameters;
}
