package net.iwazou.rdkdler.download;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.process.JaffreeAbnormalExitException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.iwazou.rdkdler.download.RdkAuthenticator.AuthResult;
import net.iwazou.rdkdler.exception.RdkDownloadException;
import net.iwazou.rdkdler.util.CommonUtils;

/**
 * ラジコのタイムフリー番組（時刻範囲指定）の音声をダウンロードし、ファイルに保存するサービスです。
 *
 * <p>内部では {@link RdkAuthenticator} による認証結果（authtoken/areaId）を用いて
 * タイムフリーのプレイリスト（m3u8）URL を組み立て、{@link FFmpeg} を外部プロセスとして実行して
 * 音声ストリームを取得します。
 *
 * <p><strong>注意</strong>
 * <ul>
 *   <li>FFmpeg 実行環境が必要です（{@link FFmpegFactory} の実装に依存）。</li>
 *   <li>出力ファイルは {@code setOverwriteOutput(true)} により上書きされます。</li>
 *   <li>{@code coverUrl} を指定した場合、拡張子が {@code .jpg/.jpeg/.png} のときのみカバーアートを埋め込みます。</li>
 * </ul>
 *
 * <p><strong>スレッドセーフ性</strong>：
 * {@link #download(String, LocalDateTime, LocalDateTime, Path, String)} は {@code synchronized} で直列化されます。
 * （認証トークンの再取得や外部プロセス実行が絡むため、同時実行を避けたい場合に有効です）
 *
 * <p>使用例：
 * <pre>{@code
 * RdkDownloadService service = new RdkDownloadService(authenticator, ffmpegFactory);
 * service.download("TBS", from, to, Path.of("out.m4a"));
 * service.download("TBS", from, to, Path.of("out_with_cover.m4a"), "https://example.com/cover.jpg");
 * }</pre>
 */
@RequiredArgsConstructor
@Slf4j
public class RdkDownloadService {

    /**
     * ラジコの認証クラスです。
     */
    private final RdkAuthenticator authenticator;

    /**
     *  {@link FFmpeg} インスタンスを生成するためのファクトリインターフェースです。
     */
    private final FFmpegFactory fFmpegFactory;

