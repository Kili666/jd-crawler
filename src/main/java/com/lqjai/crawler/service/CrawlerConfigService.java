package com.lqjai.crawler.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.pojo.CrawlerConfig;

/****
 * @Author: Kili
 * @Description: CrawlerConfigService
 * @Date 2022-04-24 01:44:59
 *****/
public interface CrawlerConfigService extends IService<CrawlerConfig> {

    /**
     * CrawlerConfig条件+分页查询
     * @param crawlerConfig 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    PageResult findPage(CrawlerConfig crawlerConfig, int pageNo, int size);

    PageResult findPage(int pageNo, int size);
    
    Integer decPage(Integer id);


}
