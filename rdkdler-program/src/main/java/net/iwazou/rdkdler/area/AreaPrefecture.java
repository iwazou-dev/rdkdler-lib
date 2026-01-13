package net.iwazou.rdkdler.area;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

/**
 * 都道府県を表す列挙型です。
 *
 * <p>本列挙型は、プロジェクト内で使用する「エリアID（例：{@code JP14}）」と
 * 都道府県名（漢字表記）を対応付けるための定義です。
 *
 * <p>主な用途：
 * <ul>
 *   <li>エリアID文字列から都道府県を特定する（{@link #fromAreaId(String)}）</li>
 *   <li>表示用の都道府県名（{@link #getKanjiName()}）を取得する</li>
 * </ul>
 *
 * <p><strong>注意</strong>：
 * エリアIDの一致判定は文字列の完全一致（大文字小文字も含めて）です。
 */
public enum AreaPrefecture {

    /** 北海道（areaId: {@code JP1}） */
    HOKKAIDO("JP1", "北海道"),
    /** 青森県（areaId: {@code JP2}） */
    AOMORI("JP2", "青森"),
    /** 岩手県（areaId: {@code JP3}） */
    IWATE("JP3", "岩手"),
    /** 宮城県（areaId: {@code JP4}） */
    MIYAGI("JP4", "宮城"),
    /** 秋田県（areaId: {@code JP5}） */
    AKITA("JP5", "秋田"),
    /** 山形県（areaId: {@code JP6}） */
    YAMAGATA("JP6", "山形"),
    /** 福島県（areaId: {@code JP7}） */
    FUKUSHIMA("JP7", "福島"),
    /** 茨城県（areaId: {@code JP8}） */
    IBARAKI("JP8", "茨城"),
    /** 栃木県（areaId: {@code JP9}） */
    TOCHIGI("JP9", "栃木"),
    /** 群馬県（areaId: {@code JP10}） */
    GUNMA("JP10", "群馬"),
    /** 埼玉県（areaId: {@code JP11}） */
    SAITAMA("JP11", "埼玉"),
    /** 千葉県（areaId: {@code JP12}） */
    CHIBA("JP12", "千葉"),
    /** 東京都（areaId: {@code JP13}） */
    TOKYO("JP13", "東京"),
    /** 神奈川県（areaId: {@code JP14}） */
    KANAGAWA("JP14", "神奈川"),
    /** 新潟県（areaId: {@code JP15}） */
    NIIGATA("JP15", "新潟"),
    /** 富山県（areaId: {@code JP16}） */
    TOYAMA("JP16", "富山"),
    /** 石川県（areaId: {@code JP17}） */
    ISHIKAWA("JP17", "石川"),
    /** 福井県（areaId: {@code JP18}） */
    FUKUI("JP18", "福井"),
    /** 山梨県（areaId: {@code JP19}） */
    YAMANASHI("JP19", "山梨"),
    /** 長野県（areaId: {@code JP20}） */
    NAGANO("JP20", "長野"),
    /** 岐阜県（areaId: {@code JP21}） */
    GIFU("JP21", "岐阜"),
    /** 静岡県（areaId: {@code JP22}） */
    SHIZUOKA("JP22", "静岡"),
    /** 愛知県（areaId: {@code JP23}） */
    AICHI("JP23", "愛知"),
    /** 三重県（areaId: {@code JP24}） */
    MIE("JP24", "三重"),
    /** 滋賀県（areaId: {@code JP25}） */
    SHIGA("JP25", "滋賀"),
    /** 京都府（areaId: {@code JP26}） */
    KYOTO("JP26", "京都"),
    /** 大阪府（areaId: {@code JP27}） */
    OSAKA("JP27", "大阪"),
    /** 兵庫県（areaId: {@code JP28}） */
    HYOGO("JP28", "兵庫"),
    /** 奈良県（areaId: {@code JP29}） */
    NARA("JP29", "奈良"),
    /** 和歌山県（areaId: {@code JP30}） */
    WAKAYAMA("JP30", "和歌山"),
    /** 鳥取県（areaId: {@code JP31}） */
    TOTTORI("JP31", "鳥取"),
    /** 島根県（areaId: {@code JP32}） */
    SHIMANE("JP32", "島根"),
    /** 岡山県（areaId: {@code JP33}） */
    OKAYAMA("JP33", "岡山"),
    /** 広島県（areaId: {@code JP34}） */
    HIROSHIMA("JP34", "広島"),
    /** 山口県（areaId: {@code JP35}） */
    YAMAGUCHI("JP35", "山口"),
    /** 徳島県（areaId: {@code JP36}） */
    TOKUSHIMA("JP36", "徳島"),
    /** 香川県（areaId: {@code JP37}） */
    KAGAWA("JP37", "香川"),
    /** 愛媛県（areaId: {@code JP38}） */
    EHIME("JP38", "愛媛"),
    /** 高知県（areaId: {@code JP39}） */
    KOUCHI("JP39", "高知"),
    /** 福岡県（areaId: {@code JP40}） */
    FUKUOKA("JP40", "福岡"),
    /** 佐賀県（areaId: {@code JP41}） */
    SAGA("JP41", "佐賀"),
    /** 長崎県（areaId: {@code JP42}） */
    NAGASAKI("JP42", "長崎"),
    /** 熊本県（areaId: {@code JP43}） */
    KUMAMOTO("JP43", "熊本"),
    /** 大分県（areaId: {@code JP44}） */
    OITA("JP44", "大分"),
    /** 宮崎県（areaId: {@code JP45}） */
    MIYAZAKI("JP45", "宮崎"),
    /** 鹿児島県（areaId: {@code JP46}） */
    KAGOSHIMA("JP46", "鹿児島"),
    /** 沖縄県（areaId: {@code JP47}） */
    OKINAWA("JP47", "沖縄");

    /**
     * エリアID（検索キー）を取得します。例：{@code JP14}
     *
     * @return エリアID
     */
    @Getter private String areaId;

    /**
     * 都道府県名（漢字表記）を取得します。例：{@code 神奈川}
     *
     * @return 都道府県名
     */
    @Getter private String kanjiName;

    private AreaPrefecture(String areaId, String kanjiName) {
        this.areaId = areaId;
        this.kanjiName = kanjiName;
    }

    /**
     * エリアID（例：{@code "JP14"}）に対応する {@link AreaPrefecture} を取得します。
     *
     * <p>一致判定は {@link String#equals(Object)} による完全一致です（前後空白の除去や大小文字の正規化は行いません）。
     * {@code areaId} が {@code null} の場合や、該当が存在しない場合は {@link Optional#empty()} を返します。
     *
     * @param areaId 検索したいエリアID文字列（{@code null} 可）
     * @return {@code areaId} に一致する都道府県。見つからない場合は {@link Optional#empty()}
     */
    public static Optional<AreaPrefecture> fromAreaId(String areaId) {
        return Arrays.stream(AreaPrefecture.values())
                .filter(area -> area.getAreaId().equals(areaId))
                .findFirst();
    }
}
