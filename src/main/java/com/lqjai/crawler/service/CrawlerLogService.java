package com.lqjai.crawler.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.pojo.CrawlerLog;

/****
 * @Author: Kili
 * @Description: CrawlerLogService
 * @Date 2022-04-25 15:07:57
 *****/
public interface CrawlerLogService extends IService<CrawlerLog> {

    /**
     * CrawlerLog条件+分页查询
     * @param crawlerLog 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    PageResult findPage(CrawlerLog crawlerLog, int pageNo, int size);

    PageResult findPage(int pageNo, int size);

}
