package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

/**
 * エリア（都道府県）内のラジオ局一覧を表すモデルです。
 *
 * <p>XML: {@code <stations ...>...</stations>}
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@JacksonXmlRootElement(localName = "stations")
@Data
public class AreaStations {

    /** XML属性: {@code area_id}（都道府県ID）*/
    @JacksonXmlProperty(isAttribute = true)
    private String areaId;

    /** XML属性: {@code area_name}（都道府県名）*/
    @JacksonXmlProperty(isAttribute = true)
    private String areaName;

    /** XML要素: {@code station}（ラジオ局情報一覧） */
    @JacksonXmlProperty(localName = "station")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Station> stations;
}
