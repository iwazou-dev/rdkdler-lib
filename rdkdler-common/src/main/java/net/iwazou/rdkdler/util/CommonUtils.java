package net.iwazou.rdkdler.util;

import java.util.Locale;
import java.util.Objects;
import net.iwazou.rdkdler.exception.RdkHttpException;
import net.iwazou.rdkdler.exception.RdkResponseException;
import net.iwazou.rdkdler.http.RdkHttpResponse;

/**
 * 共通ユーティリティクラス。
 *
 * <p>入力値の簡易バリデーションや文字列判定、HTTPレスポンスのボディ取得など、複数モジュールから再利用される
 * 小さな処理を提供します。
 *
 * <p><b>設計方針</b>
 * <ul>
 *   <li>状態を持たない（static メソッドのみ）</li>
 *   <li>不正な引数や想定外の状態は {@link IllegalArgumentException} またはドメイン例外を送出します</li>
 * </ul>
 */
public class CommonUtils {

    /**
     * バリデーション失敗時に使用する共通メッセージ。
     */
    private static final String VALIDATION_FAILED = "Validation failed";

    /**
     * インスタンス化禁止。
     */
    private CommonUtils() {}

    /**
     * HTTPレスポンスから本文（body）を取得します。
     *
     * <p>次のルールで検証を行います。
     * <ul>
     *   <li>ステータスコードが 200 でない場合は {@link RdkHttpException} を送出</li>
     *   <li>body が {@code null} または空/空白のみの場合は {@link RdkResponseException} を送出</li>
     * </ul>
     *
     * @param response HTTPレスポンス（null は不可）
     * @return 空でない本文文字列
     * @throws NullPointerException response が {@code null} の場合
     * @throws RdkHttpException ステータスコードが 200 以外の場合
     * @throws RdkResponseException ステータスコードは 200 だが body が空/空白のみの場合
     */
    public static String getBody(RdkHttpResponse response)
            throws RdkHttpException, RdkResponseException {
        if (response.statusCode() != 200) {
            throw new RdkHttpException(response.statusCode(), response.body());
        }
        if (response.body() == null || response.body().isBlank()) {
            // ここはサーバーが200を返しているがbodyがないため、RdkResponseExceptionとする
            throw new RdkResponseException("empty body");
        }
        return response.body();
    }

    /**
     * 文字列が {@code null} でなく、かつ空文字（length=0）でないことを検証します。
     *
     * @param chars 検証対象文字列
     * @throws NullPointerException chars が {@code null} の場合
     * @throws IllegalArgumentException chars が空文字の場合
     */
    public static void notEmpty(String chars) {
        Objects.requireNonNull(chars, VALIDATION_FAILED);
        if (chars.isEmpty()) throw new IllegalArgumentException(VALIDATION_FAILED);
    }

    /**
     * 文字列が {@code null} でなく、かつ空白のみでないことを検証します。
     *
     * @param chars 検証対象文字列
     * @throws NullPointerException chars が {@code null} の場合
     * @throws IllegalArgumentException chars が空/空白のみの場合
     */
    public static void notBlank(String chars) {
        Objects.requireNonNull(chars, VALIDATION_FAILED);
        if (chars.isBlank()) throw new IllegalArgumentException(VALIDATION_FAILED);
    }

    /**
     * 式が {@code true} であることを検証します。
     *
     * @param expression 検証する式
     * @throws IllegalArgumentException expression が {@code false} の場合
     */
    public static void isTrue(boolean expression) {
        if (!expression) throw new IllegalArgumentException(VALIDATION_FAILED);
    }

    /**
     * 文字列が {@code null} または空/空白のみかどうかを返します。
     *
     * @param chars 判定対象文字列
     * @return {@code null} または空/空白のみの場合 {@code true}
     */
    public static boolean isBlank(String chars) {
        return (chars == null || chars.isBlank());
    }

    /**
     * 文字列が {@code null} ではなく、かつ空/空白のみではないかどうかを返します。
     *
     * @param chars 判定対象文字列
     * @return {@link #isBlank(String)} が {@code false} の場合 {@code true}
     */
    public static boolean isNotBlank(String chars) {
        return !isBlank(chars);
    }

    /**
     * 文字列が指定した接尾辞（suffix）で終わるかを、大小文字を無視して判定します。
     *
     * <p>両方が {@code null} の場合は {@code true} を返します。片方のみ {@code null} の場合は {@code false} です。
     *
     * @param str 対象文字列
     * @param suffix 接尾辞
     * @return 大小文字を無視して {@code str} が {@code suffix} で終わる場合 {@code true}
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null && suffix == null) return true;
        if (str == null) return false;
        if (suffix == null) return false;
        return str.toLowerCase(Locale.ROOT).endsWith(suffix.toLowerCase(Locale.ROOT));
    }
}
