package com.lqjai.crawler.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/****
 * @Author: Kili
 * @Description: CrawlerLogPojo
 * @Date 2022-04-25 15:18:53
 *****/
@ApiModel(description = "CrawlerLog",value = "CrawlerLog")
@TableName(value="tb_crawler_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CrawlerLog implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "主键id",required = true)
	@TableId
	private Integer id;	//主键id

	@ApiModelProperty(value = "爬取的url地址",required = false)
	private String url;	//爬取的url地址

	@ApiModelProperty(value = "创建时间",required = false)
	private LocalDateTime createTime;	//创建时间

	@ApiModelProperty(value = "更新时间",required = false)
	private LocalDateTime updateTiime;	//更新时间

	@ApiModelProperty(value = "0-定时任务，1-crawlerItem, 2-crawList",required = false)
	private Integer type;	//0-定时任务，1-crawlerItem, 2-crawList

	@ApiModelProperty(value = "爬取结果",required = false)
	private String msg;	//爬取结果

	@ApiModelProperty(value = "操作者",required = false)
	private String operator;	//操作者

}
