package org.jc.http;

import okhttp3.*;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.net.IPAddress;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HTTPClientUtil {

    private static OkHttpClient buildHttpClient(HTTPMessageConfigInterface config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Set timeouts
        builder.connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS);
        builder.readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS);

        // SSL check (for simplicity, this example does not handle SSL configurations)
        if (!config.isSecureCheckEnabled()) {
            builder.hostnameVerifier((hostname, session) -> true);
            // Additional SSL configurations can be added here
        }

        // Proxy configuration
        IPAddress proxyAddress = config.getProxyAddress();
        if (proxyAddress != null) {
            builder.proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP,
                    new java.net.InetSocketAddress(proxyAddress.getInetAddress(), proxyAddress.getPort())));
        }

        return builder.build();
    }

    private static Request buildRequest(HTTPMessageConfigInterface config) {
        Request.Builder requestBuilder = new Request.Builder();

        // Set URL
        String url = config.getURL() != null ? config.getURL() : config.getURI();
        requestBuilder.url(url);

        // Set HTTP method and content
        String method = config.getMethod().toString();
        RequestBody body = null;
        if (config.getContent() != null) {
            body = RequestBody.create(config.getContent(), MediaType.parse(config.getContentType()));
        }

        switch(config.getMethod())
        {
            case GET:
                requestBuilder.get();

                break;
            case POST:
                requestBuilder.post(body);
                break;
            case HEAD:
                break;
            case OPTIONS:
                break;
            case PUT:
                requestBuilder.put(body);
                break;
            case DELETE:
                requestBuilder.delete(body);
                break;
            case TRACE:
                break;
            case CONNECT:
                break;
            case PATCH:
                requestBuilder.patch(body);
                break;
            case COPY:
                break;
            case LINK:
                break;
            case UNLINK:
                break;
            case PURGE:
                break;
            case LOCK:
                break;
            case UNLOCK:
                break;
            case PROPFIND:
                break;
            default:
                throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }

        switch (method) {
            case "GET":
                requestBuilder.get();
                break;
            case "POST":
                requestBuilder.post(body != null ? body : RequestBody.create(new byte[0]));
                break;
            case "PUT":
                requestBuilder.put(body != null ? body : RequestBody.create(new byte[0]));
                break;
            case "DELETE":
                requestBuilder.delete(body != null ? body : RequestBody.create(new byte[0]));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported HTTP method: " + method);
        }

        // Set headers
        NVGenericMap headers = config.getHeaders();
        if (headers != null) {
            for (GetNameValue<?> header : headers.values()) {
                requestBuilder.addHeader(header.getName(), header.getValue().toString());
            }
        }

        // Set User-Agent
        String userAgent = config.getUserAgent();
        if (userAgent != null) {
            requestBuilder.addHeader("User-Agent", userAgent);
        }

        // Set cookies
        String cookie = config.getCookie();
        if (cookie != null) {
            requestBuilder.addHeader("Cookie", cookie);
        }

        return requestBuilder.build();
    }

    public static Response executeRequest(HTTPMessageConfigInterface config) throws IOException {
        OkHttpClient client = buildHttpClient(config);
        Request request = buildRequest(config);
        return client.newCall(request).execute();
    }

    public static void main(String[] args) {
        // Example usage
        HTTPMessageConfigInterface config =  HTTPMessageConfig.createAndInit("https://jsonplaceholder.typicode.com/posts", null, HTTPMethod.POST, false );

        config.setContentType("application/json");
        config.setContent("{\"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}");

        try {
            Response response = executeRequest(config);
            System.out.println("Response code: " + response.code());
            System.out.println("Response body: " + response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
