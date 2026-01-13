package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.List;
import lombok.Data;

/**
 * ラジオ局（station）情報を表すモデルです。
 *
 * <p>XML: {@code <station>...</station>}
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@Data
public class Station {

    /** XML要素: {@code id}（ラジオ局ID） */
    @JacksonXmlProperty(localName = "id")
    private String stationId;

    /** XML要素: {@code name}（ラジオ局名） */
    @JacksonXmlProperty(localName = "name")
    private String stationName;

    /** XML要素: {@code ascii_name} */
    private String asciiName;

    /** XML要素: {@code ruby} */
    private String ruby;

    /** XML要素: {@code areafree} */
    private Integer areafree;

    /** XML要素: {@code timefree} */
    private Integer timefree;

    /** XML要素: {@code logo} */
    @JacksonXmlProperty(localName = "logo")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Logo> logos;

    /** XML要素: {@code banner} */
    private String banner;

    /** XML要素: {@code href} */
    private String href;

    /** XML要素: {@code simul_max_delay} */
    private Integer simulMaxDelay;

    /** XML要素: {@code tf_max_delay} */
    private Integer tfMaxDelay;

    /**
     * ロゴ（logo）要素を表すモデルです。
     *
     * <p>XML: {@code <logo width="..." height="..." align="...">URL</logo>}
     */
    @Data
    public static class Logo {

        /** XML属性: {@code width} */
        @JacksonXmlProperty(isAttribute = true)
        private String width;

        /** XML属性: {@code height} */
        @JacksonXmlProperty(isAttribute = true)
        private String height;

        /** XML属性: {@code align} */
        @JacksonXmlProperty(isAttribute = true)
        private String align;

        /** XMLテキスト: {@code logo} 要素の本文（URL） */
        @JacksonXmlText private String value;
    }
}
