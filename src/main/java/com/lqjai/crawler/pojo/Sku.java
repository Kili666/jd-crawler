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
 * @Description: SkuPojo
 * @Date 2022-04-24 17:05:11
 *****/
@ApiModel(description = "Sku",value = "Sku")
@TableName(value="tb_sku")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Sku implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "商品id",required = true)
	@TableId
	private String id;	//商品id

	@ApiModelProperty(value = "商品条码",required = true)
	private String sn;	//商品条码

	@ApiModelProperty(value = "SKU名称",required = true)
	private String name;	//SKU名称

	@ApiModelProperty(value = "价格（分）",required = true)
	private Integer price;	//价格（分）

	@ApiModelProperty(value = "库存数量",required = true)
	private Integer num;	//库存数量

	@ApiModelProperty(value = "库存预警数量",required = false)
	private Integer alertNum;	//库存预警数量

	@ApiModelProperty(value = "商品图片",required = false)
	private String image;	//商品图片

	@ApiModelProperty(value = "商品图片列表",required = false)
	private String images;	//商品图片列表

	@ApiModelProperty(value = "重量（克）",required = false)
	private Integer weight;	//重量（克）

	@ApiModelProperty(value = "创建时间",required = false)
	private LocalDateTime createTime;	//创建时间

	@ApiModelProperty(value = "更新时间",required = false)
	private LocalDateTime updateTime;	//更新时间

	@ApiModelProperty(value = "SPUID",required = false)
	private String spuId;	//SPUID

	@ApiModelProperty(value = "类目ID",required = false)
	private Integer categoryId;	//类目ID

	@ApiModelProperty(value = "类目名称",required = false)
	private String categoryName;	//类目名称

	@ApiModelProperty(value = "品牌名称",required = false)
	private String brandName;	//品牌名称

	@ApiModelProperty(value = "规格",required = false)
	private String spec;	//规格

	@ApiModelProperty(value = "销量",required = false)
	private Integer saleNum;	//销量

	@ApiModelProperty(value = "图片状态：0-默认（未知），1-有效，2-无效",required = false)
	private Integer pstatus;	//图片状态：0-默认（未知），1-有效，2-无效

	@ApiModelProperty(value = "评论数",required = false)
	private Integer commentNum;	//评论数

	@ApiModelProperty(value = "商品状态 1-正常，2-下架，3-删除",required = false)
	private String status;	//商品状态 1-正常，2-下架，3-删除

	@ApiModelProperty(value = "",required = false)
	private Integer version;	//

	@ApiModelProperty(value = "实付金额",required = false)
	private Integer paymoney;	//实付金额

}
