package com.lqjai.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqjai.crawler.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/****
 * @Author: com.lqjai
 * @Description: BrandDao
 * @Date 2022-02-12 02:43:46
 *****/
public interface BrandMapper extends BaseMapper<Brand> {
    /**
     * 根据id查询已经逻辑删除的数据
     */
    @Select("select * from tb_brand where id = #{id} and deleted=1")
    public Brand findLogicDelById(Long id);

    /**
     * 查询所有已经逻辑删除的数据
     */
    @Select("select * from tb_brand where deleted=1")
    public List<Brand> findAllLogicDel();

    /**
     * 根据id物理删除数据
     */
    @Delete("delete from tb_brand where id=#{id}")
    public Boolean deleteTablelogic(Long id);

    /**
     * 根据id更新逻辑删除的数据
     * 复杂sql语句写在xml里面
     * @param brand
     */
    public Boolean updateLogicDelById(Brand brand);

    /**
     * 根据分类名称查询品牌列表
     * @param categoryName
     * @return
     */
    @Select("SELECT name,image FROM tb_brand WHERE id  IN (SELECT brand_id FROM tb_category_brand WHERE  category_id IN (SELECT id FROM tb_category WHERE NAME=#{name}) )order by seq")
    public List<Brand> findListByCategoryName(@Param("name") String categoryName);

}
