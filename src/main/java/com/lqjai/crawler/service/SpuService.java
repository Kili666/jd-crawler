package com.lqjai.crawler.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.pojo.Spu;

/****
 * @Author: com.lqjai
 * @Description: SpuService
 * @Date 2022-02-12 02:43:47
 *****/
public interface SpuService extends IService<Spu> {

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    PageResult findPage(Spu spu, int pageNo, int size);


}
