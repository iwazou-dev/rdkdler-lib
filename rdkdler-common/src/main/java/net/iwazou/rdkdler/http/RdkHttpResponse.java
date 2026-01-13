package net.iwazou.rdkdler.http;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * {@link RdkHttpClient} が返す HTTP レスポンスの抽象化インターフェース。
 *
 * <p>実装（JDK HttpClient / Apache HttpClient 等）によるレスポンス表現の違いを吸収し、
 * 呼び出し側が共通の API でステータス・ヘッダ・本文へアクセスできるようにします。
 *
 * <p><b>ヘッダキーの運用について</b><br>
 * {@link #headers()} のキーは大文字小文字の揺れがあるため、実装側で小文字化して格納する運用を推奨します。
 * 本インターフェースの {@link #firstHeader(String)} も、その前提でキーを小文字化して参照します。
 */
public interface RdkHttpResponse {

    /**
     * HTTP ステータスコードを返します。
     *
     * @return ステータスコード（例: 200）
     */
    int statusCode();

    /**
     * レスポンスヘッダを返します。
     *
     * <p>キーはヘッダ名、値はヘッダ値のリストです。複数回出現するヘッダに対応するため
     * 値を {@link List} としています。
     *
     * @return ヘッダマップ
     */
    Map<String, List<String>> headers(); // keyは小文字化など運用を決める

    /**
     * レスポンス本文を返します。
     *
     * <p>本文が存在しない場合は {@code null} を返す実装もあり得ます。
     *
     * @return 本文（null の可能性あり）
     */
    String body();

    /**
     * 指定したヘッダ名に対応する最初の値を返します。
     *
     * <p>ヘッダ名は {@link Locale#ROOT} を用いて小文字化し、{@link #headers()} を参照します。
     * そのため、実装側でもヘッダキーを小文字化して保持している運用を推奨します。
     *
     * @param name ヘッダ名（例: {@code "content-type"}）
     * @return 値が存在する場合は最初の要素、存在しない場合は {@link Optional#empty()}
     */
    default Optional<String> firstHeader(String name) {
        List<String> vs = headers().getOrDefault(name.toLowerCase(Locale.ROOT), List.of());
        return vs.isEmpty() ? Optional.empty() : Optional.ofNullable(vs.get(0));
    }
}