    private static final String X_RADIKO_AUTHTOKEN = "X-Radiko-AuthToken";
    private static final String X_RADIKO_AREAID = "X-Radiko-AreaId";
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 指定した放送局・時刻範囲のタイムフリー音声をダウンロードして保存します（カバーアート埋め込みなし）。
     *
     * <p>本メソッドは {@link #download(String, LocalDateTime, LocalDateTime, Path, String)} を
     * {@code coverUrl=null} で呼び出します。
     *
     * @param stationId 放送局ID（例：TBS）。{@code null} は想定しません。
     * @param from 開始日時。{@code null} は想定しません。
     * @param to 終了日時。{@code null} は想定しません。
     * @param out 出力ファイルの {@link Path}。{@code null} は想定しません。
     * @throws IOException 認証処理や外部プロセス実行に付随する I/O で失敗した場合
     * @throws InterruptedException 認証処理や外部プロセス実行が割り込まれた場合
     * @throws RdkDownloadException FFmpeg が異常終了した場合（exit code 非 0 など）
     */
    public void download(String stationId, LocalDateTime from, LocalDateTime to, Path out)
            throws IOException, InterruptedException {
        download(stationId, from, to, out, null);
    }

    /**
     * 指定した放送局・時刻範囲のタイムフリー音声をダウンロードして保存します。
     *
     * <p>処理概要：
     * <ol>
     *   <li>{@link RdkAuthenticator#auth()} で認証し、authtoken/areaId を取得（必要に応じて再認証）</li>
     *   <li>{@code from/to} を {@code yyyyMMddHHmmss} 形式に変換し、m3u8 プレイリスト URL を生成</li>
     *   <li>FFmpeg の入力に m3u8 URL を指定し、radiko 用 HTTP ヘッダー（authtoken/areaId）を付与</li>
     *   <li>音声を再エンコードせずコピーし（{@code -c:a copy}）、必要なビットストリーム変換を適用</li>
     *   <li>{@code coverUrl} が JPEG/PNG の場合は画像も入力に追加し、カバーアートとして埋め込み</li>
     * </ol>
     *
     * <p>{@code coverUrl} が {@code null} または未対応拡張子の場合、画像埋め込みは行いません。
     * 出力ファイルは常に上書きされます（{@code setOverwriteOutput(true)}）。
     *
     * @param stationId 放送局ID（例：TBS）。{@code null} は想定しません。
     * @param from 開始日時。{@code null} は想定しません。
     * @param to 終了日時。{@code null} は想定しません。
     * @param out 出力ファイルの {@link Path}。{@code null} は想定しません。
     * @param coverUrl カバーアート画像 URL。{@code null} 可。
     *                 {@code .jpg/.jpeg/.png} の場合のみ埋め込み対象になります。
     * @throws IOException 認証処理や外部プロセス実行に付随する I/O で失敗した場合
     * @throws InterruptedException 認証処理や外部プロセス実行が割り込まれた場合
     */
    public synchronized void download(
            String stationId, LocalDateTime from, LocalDateTime to, Path out, String coverUrl)
            throws IOException, InterruptedException {

        // 認証する
        AuthResult result = authenticator.auth();
        log.debug(
                "download(String, LocalDateTime, LocalDateTime, Path, String) : authtoken={},"
                        + " areaId={}",
                result.authtoken(),
                result.areaId());

        String fromStr = from.format(DATE_TIME_FMT);
        String toStr = to.format(DATE_TIME_FMT);

        // 擬似ランダム32桁（16進）
        String lsid = randomHex(32);

        String m3u8 =
                String.format(
                        "https://radiko.jp/v2/api/ts/playlist.m3u8?station_id=%s&start_at=%s&ft=%s&end_at=%s&to=%s&seek=%s&l=15&lsid=%s&type=c",
                        stationId, fromStr, fromStr, toStr, toStr, fromStr, lsid);
        log.debug(
                "download(String, LocalDateTime, LocalDateTime, Path, String) : FFmpeg URL={}",
                m3u8);

        String headers =
                String.join(
                        "\r\n",
                        X_RADIKO_AREAID + ": " + result.areaId(),
                        X_RADIKO_AUTHTOKEN + ": " + result.authtoken());
        log.debug(
                "download(String, LocalDateTime, LocalDateTime, Path, String) : FFmpeg headers={}",
                headers);
        final AtomicLong durationMillis = new AtomicLong();
        FFmpeg fFmpeg =
                fFmpegFactory
                        .create()
                        .addInput(
                                UrlInput.fromUrl(m3u8)
                                        .addArguments("-fflags", "+discardcorrupt")
                                        .addArguments("-headers", headers));
        UrlOutput output =
                UrlOutput.toPath(out)
                        // マッピング：入力0の音声
                        .addArguments("-map", "0:a")
                        // 音声を再エンコードせずにそのままコピーする
                        .addArguments("-c:a", "copy")
                        // ADTS → ASC 変換する
                        .addArguments("-bsf:a", "aac_adtstoasc");

        String imageFormat = getImageFormat(coverUrl);
        if (imageFormat != null) {
            // 入力2：カバーアート（JPEG）
            fFmpeg.addInput(UrlInput.fromUrl(coverUrl));
            output
                    // マッピング：入力1の画像
                    .addArguments("-map", "1:v")
                    // 画像を判別した画像形式として埋め込む
                    .addArguments("-c:v", imageFormat)
                    // attached_pic フラグを付与
                    .addArguments("-disposition:v:0", "attached_pic");
        }
        try {
            fFmpeg.setOverwriteOutput(true).addOutput(output).execute();
        } catch (JaffreeAbnormalExitException e) {
            throw new RdkDownloadException(e);
        }
        log.debug(
                "download(String, LocalDateTime, LocalDateTime, Path, String) : Path={},"
                        + " durationMillis={}",
                out.toString(),
                durationMillis.get());
    }

    /**
     * カバーアート画像 URL から、FFmpeg に渡す画像コーデック名（文字列）を判定して返します。
     *
     * <p>判定は URL 末尾の拡張子（大文字小文字は区別しない）で行います。
     * <ul>
     *   <li>{@code .jpg/.jpeg} → {@code "mjpeg"}</li>
     *   <li>{@code .png} → {@code "png"}</li>
     *   <li>上記以外、または {@code coverUrl==null} → {@code null}</li>
     * </ul>
     *
     * @param coverUrl 画像ファイル URL（{@code null} 可）
     * @return FFmpeg に渡す画像コーデック名（{@code "mjpeg"} または {@code "png"}）。未対応の場合は {@code null}。
     */
    String getImageFormat(String coverUrl) {
        String imageFormat = null;
        if (CommonUtils.endsWithIgnoreCase(coverUrl, ".jpg")
                || CommonUtils.endsWithIgnoreCase(coverUrl, ".jpeg")) {
            imageFormat = "mjpeg";
        } else if (CommonUtils.endsWithIgnoreCase(coverUrl, ".png")) {
            imageFormat = "png";
        }
        return imageFormat;
    }

    /**
     * 指定された長さ {@code n} のランダムな小文字 16 進数文字列（0-9, a-f）を生成します。
     *
     * <p>{@link SecureRandom} により {@code (n + 1) / 2} バイト分の乱数を生成し、各バイトを
     * 2 桁の小文字 16 進表現に変換して連結したのち、先頭 {@code n} 文字を返します。
     *
     * @param n 生成する 16 進数文字列の長さ（文字数）。0 以上を想定します。
     *          0 の場合は空文字列を返します。
     * @return 長さ {@code n} のランダムな 16 進数文字列
     * @throws NegativeArraySizeException {@code n} が負の場合（内部配列長が負になり得るため）
     */
    private String randomHex(int n) {
        byte[] buf = new byte[(n + 1) / 2];
        new SecureRandom().nextBytes(buf);
        StringBuilder sb = new StringBuilder(n);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.substring(0, n);
    }
}
