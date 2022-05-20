package com.lqjai.crawler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.mapper.CrawlerLogMapper;
import com.lqjai.crawler.pojo.CrawlerLog;
import com.lqjai.crawler.service.CrawlerLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/****
 * @Author: Kili
 * @Description: CrawlerLogServiceImpl
 * @Date 2022-04-25 15:07:57
 *****/
@Service
@Slf4j
public class CrawlerLogServiceImpl extends ServiceImpl<CrawlerLogMapper, CrawlerLog> implements CrawlerLogService{

    /**
     * CrawlerLog条件+分页查询
     * @param crawlerLog 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult findPage(CrawlerLog crawlerLog, int pageNo, int size){
        Page<CrawlerLog> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        LambdaQueryWrapper<CrawlerLog> wrapper = Wrappers.lambdaQuery(crawlerLog).orderByDesc(CrawlerLog::getCreateTime);
        IPage result = this.page(page, wrapper);
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }

    @Override
    public PageResult findPage(int pageNo, int size){
        Page<CrawlerLog> page = new Page<>();
        LambdaQueryWrapper<CrawlerLog> wrapper = Wrappers.<CrawlerLog>lambdaQuery().orderByDesc(CrawlerLog::getCreateTime);//根据某个字段排序，自己根据实际情况小改一下
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page, wrapper);
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }
}
