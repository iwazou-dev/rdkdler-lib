package net.iwazou.rdkdler.download.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

@JacksonXmlRootElement(localName = "urls")
@Data
public class Urls {

    @JacksonXmlProperty(localName = "url")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<UrlEntry> urlEntries;

    @Data
    public static class UrlEntry {
        @JacksonXmlProperty(isAttribute = true)
        private int areafree;

        @JacksonXmlProperty(isAttribute = true)
        private int maxDelay;

        @JacksonXmlProperty(isAttribute = true)
        private int timefree;

        @JacksonXmlProperty(localName = "playlist_create_url")
        private String playlistCreateUrl;
    }
}
