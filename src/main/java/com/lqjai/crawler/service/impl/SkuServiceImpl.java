package com.lqjai.crawler.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.mapper.SkuMapper;
import com.lqjai.crawler.pojo.Sku;
import com.lqjai.crawler.service.SkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/****
 * @Author: com.lqjai
 * @Description: SkuServiceImpl
 * @Date 2022-02-12 02:43:47
 *****/
@Service
@Slf4j
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService{

    // 任务执行器
    private ExecutorService executorService;

    // 初始化线程池
    @PostConstruct
    private void initThreadPool() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(5, 5, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.DiscardPolicy());
        }
    }

    /**
     * Sku条件+分页查询
     * @param sku 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult findPage(Sku sku, int pageNo, int size){
        Page<Sku> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page, Wrappers.lambdaQuery(sku).orderByDesc(Sku::getCreateTime));
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }

    @Override
    public PageResult findPage(int pageNo, int size){
        Page<Sku> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page,  Wrappers.<Sku>lambdaQuery().orderByDesc(Sku::getCreateTime));
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }
}
