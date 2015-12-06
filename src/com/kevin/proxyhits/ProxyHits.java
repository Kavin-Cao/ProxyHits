package com.kevin.proxyhits;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

public class ProxyHits {
    private static Logger log = Logger.getLogger(ProxyHits.class.getName());

    public static void main(String[] args) throws ClientProtocolException, IOException {
        try {
            ProxyHits proxyHits = new ProxyHits();
            proxyHits.startHits();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void startHits() throws IOException {
     // 获得配置文件的实例
        final Config config = Config.get();

        // 获得代理服务器列表
        List<ProxyServer> servers = config.getServers();
        if (servers == null || servers.isEmpty()) {
            System.out.println("servers配置文件下的代理服务器列表为空,请配置代理服务器列表");
            return;
        }

        // 存入代理请求成功的日志string
        final List<String> successLogs = Collections.synchronizedList(new ArrayList<String>());
        // 存入代理请求失败的日志string
        final List<String> failedLogs = Collections.synchronizedList(new ArrayList<String>());
        for (final ProxyServer proxyServer : servers) {
            ThreadPools.getPool().addTask(new Runnable() {
                public void run() {
                    for (int i = 0, len = config.getTargets().size(); i < len; i++) {
                        final String target = config.getTargets().get(i);
                        ThreadPools.getPool().addTask(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    boolean delay = config.isDelay();
                                    int minDelay = 0;
                                    int maxDelay = 0;
                                    int sleep = 0;
                                    if (delay) {
                                        minDelay = config.getMinDelay();
                                        maxDelay = config.getMaxDelay();
                                        if (maxDelay < minDelay) {
                                            delay = false;
                                            sleep = 0;
                                        } else {
                                            sleep = (int) Math.round(Math.random() * (maxDelay - minDelay) + minDelay);
                                        }
                                    }
                                    try {
                                        Thread.sleep(sleep);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    // 开始通过代理方式请求目标url
                                    boolean res = HttpUtils.getByProxy(target, proxyServer, new ResponseHandler<Boolean>() {

                                        @Override
                                        public Boolean handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                                            if (response.getStatusLine().getStatusCode() == 200) {
                                                return true;
                                            } else {
                                                return false;
                                            }

                                        }
                                    });
                                    if (res) {
                                        successLogs.add("通过代理服务器【" + proxyServer.getIp() + ":" + proxyServer.getPort() + "】访问目标地址" + target + "成功");
                                    } else {
                                        failedLogs.add("通过代理服务器【" + proxyServer.getIp() + ":" + proxyServer.getPort() + "】访问目标地址" + target + "失败");
                                    }
                                } catch (IOException e) {

                                }
                            }
                        });
                    }
                }
            });
        }

        ThreadPools.getPool().shutdown();
        while (true) {
            if (ThreadPools.getPool().isTerminated()) {
                log.info("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆");
                break;
            }
        }

        failedLogs.addAll(successLogs);
        log.info("共有" + successLogs.size() + "线程执行完毕");
        for (String l : successLogs) {
            log.info(l);
        }
    }
}
