package com.lqjai.crawler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.mapper.SpuMapper;
import com.lqjai.crawler.pojo.Spu;
import com.lqjai.crawler.service.BrandService;
import com.lqjai.crawler.service.CategoryService;
import com.lqjai.crawler.service.SkuService;
import com.lqjai.crawler.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/****
 * @Author: com.lqjai
 * @Description: SpuServiceImpl
 * @Date 2022-02-12 02:43:47
 *****/
@Service
public class SpuServiceImpl extends ServiceImpl<SpuMapper, Spu> implements SpuService {

    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult findPage(Spu spu, int pageNo, int size){
        Page<Spu> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page, new QueryWrapper<>(spu));
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }
}
