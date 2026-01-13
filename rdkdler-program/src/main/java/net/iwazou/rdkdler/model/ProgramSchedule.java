package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

/**
 * ラジオ局のスケジュール情報（XML要素: {@code radiko}）
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@JacksonXmlRootElement(localName = "radiko")
@Data
public class ProgramSchedule {

    /** XML要素: {@code ttl} */
    private String ttl;

    /** XML要素: {@code srvtime} */
    private String srvtime;

    /** XML要素: {@code stations}/{@code station}（ラジオ局の番組情報一覧） */
    @JacksonXmlElementWrapper(localName = "stations")
    @JacksonXmlProperty(localName = "station")
    private List<StationProgramSchedule> stationProgramSchedules;
}
