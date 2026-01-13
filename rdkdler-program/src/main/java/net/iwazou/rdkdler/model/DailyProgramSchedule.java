package net.iwazou.rdkdler.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

/**
 * ラジオ局の１日分の番組情報（XML要素: {@code progs}）
 */
@SuppressWarnings("doclint:missing") // javadoc生成時の警告を抑止
@Data
public class DailyProgramSchedule {

    /** XML要素: {@code date}（放送日※朝5時区切り） */
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate date;

    /** XML要素: {@code prog}（番組情報一覧） */
    @JacksonXmlProperty(localName = "prog")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<ProgramEntry> programEntrys;
}
