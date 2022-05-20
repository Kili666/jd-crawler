package com.lqjai.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqjai.crawler.pojo.CrawlerConfig;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/****
 * @Author: Kili
 * @Description: CrawlerConfigDao
 * @Date 2022-04-24 01:44:59
 *****/
public interface CrawlerConfigMapper extends BaseMapper<CrawlerConfig> {
    /**
     * 根据id查询已经逻辑删除的数据
     */
    @Select("select * from tb_crawler_config where id = #{id} and deleted=1")
    public CrawlerConfig findLogicDelById(Long id);

    /**
     * 查询所有已经逻辑删除的数据
     */
    @Select("select * from tb_crawler_config where deleted=1")
    public List<CrawlerConfig> findAllLogicDel();

    /**
     * 根据id物理删除数据
     */
    @Delete("delete from tb_crawler_config where id=#{id}")
    public Boolean deleteTablelogic(Long id);

}
