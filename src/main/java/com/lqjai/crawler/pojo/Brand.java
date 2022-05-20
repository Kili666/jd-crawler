package com.lqjai.crawler.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/****
 * @Author: com.lqjai
 * @Description: BrandPojo
 * @Date 2022-02-12 02:25:36
 *****/
@ApiModel(description = "Brand",value = "Brand")
@TableName(value="tb_brand")
@Data
@Accessors(chain = true)
public class Brand implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "品牌id",required = true)
	@TableId(type = IdType.AUTO)
	private Integer id;	//品牌id

	@ApiModelProperty(value = "品牌名称",required = true)
	private String name;	//品牌名称

	@ApiModelProperty(value = "品牌图片地址",required = false)
	private String image;	//品牌图片地址

	@ApiModelProperty(value = "品牌的首字母",required = false)
	private String letter;	//品牌的首字母

	@ApiModelProperty(value = "排序",required = false)
	private Integer seq;	//排序

}
