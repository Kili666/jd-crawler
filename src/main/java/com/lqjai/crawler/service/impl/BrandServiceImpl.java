package com.lqjai.crawler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.mapper.BrandMapper;
import com.lqjai.crawler.pojo.Brand;
import com.lqjai.crawler.service.BrandService;
import org.springframework.stereotype.Service;

import java.util.List;

/****
 * @Author: com.lqjai
 * @Description: BrandServiceImpl
 * @Date 2022-02-12 02:43:46
 *****/
@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService{

    /**
     * Brand条件+分页查询
     * @param brand 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult findPage(Brand brand, int pageNo, int size){
        Page<Brand> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page, new QueryWrapper<>(brand));
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }

    @Override
    public List<Brand> findListByCategoryName(String categoryName) {
        return baseMapper.findListByCategoryName(categoryName);
    }

    @Override
    public Integer getBrandId(String brandName) {
        if(StringUtils.isEmpty(brandName)) return -1;
        Brand brand = getOne(Wrappers.<Brand>lambdaQuery().eq(Brand::getName, brandName));
        if(brand != null) return brand.getId();
        else {
            Brand bd = new Brand().setName(brandName);
            save(bd);
            return bd.getId();
        }
    }
}
