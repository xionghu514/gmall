package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品三级分类
 * 
 * @author Guan FuQing
 * @email moumouguan@gmail.com
 * @date 2022-12-08 02:02:43
 */
@Data
@TableName("pms_category")
public class CategoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分类id
	 */
	@TableId
	private Long id;
	/**
	 * 分类名称
	 */
	private String name;
	/**
	 * 父分类id
	 */
	private Long parentId;
	/**
	 * 是否显示[0-不显示，1显示]
	 */
	private Integer status;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 图标地址
	 */
	private String icon;
	/**
	 * 计量单位
	 */
	private String unit;

	/**
	 * 扩展字段 不参与 mp 的 sql 语句生成, 默认一个 实体类 所有属性都为 某一个张表的 所有字段
	 * 		@TableField(exist = false) 该注解表示 此字段不属于 mysql 列
	 */
	@TableField(exist = false)
	private List<CategoryEntity> subs;
}
