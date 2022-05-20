package com.lqjai.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqjai.crawler.pojo.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/****
 * @Author: com.lqjai
 * @Description: CategoryDao
 * @Date 2022-02-12 02:43:46
 *****/
public interface CategoryMapper extends BaseMapper<Category> {
    /**
     * 根据id查询已经逻辑删除的数据
     */
    @Select("select * from tb_category where id = #{id} and deleted=1")
    public Category findLogicDelById(Long id);

    /**
     * 查询所有已经逻辑删除的数据
     */
    @Select("select * from tb_category where deleted=1")
    public List<Category> findAllLogicDel();

    /**
     * 根据id物理删除数据
     */
    @Delete("delete from tb_category where id=#{id}")
    public Boolean deleteTablelogic(Long id);

    /**
     * 根据id更新逻辑删除的数据
     * 复杂sql语句写在xml里面
     * @param category
     */
    public Boolean updateLogicDelById(Category category);

}
