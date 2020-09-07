package com.i5018.shiro.autoconfigure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * @author limk
 * @date 2020/8/26 9:28
 */
@Data
@ConfigurationProperties(prefix = "shiro")
public class ShiroProperties {

    /**
     * 项目标识
     */
    private String namespace = "i5018";

    private final Annotations annotations = new Annotations();
    private final Credential credential = new Credential();
    private final Session session = new Session();
    private final Cookie cookie = new Cookie();
    private final Filter filter = new Filter();
    private final KickOut kickOut = new KickOut();

    @Data
    public static class Annotations {

        private boolean enabled = true;

    }

    @Data
    public static class Credential {

        /**
         * hash算法
         */
        private String hashAlgorithm = "md5";

        /**
         * 迭代次数
         */
        private int hashIterations = 1;

        /**
         * 是否为十六进制编码
         */
        private boolean storedCredentialsHexEncoded = true;

        /**
         * 密码错误次数
         */
        private int errorsCount = 5;

        /**
         * 缓存时长
         */
        private Duration expiration = Duration.ofSeconds(7200);
    }

    @Data
    public static class Session {

        /**
         * session缓存有效期（毫秒）
         */
        private long sessionExpire = 1800000;

        /**
         * 扫描session缓存时间间隔（毫秒）
         */
        private long sessionValidationInterval = 1800000;
    }

    @Data
    public static class Cookie {
        private String cookieName = "cn-i5018";
        private int maxAge = -1;
        private String domain;
    }

    @Data
    public static class Filter {
        private String loginUrl = "login";
        private String successUrl = "/";
        private List<String> anon = Collections.emptyList();
        private List<String> authc = Collections.emptyList();
    }

    @Data
    public static class KickOut {
        private String kickOutUrl = "/login";
        private boolean kickOutAfter = false;
        private int maxSession = 1;
    }

}
