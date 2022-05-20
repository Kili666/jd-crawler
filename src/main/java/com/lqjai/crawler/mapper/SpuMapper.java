package com.lqjai.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqjai.crawler.pojo.Spu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/****
 * @Author: Kili
 * @Description: SpuDao
 * @Date 2022-04-24 15:21:55
 *****/
public interface SpuMapper extends BaseMapper<Spu> {
    /**
     * 根据id查询已经逻辑删除的数据
     */
    @Select("select * from tb_spu where id = #{id} and deleted=1")
    public Spu findLogicDelById(Long id);

    /**
     * 查询所有已经逻辑删除的数据
     */
    @Select("select * from tb_spu where deleted=1")
    public List<Spu> findAllLogicDel();

    /**
     * 根据id物理删除数据
     */
    @Delete("delete from tb_spu where id=#{id}")
    public Boolean deleteTablelogic(Long id);

    /**
     * 根据id更新逻辑删除的数据
     * 复杂sql语句写在xml里面
     * @param spu
     */
    public Boolean updateLogicDelById(Spu spu);

}
