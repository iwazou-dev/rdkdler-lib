package net.iwazou.rdkdler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestUtils {
    private TestUtils() {}

    private static final String PROPERTY_FILE_NAME = "/test-info.properties";

    public static Properties loadProperties() throws IOException {
        /*
         * test-info.properties.sampleを元にtest-info.propertiesを作成し、
         * ラジコのログイン情報などを定義する。
         */
        try (InputStream in = TestUtils.class.getResourceAsStream(PROPERTY_FILE_NAME)) {
            if (in == null) {
                throw new IllegalStateException(
                        "rdkdler-common/src/testFixtures/resources/test-info.propertiesが存在しない");
            }
            Properties props = new Properties();
            props.load(in);
            // テスト用の必須プロパティチェック
            check(props, TestPropKeys.RADIKO_AREA_CURRENT_ID);
            check(props, TestPropKeys.RADIKO_STATION_CURRENT_ID);
            check(props, TestPropKeys.FFMPEG_PATH);
            return props;
        }
    }

    private static void check(Properties props, String propertyName) {
        String value = props.getProperty(propertyName);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    String.format("%sの%sが未設定", PROPERTY_FILE_NAME, propertyName));
        }
    }
}
