package net.iwazou.rdkdler.exception;

import lombok.Getter;

/**
 * HTTP リクエストの結果がエラー（想定外のステータス）だったことを表す例外。
 *
 * <p>ステータスコードと、可能であればレスポンス本文（body）を保持します。
 * 呼び出し側は {@link #getStatusCode()} を参照して分岐したり、調査用に {@link #getBody()} をログ出力できます。
 *
 * <p>本例外は「HTTP レベルの失敗（例: 200 以外）」を表現する目的で、レスポンス形式が不正・空など
 * “内容”に関する問題は {@link RdkResponseException} と区別して扱うことを想定しています。
 */
@Getter
public class RdkHttpException extends RdkException {

    /**
     * HTTP ステータスコードを取得します。
     *
     * @return HTTP ステータスコード
     */
    private final int statusCode;

    /**
     * レスポンス本文を取得します。null の可能性があります。
     *
     * @return レスポンス本文
     */
    private final String body;

    /**
     * ステータスコードと本文を指定して生成します。
     *
     * @param statusCode HTTP ステータスコード
     * @param body レスポンス本文（null 可）
     */
    public RdkHttpException(int statusCode, String body) {
        super(String.format("HTTP error code: %d", statusCode));
        this.statusCode = statusCode;
        this.body = body;
    }
}
