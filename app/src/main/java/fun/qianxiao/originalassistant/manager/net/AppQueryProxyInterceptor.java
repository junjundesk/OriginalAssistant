package fun.qianxiao.originalassistant.manager.net;

import android.text.TextUtils;

import java.io.IOException;

import fun.qianxiao.originalassistant.R;
import fun.qianxiao.originalassistant.utils.SettingPreferences;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AppQueryProxyInterceptor
 *
 * @Author QianXiao
 * @Date 2026/6/29
 */
public class AppQueryProxyInterceptor implements Interceptor {
    private static final String HEADER_APP_QUERY = "App-Query";
    private static final String DEFAULT_PROXY_URL = "https://proxy.tiandivip.cc";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (!"true".equals(request.header(HEADER_APP_QUERY))) {
            return chain.proceed(request);
        }

        Request.Builder requestBuilder = request.newBuilder().removeHeader(HEADER_APP_QUERY);
        String proxyUrl = SettingPreferences.getString(R.string.p_key_app_query_proxy_url, DEFAULT_PROXY_URL);
        if (TextUtils.isEmpty(proxyUrl)) {
            return chain.proceed(requestBuilder.build());
        }

        HttpUrl url = HttpUrl.parse(normalizeProxyUrl(proxyUrl) + request.url());
        if (url == null) {
            return chain.proceed(requestBuilder.build());
        }
        return chain.proceed(requestBuilder.url(url).build());
    }

    private String normalizeProxyUrl(String proxyUrl) {
        proxyUrl = proxyUrl.trim();
        while (proxyUrl.endsWith("/")) {
            proxyUrl = proxyUrl.substring(0, proxyUrl.length() - 1);
        }
        return proxyUrl + "/";
    }
}
