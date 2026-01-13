package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;
import lombok.Data;

/**
 * ラジオ局の番組情報（XML要素: {@code station}）
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@Data
public class StationProgramSchedule {

    /** XML属性: {@code id}（ラジオ局ID） */
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private String stationId;

    /** XML要素: {@code name}（ラジオ局名） */
    @JacksonXmlProperty(localName = "name")
    private String stationName;

    /** XML要素: {@code progs}（１日分の番組情報） */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "progs")
    private List<DailyProgramSchedule> dailyProgramSchedules;
}
