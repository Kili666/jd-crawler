package com.lqjai.crawler.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqjai.common.utils.PageResult;
import com.lqjai.crawler.mapper.CategoryMapper;
import com.lqjai.crawler.pojo.Category;
import com.lqjai.crawler.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/****
 * @Author: com.lqjai
 * @Description: CategoryServiceImpl
 * @Date 2022-02-12 02:43:46
 *****/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    /**
     * Category条件+分页查询
     * @param category 查询条件
     * @param pageNo 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageResult findPage(Category category, int pageNo, int size){
        Page<Category> page = new Page<>();
        page.setCurrent(pageNo).setSize(size);
        IPage result = this.page(page, new QueryWrapper<>(category));
        PageResult pageResult=new PageResult(result.getTotal(),result.getRecords());
        return pageResult;
    }

    /**
     * 根据id更新逻辑删除的数据
     * 复杂sql语句写在xml里面
     * @param category
     */
    public Boolean updateLogicDelById(Category category){
        return baseMapper.updateLogicDelById(category);
    };

    @Override
    public List<Integer> getCids(List<String> categories) {
        List<Integer> cids = new ArrayList<>();
        List<Category> c0 = list(Wrappers.<Category>lambdaQuery().eq(Category::getName,categories.get(0)));
        List<Category> c1 = list(Wrappers.<Category>lambdaQuery().eq(Category::getName,categories.get(1)));
        List<Category> c2 = list(Wrappers.<Category>lambdaQuery().eq(Category::getName,categories.get(2)));
        Category ac0 = null;
        Category ac1 = null;
        Category ac2 = null;
        if(CollectionUtils.isNotEmpty(c0)){
            cids.add(c0.get(0).getId());
        }else {
            ac0 = new Category().setName(categories.get(0)).setParentId(-1);
            save(ac0);
            cids.add(ac0.getId());
        }

        if(CollectionUtils.isNotEmpty(c1)){
            cids.add(c1.get(0).getId());
        }else {
            ac1 = new Category().setName(categories.get(1)).setParentId(CollectionUtils.isNotEmpty(c0)?c0.get(0).getId():ac0.getId());
            save(ac1);
            cids.add(ac1.getId());
        }

        if(CollectionUtils.isNotEmpty(c2)){
            cids.add(c2.get(0).getId());
        }else {
            ac2= new Category().setName(categories.get(2)).setParentId(CollectionUtils.isNotEmpty(c1)?c1.get(0).getId():ac1.getId());
            save(ac2);
            cids.add(ac2.getId());
        }
        return cids;
    }
}
