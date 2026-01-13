package net.iwazou.rdkdler.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.iwazou.rdkdler.download.RdkAuthenticator.AuthResult;
import net.iwazou.rdkdler.http.RdkHttpClient;
import net.iwazou.rdkdler.http.RdkHttpRequest;
import net.iwazou.rdkdler.http.RdkHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RdkAuthenticatorMockTests {
    @Mock RdkHttpClient mockRdkHttpClient;
    private RdkAuthenticator rdkAuthenticator;

    @Captor ArgumentCaptor<RdkHttpRequest> rdkHttpRequestCaptor;

    @BeforeEach
    void setUp() {
        this.rdkAuthenticator = new RdkAuthenticator(mockRdkHttpClient);
    }

    @DisplayName("loginのテスト（モック）：正常系")
    @Test
    void test_mock_login_01() throws Exception {
        /*
         * モックの設定：ログインの場合のレスポンス
         */
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/login"))))
                .thenReturn(
                        new BasicRdkHttpResponse(
                                200,
                                null,
                                """
                                {"radiko_session":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}\
                                """));

        assertThat(rdkAuthenticator.isLoggedIn()).isFalse(); // ログイン前はfalse
        rdkAuthenticator.login("mail", "password");
        assertThat(rdkAuthenticator.isLoggedIn()).isTrue(); // ログイン後はtrue

        // postFormが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(1)).postForm(rdkHttpRequestCaptor.capture());

        // postFormの第一引数の確認
        String capturedUri = rdkHttpRequestCaptor.getValue().getUrl();
        assertThat(capturedUri).isEqualTo("https://radiko.jp/v4/api/member/login");
    }

    @DisplayName("loginのテスト（モック）：異常系")
    @ParameterizedTest(name = "No.{index} : レスポンス=[{0}], エンティティ=[{1}], エラーメッセージ=[{2}]")
    @MethodSource
    void test_mock_login_02(
            RdkHttpResponse response, Class<? extends Exception> exceptionType, String errorMessage)
            throws IOException, InterruptedException {

        /*
         * モックの設定：ログインの場合のレスポンス
         */
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/login"))))
                .thenReturn(response);

        assertThatExceptionOfType(exceptionType)
                .isThrownBy(
                        // テスト対象のメソッド呼び出し
                        () -> rdkAuthenticator.login("NG", "NG"))
                .withMessage(errorMessage);

        // postFormが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(1)).postForm(any(RdkHttpRequest.class));
    }

    static Stream<Arguments> test_mock_login_02() {
        return Stream.of(
                // ステータス 401 の場合
                Arguments.of(
                        new BasicRdkHttpResponse(
                                401,
                                new LinkedHashMap<>(),
                                """
                                {"status":"401","url":"","error_message":"パスワードまたはメールアドレスが違います。","cause":"auth","button_string":""}\
                                """),
                        net.iwazou.rdkdler.exception.RdkHttpException.class,
                        "HTTP error code: 401"),
                // ステータス 200 かつエンティティ null の場合
                Arguments.of(
                        new BasicRdkHttpResponse(200, new LinkedHashMap<>(), null),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "empty body"),
                // ステータス 200 かつエンティティ 空文字列 の場合
                Arguments.of(
                        new BasicRdkHttpResponse(200, new LinkedHashMap<>(), ""),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "empty body"),
                // ステータス 200 かつContent-Type APPLICATION_JSON以外 の場合
                Arguments.of(
                        new BasicRdkHttpResponse(
                                200,
                                new LinkedHashMap<>(),
                                """
                                <html></html>\
                                """),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        "invalid JSON response. body=<html></html>"),
                // ステータス 200 かつJSONに radiko_session が含まれない場合
                Arguments.of(
                        new BasicRdkHttpResponse(
                                200,
                                new LinkedHashMap<>(),
                                """
                                {"aaa":"bbb"}\
                                """),
                        net.iwazou.rdkdler.exception.RdkResponseException.class,
                        """
                        radiko_session does not exist in response. body={"aaa":"bbb"}\
                        """));
    }

    @DisplayName("logoutのテスト（モック）：正常系")
    @Test
    void test_mock_logout_01() throws IOException, InterruptedException {
        /*
         * ログインの場合のレスポンス
         */
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/login"))))
                .thenReturn(
                        new BasicRdkHttpResponse(
                                200,
                                null,
                                """
                                {"radiko_session":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}\
                                """));

        /*
         * ログアウトの場合のエラーレスポンス
         */
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/logout"))))
                .thenReturn(
                        new BasicRdkHttpResponse(
                                200,
                                null,
                                """
                                {"status":"200"}\
                                """));

        rdkAuthenticator.login("mail", "password");
        rdkAuthenticator.logout();

        verify(mockRdkHttpClient, times(2)).postForm(rdkHttpRequestCaptor.capture());
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(2)
                .extracting(RdkHttpRequest::getUrl)
                .containsExactly(
                        "https://radiko.jp/v4/api/member/login",
                        "https://radiko.jp/v4/api/member/logout");
    }

    @DisplayName("logoutのテスト（モック）：正常系：ログインしていない場合（なにもしない）")
    @Test
    void test_mock_logout_02() throws IOException, InterruptedException {

        // テスト対象の呼び出し
        rdkAuthenticator.logout();

        verify(mockRdkHttpClient, never()).postForm(any(RdkHttpRequest.class));
    }

    @DisplayName("logoutのテスト（モック）：異常系")
    @Test
    void test_mock_logout_03() throws Exception {
        /*
         * ログインの場合のレスポンス
         */
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/login"))))
                .thenReturn(
                        new BasicRdkHttpResponse(
                                200,
                                null,
                                """
                                {"radiko_session":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}
                                """));

        /*
         * ログアウトの場合のエラーレスポンス
         */
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/logout"))))
                .thenReturn(new BasicRdkHttpResponse(500, null, null));

        rdkAuthenticator.login("mail", "password");
        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkHttpException.class)
                .isThrownBy(
                        () -> {
                            rdkAuthenticator.logout();
                        })
                .withMessage("HTTP error code: 500");

        verify(mockRdkHttpClient, times(2)).postForm(rdkHttpRequestCaptor.capture());
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(2)
                .extracting(RdkHttpRequest::getUrl)
                .containsExactly(
                        "https://radiko.jp/v4/api/member/login",
                        "https://radiko.jp/v4/api/member/logout");
    }

    @DisplayName("auth1のテスト（モック）：異常系")
    @Test
    void test_mock_auth1_01() throws Exception {

        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(new BasicRdkHttpResponse(500, null, null));

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkHttpException.class)
                .isThrownBy(
                        () -> {
                            rdkAuthenticator.auth();
                        })
                .withMessage("HTTP error code: 500");

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(1)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        String capturedUri = rdkHttpRequestCaptor.getValue().getUrl();
        assertThat(capturedUri).isEqualTo("https://radiko.jp/v2/api/auth1");
    }

    @DisplayName("auth1のテスト（モック）：異常系")
    @Test
    void test_mock_auth1_02() throws Exception {

        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(new BasicRdkHttpResponse(200, Map.of(), "please send a part of key"));

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkResponseException.class)
                .isThrownBy(
                        () -> {
                            rdkAuthenticator.auth();
                        })
                .withMessage("X-Radiko-AuthToken is not present in header");

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(1)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        String capturedUri = rdkHttpRequestCaptor.getValue().getUrl();
        assertThat(capturedUri).isEqualTo("https://radiko.jp/v2/api/auth1");
    }

    @DisplayName("auth2のテスト（モック）：異常系")
    @Test
    void test_mock_auth2_01() throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(auth1_ok("token1xxxxxxxxxxxxxxxx", 16, 10));

        // auth2は普通に固定
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth2"))))
                .thenReturn(new BasicRdkHttpResponse(500, null, null));

        /*
         * 例外発生のテスト：
         */
        assertThatExceptionOfType(net.iwazou.rdkdler.exception.RdkHttpException.class)
                .isThrownBy(
                        // テスト対象の実行
                        () -> rdkAuthenticator.auth())
                .withMessage("HTTP error code: 500");

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(2)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(2)
                .extracting(RdkHttpRequest::getUrl)
                .containsExactly(
                        "https://radiko.jp/v2/api/auth1", "https://radiko.jp/v2/api/auth2");
    }

    @DisplayName("authのテスト（モック）：正常系：初回呼び出し")
    @Test
    void test_mock_auth_01() throws IOException, InterruptedException {

        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(auth1_ok("token1xxxxxxxxxxxxxxxx", 16, 10));

        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth2"))))
                .thenReturn(new BasicRdkHttpResponse(200, null, "JP14,神奈川県,kanagawa Japan"));

        // テスト対象の実行
        AuthResult result = rdkAuthenticator.auth();

        // 復帰値のチェック
        assertThat(result).isEqualTo(new AuthResult("token1xxxxxxxxxxxxxxxx", "JP14"));

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(2)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(2)
                .extracting(RdkHttpRequest::getUrl, RdkHttpRequest::getParameters)
                .containsExactly(
                        tuple("https://radiko.jp/v2/api/auth1", null),
                        tuple("https://radiko.jp/v2/api/auth2", null));
    }

    @DisplayName("authのテスト（モック）：正常系：認証有効期間内の二回目呼び出し")
    @Test
    void test_mock_auth_02() throws IOException, InterruptedException {

        // auth1だけ連続スタブ（1回目→auth1Resp1、2回目→auth1Resp2、3回目以降→auth1Resp2）
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(
                        // 一回目
                        auth1_ok("token1xxxxxxxxxxxxxxxx", 16, 10),
                        // 二回目
                        auth1_ok("token2xxxxxxxxxxxxxxxx", 16, 10));

        // auth2は普通に固定の復帰値
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth2"))))
                .thenReturn(
                        // 一回目
                        new BasicRdkHttpResponse(200, null, "JP14,神奈川県,kanagawa Japan"),
                        // 二回目 ※実際はありえないが二回目の復帰値を変える
                        new BasicRdkHttpResponse(200, null, "JP99,二回目,nikaime"));

        // テスト対象の実行
        rdkAuthenticator.auth();
        AuthResult result = rdkAuthenticator.auth();

        // 復帰値のチェック
        assertThat(result).isEqualTo(new AuthResult("token1xxxxxxxxxxxxxxxx", "JP14"));

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(2)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(2)
                .extracting(RdkHttpRequest::getUrl)
                .containsExactly(
                        "https://radiko.jp/v2/api/auth1", "https://radiko.jp/v2/api/auth2");
    }

    @DisplayName("authのテスト（モック）：正常系：認証有効期間外の二回目呼び出し")
    @Test
    void test_mock_auth_03() throws IOException, InterruptedException {

        // auth1だけ連続スタブ（1回目→auth1Resp1、2回目→auth1Resp2、3回目以降→auth1Resp2）
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(
                        // 一回目
                        auth1_ok("token1xxxxxxxxxxxxxxxx", 16, 10),
                        // 二回目
                        auth1_ok("token2xxxxxxxxxxxxxxxx", 16, 10));

        // auth2は普通に固定の復帰値
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth2"))))
                .thenReturn(
                        // 一回目
                        new BasicRdkHttpResponse(200, null, "JP14,神奈川県,kanagawa Japan"),
                        // 二回目 ※実際はありえないが二回目の復帰値を変える
                        new BasicRdkHttpResponse(200, null, "JP99,二回目,nikaime"));

        // 認証有効期間を0に設定
        rdkAuthenticator.setReauthenticationInterval(0);
        // テスト対象の実行
        rdkAuthenticator.auth();
        AuthResult result = rdkAuthenticator.auth();

        // 復帰値のチェック
        assertThat(result).isEqualTo(new AuthResult("token2xxxxxxxxxxxxxxxx", "JP99"));

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(4)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(4)
                .extracting(RdkHttpRequest::getUrl)
                .containsExactly(
                        "https://radiko.jp/v2/api/auth1", "https://radiko.jp/v2/api/auth2",
                        "https://radiko.jp/v2/api/auth1", "https://radiko.jp/v2/api/auth2");
    }

    @DisplayName("authのテスト（モック）：正常系：login済みの場合")
    @Test
    void test_mock_auth_04() throws IOException, InterruptedException {

        // loginのスタブ
        when(mockRdkHttpClient.postForm(
                        argThat(req -> req != null && req.getUrl().endsWith("/login"))))
                .thenReturn(
                        new BasicRdkHttpResponse(
                                200,
                                null,
                                """
                                {"radiko_session":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"}\
                                """));

        // auth1だけ連続スタブ（1回目→auth1Resp1、2回目→auth1Resp2、3回目以降→auth1Resp2）
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth1"))))
                .thenReturn(auth1_ok("token1xxxxxxxxxxxxxxxx", 16, 10));

        // auth2は普通に固定の復帰値
        when(mockRdkHttpClient.get(argThat(req -> req != null && req.getUrl().endsWith("/auth2"))))
                .thenReturn(new BasicRdkHttpResponse(200, null, "JP14,神奈川県,kanagawa Japan"));

        // テスト対象の実行
        rdkAuthenticator.login("mail", "password");
        rdkAuthenticator.auth();
        AuthResult result = rdkAuthenticator.auth();

        // 復帰値のチェック
        assertThat(result).isEqualTo(new AuthResult("token1xxxxxxxxxxxxxxxx", "JP14"));

        // getが何回呼び出されたか確認
        verify(mockRdkHttpClient, times(2)).get(rdkHttpRequestCaptor.capture());

        // getの第一引数の確認
        assertThat(rdkHttpRequestCaptor.getAllValues())
                .hasSize(2)
                .extracting(RdkHttpRequest::getUrl, RdkHttpRequest::getParameters)
                .containsExactly(
                        tuple("https://radiko.jp/v2/api/auth1", null),
                        tuple(
                                "https://radiko.jp/v2/api/auth2",
                                Map.of(
                                        "radiko_session",
                                        "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")));
    }

    private RdkHttpResponse auth1_ok(String token, int keylength, int keyoffset) {
        return new BasicRdkHttpResponse(
                200,
                Map.of(
                        "x-radiko-authtoken",
                        List.of(token),
                        "x-radiko-keylength",
                        List.of(String.valueOf(keylength)),
                        "x-radiko-keyoffset",
                        List.of(String.valueOf(keyoffset))),
                "please send a part of key");
    }

    private record BasicRdkHttpResponse(
            int statusCode, Map<String, List<String>> headers, String body)
            implements RdkHttpResponse {}
}
