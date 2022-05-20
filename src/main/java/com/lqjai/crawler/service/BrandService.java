package com.lqjai.crawler.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.pojo.Brand;

import java.util.List;

/****
 * @Author: com.lqjai
 * @Description: BrandService
 * @Date 2022-02-12 02:43:46
 *****/
public interface BrandService extends IService<Brand> {
    /**
     * 根据商品分类名称查询规格列表
     * @param categoryName
     * @return
     */
    public List<Brand> findListByCategoryName(String categoryName);

    /**
     * Brand条件+分页查询
     * @param brand 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    PageResult findPage(Brand brand, int pageNo, int size);

    public Integer getBrandId(String brand);

}
