package com.lqjai.crawler.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.pojo.Sku;

/****
 * @Author: com.lqjai
 * @Description: SkuService
 * @Date 2022-02-12 02:43:47
 *****/
public interface SkuService extends IService<Sku> {

    /**
     * Sku条件+分页查询
     * @param sku 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    PageResult findPage(Sku sku, int pageNo, int size);

    PageResult findPage(int pageNo, int size);

}
