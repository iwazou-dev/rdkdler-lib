package net.iwazou.rdkdler.exception;

/**
 * HTTP レスポンスの内容が想定と異なる（不正・不足・解析失敗など）ことを表す例外。
 *
 * <p>例：
 * <ul>
 *   <li>ステータスコードは 200 だが body が空/空白のみ</li>
 *   <li>JSON/XML のパースに失敗した</li>
 *   <li>必要なフィールドが欠けている</li>
 * </ul>
 *
 * <p>「通信自体の失敗／ステータス異常」は {@link RdkHttpException}、
 * 「内容の不正」は本例外、といった形で責務を分けて扱うことを想定しています。
 */
public class RdkResponseException extends RdkException {

    /**
     * メッセージを指定して生成します。
     *
     * @param message 例外メッセージ
     */
    public RdkResponseException(String message) {
        super(message);
    }

    /**
     * メッセージと原因例外（cause）を指定して生成します。
     *
     * @param message 例外メッセージ
     * @param cause 原因例外
     */
    public RdkResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
