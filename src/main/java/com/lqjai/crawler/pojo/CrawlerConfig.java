package com.lqjai.crawler.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/****
 * @Author: Kili
 * @Description: CrawlerConfigPojo
 * @Date 2022-04-24 01:44:59
 *****/
@ApiModel(description = "CrawlerConfig",value = "CrawlerConfig")
@TableName(value="tb_crawler_config")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CrawlerConfig implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "",required = true)
	@TableId
	private Integer id;	//

	@ApiModelProperty(value = "搜索url",required = false)
	private String searchUrl;	//搜索url

	@TableField(update = "")
	@ApiModelProperty(value = "cookies值",required = false)
	private String cookies;	//cookies值

	@ApiModelProperty(value = "页数",required = false)
	private Integer page;	//页数

	@ApiModelProperty(value = "开始状态，默认0",required = false)
	private Integer openStatus;	//开始状态，默认0

	@ApiModelProperty(value = "ip主机",required = false)
	private String ip;	//ip主机

}
