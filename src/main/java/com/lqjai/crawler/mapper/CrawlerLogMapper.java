package com.lqjai.crawler.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqjai.crawler.pojo.CrawlerLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/****
 * @Author: Kili
 * @Description: CrawlerLogDao
 * @Date 2022-04-25 15:07:57
 *****/
public interface CrawlerLogMapper extends BaseMapper<CrawlerLog> {
    /**
     * 根据id查询已经逻辑删除的数据
     */
    @Select("select * from tb_crawler_log where id = #{id} and deleted=1")
    public CrawlerLog findLogicDelById(Long id);

    /**
     * 查询所有已经逻辑删除的数据
     */
    @Select("select * from tb_crawler_log where deleted=1")
    public List<CrawlerLog> findAllLogicDel();

    /**
     * 根据id物理删除数据
     */
    @Delete("delete from tb_crawler_log where id=#{id}")
    public Boolean deleteTablelogic(Long id);

    /**
     * 根据id更新逻辑删除的数据
     * 复杂sql语句写在xml里面
     * @param crawlerLog
     */
    public Boolean updateLogicDelById(CrawlerLog crawlerLog);

}
