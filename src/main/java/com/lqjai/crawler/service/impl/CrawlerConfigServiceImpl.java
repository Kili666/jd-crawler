package com.lqjai.crawler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.mapper.CrawlerConfigMapper;
import com.lqjai.crawler.pojo.CrawlerConfig;
import com.lqjai.crawler.service.CrawlerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/****
 * @Author: Kili
 * @Description: CrawlerConfigServiceImpl
 * @Date 2022-04-24 01:44:59
 *****/
@Service
@Slf4j
public class CrawlerConfigServiceImpl extends ServiceImpl<CrawlerConfigMapper, CrawlerConfig> implements CrawlerConfigService{

    /**
     * CrawlerConfig条件+分页查询
     * @param crawlerConfig 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult findPage(CrawlerConfig crawlerConfig, int pageNo, int size){
        Page<CrawlerConfig> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        LambdaQueryWrapper<CrawlerConfig> wrapper = Wrappers.lambdaQuery(crawlerConfig);
        IPage result = this.page(page, wrapper);
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }

    @Override
    public PageResult findPage(int pageNo, int size){
        Page<CrawlerConfig> page = new Page<>();
        LambdaQueryWrapper<CrawlerConfig> wrapper = Wrappers.<CrawlerConfig>lambdaQuery();//根据某个字段排序，自己根据实际情况小改一下
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page, wrapper);
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }

    @Override
    public Integer decPage(Integer id) {
        CrawlerConfig crawlerConfig = getById(id);
        updateById(crawlerConfig.setPage(crawlerConfig.getPage()-1));
        return crawlerConfig.getPage();
    }

}
