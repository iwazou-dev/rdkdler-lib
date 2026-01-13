package net.iwazou.rdkdler.area;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AreaPrefectureTests {

    @DisplayName("getNameのテスト：正常系")
    @Test
    void test_getName_01() {
        AreaPrefecture code = AreaPrefecture.HOKKAIDO;
        assertThat(code.getAreaId()).isEqualTo("JP1");
        assertThat(code.getKanjiName()).isEqualTo("北海道");
    }
}
