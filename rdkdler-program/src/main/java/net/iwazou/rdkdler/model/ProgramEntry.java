package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 番組情報（XML要素: {@code prog}）
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@Data
public class ProgramEntry {

    /** XML属性: {@code id} */
    @JacksonXmlProperty(isAttribute = true)
    private String id;

    /** XML属性: {@code master_id} */
    @JacksonXmlProperty(isAttribute = true)
    private String masterId;

    /** XML属性: {@code ft} */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime ft;

    /** XML属性: {@code to} */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime to;

    /** XML属性: {@code ftl} */
    @JacksonXmlProperty(isAttribute = true)
    private String ftl;

    /** XML属性: {@code tol} */
    @JacksonXmlProperty(isAttribute = true)
    private String tol;

    /** XML属性: {@code dur} */
    @JacksonXmlProperty(isAttribute = true)
    private String dur;

    /** XML要素: {@code title} */
    private String title;

    /** XML要素: {@code url} */
    private String url;

    /** XML要素: {@code url_link} */
    private String urlLink;

    /** XML要素: {@code failed_record} */
    private String failedRecord;

    /** XML要素: {@code ts_in_ng} */
    private Integer tsInNg;

    /** XML要素: {@code tsplus_in_ng} */
    private Integer tsplusInNg;

    /** XML要素: {@code ts_out_ng} */
    private Integer tsOutNg;

    /** XML要素: {@code tsplus_out_ng} */
    private Integer tsplusOutNg;

    /** XML要素: {@code desc} */
    private String desc;

    /** XML要素: {@code info} */
    private String info;

    /** XML要素: {@code pfm} */
    private String pfm;

    /** XML要素: {@code img} */
    private String img;

    /** XML要素: {@code tag} */
    @JacksonXmlProperty(localName = "tag")
    private List<Item> items;

    /** XML要素: {@code genre} */
    private Genre genre;

    /** XML要素: {@code metas} */
    @JacksonXmlProperty(localName = "metas")
    private List<Meta> metas;

    /**
     * タグ項目（XML要素: {@code item}）
     */
    @Data
    public static class Item {

        /** XML要素: {@code name} */
        private String name;
    }

    /**
     * ジャンル情報（XML要素: {@code genre}）
     */
    @Data
    public static class Genre {

        /** XML要素: {@code personality} */
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "personality")
        private List<Personality> personalitys;

        /** XML要素: {@code program} */
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "program")
        private List<Program> programs;
    }

    /** 人物カテゴリ（XML要素: {@code personality}） */
    @Data
    public static class Personality {

        /** XML属性: {@code id} */
        @JacksonXmlProperty(isAttribute = true)
        private String id;

        /** XML要素: {@code name} */
        private String name;
    }

    /**
     * 番組カテゴリ（XML要素: {@code program}）
     */
    @Data
    public static class Program {

        /** XML属性: {@code id} */
        @JacksonXmlProperty(isAttribute = true)
        private String id;

        /** XML要素: {@code name} */
        private String name;
    }

    /**
     * メタ情報（XML要素: {@code meta}）
     */
    @Data
    public static class Meta {

        /** XML属性: {@code name} */
        @JacksonXmlProperty(isAttribute = true)
        private String name;

        /** XML属性: {@code value} */
        @JacksonXmlProperty(isAttribute = true)
        private String value;
    }
}
