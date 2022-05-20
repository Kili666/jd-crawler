package com.lqjai.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lqj
 * @version 1.0 2020/11/30
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mall.config")
public class ConfigProperties {

    /**
     * 账户token有效期：默认1440*60*7分钟（7天）
     */
    private Long customer_token_expire = 241920L;
    /**
     * 账户有效期（180秒)
     */
    private Long nonce_str_expire = 180L;
    /**
     * 账户token有效期：默认1440*60分钟（1天）
     */
    private Long account_token_expire = 34560L;

    /**
     * 默认1440*60*7分钟（7天）
     */
    private Long account_rememberme_token_expire = 241920L;
    /**
     * 域名
     */
    private String domain = "ishoptop.com";

    /**
     * 60秒内不能重复
     */
    private Long mobile_frequency = 60l;
    /**
     * 1天限制10次
     */
    private Integer days_moblie_limit = 10;
    /**
     * 60秒内不能重复
     */
    private Long email_frequency = 60l;
    /**
     * 一天限制10次
     */
    private Integer days_email_limit = 10;

    /**
     * 内部店铺列表
     */
    private List<Integer> innerShopList = new ArrayList<>();
}

