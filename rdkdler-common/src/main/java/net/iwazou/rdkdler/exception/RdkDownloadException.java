package net.iwazou.rdkdler.exception;

/**
 * ダウンロード処理（録音・保存・変換など）に関連する例外。
 *
 * <p>内部で発生した {@link Throwable} をラップして呼び出し側に通知する用途を想定しています。
 * 具体的には、ネットワーク・IO・外部プロセス（例: FFmpeg）など、ダウンロード処理の実行中に発生した
 * 低レベル例外を上位のドメイン例外へ変換する際に利用します。
 */
public class RdkDownloadException extends RdkException {

    /**
     * 原因例外（cause）を指定して生成します。
     *
     * @param cause 原因例外
     */
    public RdkDownloadException(Throwable cause) {
        super(cause);
    }
}
