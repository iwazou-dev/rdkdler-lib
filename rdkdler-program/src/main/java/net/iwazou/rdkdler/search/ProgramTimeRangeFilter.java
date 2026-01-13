package net.iwazou.rdkdler.search;

/**
 * ラジコの番組検索 API における状態フィルタ（クエリパラメータ {@code filter}）を表す列挙型です。
 *
 * <p>この列挙型は、番組検索 API の {@code filter} パラメータに渡す文字列値を型安全に扱うために使用します。
 * パラメータ値と検索対象の対応は次の通りです。</p>
 *
 * <ul>
 *   <li>{@link #PAST}   : {@code "past"}    … 放送済みの番組（タイムフリー等）を検索対象とする</li>
 *   <li>{@link #FUTURE} : {@code "future"}  … 未放送（放送予定）の番組を検索対象とする</li>
 *   <li>{@link #ALL}    : 空文字列         … 放送済み・未放送の両方を検索対象とする</li>
 * </ul>
 *
 * <p><strong>注意:</strong> 本列挙型が表す値は、ラジコ側の実装（非公開・内部仕様）に依存します。
 * 将来、値や意味が変更される可能性があるため、API呼び出しが失敗した場合はパラメータ仕様の再確認を推奨します。</p>
 *
 * @see ProgramSearchService#setFilter(ProgramTimeRangeFilter)
 */
public enum ProgramTimeRangeFilter {

    /**
     * 放送済みの番組のみを検索対象とするフィルタ（{@code filter=past}）。
     */
    PAST("past"),

    /**
     * 未放送（放送予定）の番組のみを検索対象とするフィルタ（{@code filter=future}）。
     */
    FUTURE("future"),

    /**
     * 放送済み・未放送の両方を検索対象とするフィルタ（{@code filter=}：空文字列）。
     */
    ALL("");

    /**
     * 番組検索 API の {@code filter} パラメータに渡す文字列値。
     */
    private String filter;

    private ProgramTimeRangeFilter(String filter) {
        this.filter = filter;
    }

    /**
     * 番組検索 API の {@code filter} パラメータに渡す文字列値を返します。
     *
     * @return {@code "past"} / {@code "future"} / 空文字列（{@link #ALL} の場合）
     */
    public String getValue() {
        return this.filter;
    }
}
