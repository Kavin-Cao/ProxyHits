package com.kevin.proxyhits;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.http.conn.util.InetAddressUtils;

public class Config {

    private static final Config config = new Config();

    private Properties properties;

    private Logger log = Logger.getLogger(Config.class.getName());

    private List<String> targets = new ArrayList<String>();

    // 代理服务器
    List<ProxyServer> servers = new ArrayList<ProxyServer>();

    //是否需要延时启动访问
    private boolean delay = false;
    //最小延时毫秒数
    private int minDelay = 0;
    //最大延时毫秒数
    private int maxDelay = 0;

    public static Config get() throws IOException {
        config.load();
        config.init();
        return config;
    }

    public static Config get(String path) throws IOException {
        config.load(path);
        config.init();
        return config;
    }

    private Config() {

    }

    private void load() throws IOException {
        load("/com/kevin/resources/conf.properties");
    }

    private void load(String path) throws IOException {
        InputStream in;
        in = getClass().getResourceAsStream(path);
        properties = new Properties();
        properties.load(in);
    }

    private void init() throws IOException {
        String targetStr = properties.getProperty("proxyhits.target", "");
        if (targetStr.length() == 0) {
            System.out.println("没有设置代理访问的目标url,如需设置多个请以空格分隔");
            return;
        }
        targets = Arrays.asList(targetStr.trim().split("\\s+"));
        try {
            delay = Boolean.parseBoolean(properties.getProperty("proxyhits.random.delay", "false"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (delay) {
            try {
                minDelay = Integer.parseInt(properties.getProperty("proxyhits.random.min", "0"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                maxDelay = Integer.parseInt(properties.getProperty("proxyhits.random.max", "0"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        URI uri = null;
        try {
            uri = this.getClass().getResource("/com/kevin/resources/servers").toURI();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        /**
         * 通过nio的Files工具类直接行级读取servers文件内容
         */
        List<String> serversFile = Files.readAllLines(Paths.get(uri), Charset.defaultCharset());

        if (serversFile == null || serversFile.isEmpty()) {
            System.out.println("servers配置文件下的代理服务器列表为空,请配置代理服务器列表");
            return;
        }
        for (String s : serversFile) {
            try {
                s = s.trim();

                // `#`开头的为注释
                if (s.startsWith("#")) {
                    continue;
                }

                String ip[] = s.split(":");
                if (ip.length == 2) {
                    if (InetAddressUtils.isIPv4Address(ip[0])) {
                        servers.add(new ProxyServer(ip[0], Integer.parseInt(ip[1])));
                    } else {
                        log.warning(ip[0] + "不是标准的IPv4地址");
                    }
                } else if (ip.length == 1) {
                    //验证IP地址格式
                    if (InetAddressUtils.isIPv4Address(ip[0])) {
                        servers.add(new ProxyServer(ip[0], 80));
                    } else {
                        log.warning(ip[0] + "不是标准的IPv4地址");
                    }
                } else {
                    log.warning(ip[0] + "不是标准的IPv4地址");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<ProxyServer> getServers() {
        return servers;
    }

    public void setServers(List<ProxyServer> servers) {
        this.servers = servers;
    }

    public List<String> getTargets() {
        return targets;
    }

    public boolean isDelay() {
        return delay;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public void setDelay(boolean delay) {
        this.delay = delay;
    }

    public void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }
}
