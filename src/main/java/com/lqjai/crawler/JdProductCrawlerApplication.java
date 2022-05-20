package com.lqjai.crawler;

import com.lqjai.common.handler.BaseExceptionHandler;
import com.lqjai.common.utils.IdWorker;
import com.lqjai.common.utils.QiniuUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
//使用定时任务，需要先开启定时任务，需要添加注解
@EnableScheduling
@EnableAsync
@MapperScan("com.lqjai.crawler.mapper")
public class JdProductCrawlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdProductCrawlerApplication.class, args);
    }

    @Bean
    public BaseExceptionHandler baseExceptionHandler(){
        return new BaseExceptionHandler();
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public QiniuUtils qiniuUtils(){
        return new QiniuUtils();
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

}
