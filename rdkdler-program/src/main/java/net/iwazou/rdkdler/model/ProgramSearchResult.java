package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 番組検索結果情報ルートJSON
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@Data
public class ProgramSearchResult {

    /** JSONキー: {@code meta} */
    private ResultMeta meta;

    /** JSONキー: {@code data} */
    @JsonProperty("data")
    private List<ResultData> resultDatas;

    /**
     * 検索結果メタ情報（meta）
     */
    @Data
    public static class ResultMeta {

        /** JSONキー: {@code key} */
        private List<String> key;

        /** JSONキー: {@code station_id} */
        private List<String> stationId;

        /** JSONキー: {@code area_id} */
        private List<String> areaId;

        /** JSONキー: {@code cur_area_id} */
        private String curAreaId;

        /** JSONキー: {@code region_id} */
        private String regionId;

        /** JSONキー: {@code start_day} */
        private LocalDate startDay;

        /** JSONキー: {@code end_day} */
        private LocalDate endDay;

        /** JSONキー: {@code filter} */
        private String filter;

        /** JSONキー: {@code result_count} */
        private int resultCount;

        /** JSONキー: {@code page_idx} */
        private int pageIdx;

        /** JSONキー: {@code row_limit} */
        private int rowLimit;

        /** JSONキー: {@code kakuchou} */
        private List<String> kakuchou;

        /** JSONキー: {@code suisengo} */
        private String suisengo;

        /** JSONキー: {@code genre_id} */
        private List<String> genreId;
    }

    /**
     * 番組情報（data）
     */
    @Data
    public static class ResultData {

        /** JSONキー: {@code start_time} */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        /** JSONキー: {@code end_time} */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        /** JSONキー: {@code start_time_s} */
        private String startTimeS;

        /** JSONキー: {@code end_time_s} */
        private String endTimeS;

        /** JSONキー: {@code program_date} */
        @JsonFormat(pattern = "yyyyMMdd")
        private LocalDate programDate;

        /** JSONキー: {@code program_url} */
        private String programUrl;

        /** JSONキー: {@code station_id} */
        private String stationId;

        /** JSONキー: {@code performer} */
        private String performer;

        /** JSONキー: {@code title} */
        private String title;

        /** JSONキー: {@code info} */
        private String info;

        /** JSONキー: {@code description} */
        private String description;

        /** JSONキー: {@code status} */
        private String status;

        /** JSONキー: {@code img} */
        private String img;

        /** JSONキー: {@code genre} */
        private Genre genre;

        /** JSONキー: {@code ts_in_ng} */
        private Integer tsInNg;

        /** JSONキー: {@code ts_out_ng} */
        private Integer tsOutNg;

        /** JSONキー: {@code tsplus_in_ng} */
        private Integer tsplusInNg;

        /** JSONキー: {@code tsplus_out_ng} */
        private Integer tsplusOutNg;

        /** JSONキー: {@code metas} */
        private List<Meta> metas;
    }

    /**
     * ジャンル情報（genre）
     */
    @Data
    public static class Genre {

        /** JSONキー: {@code personality} */
        private Category personality;

        /** JSONキー: {@code program} */
        private Category program;
    }

    /**
     * パーソナリティーカテゴリー/番組カテゴリー情報（personality / program）
     */
    @Data
    public static class Category {

        /** JSONキー: {@code id} */
        private String id;

        /** JSONキー: {@code name} */
        private String name;
    }

    /**
     * 番組メタ情報（metas）
     */
    @Data
    public static class Meta {

        /** JSONキー: {@code name} */
        private String name;

        /** JSONキー: {@code value} */
        private String value;
    }
}
