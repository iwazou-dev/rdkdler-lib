package net.iwazou.rdkdler;

import java.util.List;
import java.util.Map;
import net.iwazou.rdkdler.http.RdkHttpResponse;

public record TestRdkHttpResponse(int statusCode, Map<String, List<String>> headers, String body)
        implements RdkHttpResponse {}
