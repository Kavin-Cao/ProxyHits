package com.kevin.proxyhits;

import java.io.IOException;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;

public class HttpUtils {

    public static final int DEFAULT_READ_TIMEOUT = 20 * 1000;

    public static String getByProxy(String url, ProxyServer server) throws ClientProtocolException, IOException {
        return get(url, server, null);
    }

    public static String get(String url) throws ClientProtocolException, IOException {
        return get(url, null, null);
    }

    public static <T> T getByProxy(String url, ProxyServer server, ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
        return get(url, server, responseHandler);
    }

    public static <T> T get(String url, ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
        return get(url, null, responseHandler);
    }

    private static <T> T get(String url, ProxyServer server, ResponseHandler<T> responseHandler) throws ClientProtocolException, IOException {
        String resStr = "";
        HttpGet httpget = new HttpGet(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        /**
         * 设置User-Agent 请求头,部分目标URL可能因为没有该消息头返回403
         */
        httpget.addHeader("User-Agent", "Mozilla/37.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
        if (null != server) {
            HttpHost host = new HttpHost(server.getIp(), server.getPort());
            RequestConfig conf = RequestConfig.custom().setProxy(host).setSocketTimeout(DEFAULT_READ_TIMEOUT).build();
            httpget.setConfig(conf);
        }
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpget);

            if (null != responseHandler) {
                return responseHandler.handleResponse(response);
            } else {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    resStr = EntityUtils.toString(entity);
                }
            }
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (T) resStr;
    }
}
