package com.lqjai.crawler.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.pojo.Category;

import java.util.List;

/****
 * @Author: com.lqjai
 * @Description: CategoryService
 * @Date 2022-02-12 02:43:46
 *****/
public interface CategoryService extends IService<Category> {

    /**
     * Category条件+分页查询
     * @param category 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    PageResult findPage(Category category, int pageNo, int size);


    List<Integer> getCids(List<String> categories);

}
