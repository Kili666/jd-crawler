package com.lqjai.crawler.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
 * @Description: SpuPojo
 * @Date 2022-04-24 15:21:54
 *****/
@ApiModel(description = "Spu",value = "Spu")
@TableName(value="tb_spu")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Spu implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "主键",required = true)
	@TableId
	private String id;	//主键

	@ApiModelProperty(value = "货号",required = false)
	private String sn;	//货号

	@ApiModelProperty(value = "SPU名",required = false)
	private String name;	//SPU名

	@ApiModelProperty(value = "副标题",required = false)
	private String caption;	//副标题

	@ApiModelProperty(value = "品牌ID",required = false)
	private Integer brandId;	//品牌ID

	@ApiModelProperty(value = "一级分类",required = false)
	private Integer category1Id;	//一级分类

	@ApiModelProperty(value = "二级分类",required = false)
	private Integer category2Id;	//二级分类

	@ApiModelProperty(value = "三级分类",required = false)
	private Integer category3Id;	//三级分类

	@ApiModelProperty(value = "模板ID",required = false)
	private Integer templateId;	//模板ID

	@ApiModelProperty(value = "运费模板id",required = false)
	private Integer freightId;	//运费模板id

	@ApiModelProperty(value = "图片",required = false)
	private String image;	//图片

	@ApiModelProperty(value = "图片列表",required = false)
	private String images;	//图片列表

	@ApiModelProperty(value = "图片状态：0-默认（未知），1-有效，2-无效",required = false)
	private Integer pstatus;	//图片状态：0-默认（未知），1-有效，2-无效

	@ApiModelProperty(value = "售后服务",required = false)
	private String saleService;	//售后服务

	@ApiModelProperty(value = "介绍",required = false)
	private String introduction;	//介绍

	@ApiModelProperty(value = "规格列表",required = false)
	private String specItems;	//规格列表

	@ApiModelProperty(value = "参数列表",required = false)
	private String paraItems;	//参数列表

	@ApiModelProperty(value = "销量",required = false)
	private Integer saleNum;	//销量

	@ApiModelProperty(value = "评论数",required = false)
	private Integer commentNum;	//评论数

	@ApiModelProperty(value = "是否上架",required = false)
	private String isMarketable;	//是否上架

	@ApiModelProperty(value = "是否启用规格",required = false)
	private String isEnableSpec;	//是否启用规格

	@ApiModelProperty(value = "是否删除",required = false)
	@TableLogic
	private String deleted;	//是否删除

	@ApiModelProperty(value = "审核状态",required = false)
	private String status;	//审核状态

	@ApiModelProperty(value = "原价",required = false)
	private Integer oprice;	//原价

	@ApiModelProperty(value = "现价",required = false)
	private Integer sprice;	//现价

	@ApiModelProperty(value = "创建时间",required = false)
	private LocalDateTime createTime;	//创建时间

	@ApiModelProperty(value = "更新时间",required = false)
	private LocalDateTime updateTime;	//更新时间

}
