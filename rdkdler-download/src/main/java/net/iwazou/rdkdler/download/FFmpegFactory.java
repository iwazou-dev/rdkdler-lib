package net.iwazou.rdkdler.download;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;

/**
 * {@link FFmpeg} インスタンスを生成するためのファクトリインターフェースです。
 *
 * <p>実装クラス側で、FFmpeg 実行ファイルのパスや共通オプション、ログ設定などを
 * 集中管理し、呼び出し側はこのインターフェースを通じて {@link FFmpeg} を取得します。
 * これにより、テスト時には本物の {@link FFmpeg} の代わりにモックを返す実装に差し替えるなど、
 * DI（依存性の注入）やテスト容易性を高めることができます。
 */
public interface FFmpegFactory {

    /**
     * {@link FFmpeg} インスタンスを生成して返します。
     *
     * <p>実装クラスでは、FFmpeg バイナリの場所や共通で付与するオプション、
     * ログレベルなどを必要に応じて設定してください。
     *
     * @return 実行可能な {@link FFmpeg} インスタンス
     */
    FFmpeg create();
}
