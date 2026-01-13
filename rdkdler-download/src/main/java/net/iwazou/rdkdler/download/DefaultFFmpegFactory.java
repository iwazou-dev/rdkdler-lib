package net.iwazou.rdkdler.download;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * {@link FFmpegFactory} のデフォルト実装クラスです。
 *
 * <p>指定されたディレクトリ配下に存在する FFmpeg 実行ファイルを利用して、
 * {@link FFmpeg} インスタンスを生成します。
 *
 * <p>このクラスのインスタンスは、次の 2 通りの方法で生成できます。
 * <ul>
 *   <li>{@code new DefaultFFmpegFactory(path)} –
 *       FFmpeg バイナリを配置したディレクトリを指定するコンストラクタ</li>
 *   <li>{@code new DefaultFFmpegFactory()} – 引数なしのデフォルトコンストラクタ</li>
 * </ul>
 *
 * <p>ソースコード上では Lombok の {@code @AllArgsConstructor} /
 * {@code @NoArgsConstructor} によって、これらのコンストラクタを自動生成しています。
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@NoArgsConstructor
@AllArgsConstructor
public class DefaultFFmpegFactory implements FFmpegFactory {

    /**
     * FFmpeg 実行ファイルが配置されているディレクトリのパス。
     *
     * <p>この値は {@link FFmpeg#atPath(Path)} にそのまま渡されます。
     * {@code null} を指定した場合の挙動は、使用している Jaffree のバージョンにおける
     * {@link FFmpeg#atPath(Path)} の仕様に従います。
     */
    private Path pathToDir = null;

    /**
     * {@link #pathToDir} で指定したディレクトリ配下の FFmpeg 実行ファイルを利用して、
     * {@link FFmpeg} インスタンスを生成します。
     *
     * <p>{@link FFmpeg} の生成に失敗した場合や FFmpeg 実行ファイルが見つからない場合の
     * 例外／エラーは、{@link FFmpeg#atPath(Path)} の仕様に準じます。
     *
     * @return 生成された {@link FFmpeg} インスタンス
     */
    @Override
    public FFmpeg create() {
        return FFmpeg.atPath(pathToDir);
    }
}
