package com.kevin.proxyhits;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

//用于进行Https请求的HttpClient
public class SSLClient extends DefaultHttpClient {
    public static final int DEFAULT_READ_TIMEOUT = 20 * 1000;

    public static <T> T doPost(String url, Map<String, String> param, ProxyServer server, ResponseHandler<T> responseHandler) throws Exception {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = "";
        httpClient = new SSLClient();
        httpPost = new HttpPost(url);
        HttpHost host = new HttpHost(server.getIp(), server.getPort());
        /**
         * 设置User-Agent 请求头,部分目标URL可能因为没有该消息头返回403
         */
        httpPost.addHeader("User-Agent", "Mozilla/37.0 (compatible; MSIE 10.0; Windows NT; DigExt)");
        RequestConfig conf = RequestConfig.custom().setProxy(host).setSocketTimeout(DEFAULT_READ_TIMEOUT).build();
        httpPost.setConfig(conf);
        // 设置参数
        if (param != null && param.size() > 0) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            Iterator iterator = param.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> elem = (Entry<String, String>) iterator.next();
                list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, "UTF-8");
                httpPost.setEntity(entity);
            }
        }
        HttpResponse response = httpClient.execute(httpPost);
        if (response != null) {
            if (null != responseHandler) {
                return responseHandler.handleResponse(response);
            } else {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity);
                }
            }
        }
        return (T) result;
    }

    public SSLClient() throws Exception {
        super();
        SSLContext ctx = SSLContext.getInstance("TLS");
        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        ctx.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", 443, ssf));
    }
}
