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
 * @Description: CategoryPojo
 * @Date 2022-02-12 02:25:36
 *****/
@ApiModel(description = "Category",value = "Category")
@TableName(value="tb_category")
@Data
@Accessors(chain = true)
public class Category implements Serializable{
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "分类ID",required = true)
	@TableId(type = IdType.AUTO)
	private Integer id;	//分类ID

	@ApiModelProperty(value = "分类名称",required = false)
	private String name;	//分类名称

	@ApiModelProperty(value = "商品数量",required = false)
	private Integer goodsNum;	//商品数量

	@ApiModelProperty(value = "是否显示",required = false)
	private String isShow;	//是否显示

	@ApiModelProperty(value = "是否导航",required = false)
	private String isMenu;	//是否导航

	@ApiModelProperty(value = "排序",required = false)
	private Integer seq;	//排序

	@ApiModelProperty(value = "上级ID",required = false)
	private Integer parentId;	//上级ID

	@ApiModelProperty(value = "模板ID",required = false)
	private Integer templateId;	//模板ID

}
