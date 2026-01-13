package net.iwazou.rdkdler.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Properties;
import net.iwazou.rdkdler.TestPropKeys;
import net.iwazou.rdkdler.TestUtils;
import net.iwazou.rdkdler.download.RdkAuthenticator.AuthResult;
import net.iwazou.rdkdler.http.internal.JdkRdkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IT_RdkAuthenticatorTests {
    private RdkAuthenticator rdkAuthenticator;

    @BeforeEach
    void setUp() {
        rdkAuthenticator =
                new RdkAuthenticator(
                        new JdkRdkHttpClient(
                                HttpClient.newBuilder()
                                        .connectTimeout(Duration.ofSeconds(20))
                                        .build()));
    }

    @DisplayName("login,logoutのテスト：正常系")
    @Test
    void test_login_01() throws IOException, InterruptedException {
        Properties props = TestUtils.loadProperties();
        String mail = props.getProperty(TestPropKeys.RADIKO_LOGIN_MAIL);
        String password = props.getProperty(TestPropKeys.RADIKO_LOGIN_PASSWORD);
        assumeTrue(mail != null || password != null, "アカウントが設定されていないのでテストをスキップする");

        assertThat(rdkAuthenticator.isLoggedIn()).isFalse();
        rdkAuthenticator.login(mail, password);
        assertThat(rdkAuthenticator.isLoggedIn()).isTrue();
        rdkAuthenticator.logout();
        assertThat(rdkAuthenticator.isLoggedIn()).isFalse();
    }

    @DisplayName("authのテスト：正常系")
    @Test
    void test_auth_01() throws IOException, InterruptedException {
        Properties props = TestUtils.loadProperties();
        String areaId = props.getProperty(TestPropKeys.RADIKO_AREA_CURRENT_ID);

        AuthResult result = rdkAuthenticator.auth();
        assertThat(result.authtoken()).isNotBlank();
        assertThat(result.areaId()).isEqualTo(areaId);
    }
}
