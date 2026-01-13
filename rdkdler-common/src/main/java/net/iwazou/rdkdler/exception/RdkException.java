package net.iwazou.rdkdler.exception;

import java.io.IOException;

/**
 * rdkdler ライブラリで発生する例外の基底クラス。
 *
 * <p>本プロジェクトでは、外部通信や入出力（HTTPアクセス、ストリーム処理、ファイル操作など）に起因する
 * 失敗を呼び出し側へ通知するため、チェック例外（{@link IOException}）として扱います。
 *
 * <p>具体的な例外種別（例：HTTPエラー、レスポンス不正など）は本クラスを継承したサブクラスとして定義します。
 */
public class RdkException extends IOException {

    /**
     * メッセージを指定して例外を生成します。
     *
     * @param message 例外メッセージ
     */
    public RdkException(String message) {
        super(message);
    }

    /**
     * 原因例外（cause）を指定して例外を生成します。
     *
     * @param cause 原因例外
     */
    public RdkException(Throwable cause) {
        super(cause);
    }

    /**
     * メッセージと原因例外（cause）を指定して例外を生成します。
     *
     * @param message 例外メッセージ
     * @param cause 原因例外
     */
    public RdkException(String message, Throwable cause) {
        super(message, cause);
    }
}
