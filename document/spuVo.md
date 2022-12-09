## 封装大保存对应的接受对象

```json
{
  "name": "尚硅谷手机",
  "brandId": 1,
  "categoryId": 225,
  "publishStatus": 1,
  "spuImages": [
    "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/12cb51b4-f524-42d8-85c8-a2f9928cfdde_b094601548ddcb1b.jpg",
    "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/8ff3d891-b434-4865-8bd4-8546c455b398_d511faab82abb34b.jpg"
  ],
  "baseAttrs": [
    {
      "attrId": 1,
      "attrName": "上市年份",
      "valueSelected": [
        "2018"
      ]
    },
    {
      "attrId": 2,
      "attrName": "产品名称",
      "valueSelected": [
        "HUAWEI Mate 30"
      ]
    },
    {
      "attrId": 10,
      "attrName": "上市月份",
      "valueSelected": [
        "二月"
      ]
    },
    {
      "attrId": 6,
      "attrName": "CPU品牌",
      "valueSelected": [
        "骁龙"
      ]
    },
    {
      "attrId": 7,
      "attrName": "CPU型号",
      "valueSelected": [
        "骁龙865"
      ]
    },
    {
      "attrId": 8,
      "attrName": "分辨率",
      "valueSelected": [
        "2340*1080"
      ]
    },
    {
      "attrId": 9,
      "attrName": "屏幕尺寸",
      "valueSelected": [
        "7"
      ]
    }
  ],
  "skus": [
    {
      "attr_3": "黑色",
      "name_3": "机身颜色",
      "price": "4200",
      "stock": 0,
      "growBounds": "4000",
      "buyBounds": "2000",
      "work": [
        1,
        0,
        1,
        0
      ],
      "fullCount": 1,
      "discount": "80",
      "fullPrice": "3000",
      "reducePrice": "2000",
      "fullAddOther": 1,
      "images": [
        "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/d9f78362-cc4b-498e-8403-3360b4fabb2f_0d40c24b264aa511.jpg",
        "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/4ce32f4d-81f1-45ec-81a5-f011f15a795c_1f15cdbcf9e1273c.jpg",
        "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/32db630c-79ab-4c11-8596-aec13b0fd47b_28f296629cca865e.jpg"
      ],
      "name": "尚硅谷手机 黑色,8G,256G",
      "title": "尚硅谷手机 一亿像素三摄 全网通手机 陶瓷黑 黑色,8G,256G",
      "subTitle": "尚硅谷手机 黑色,8G,256G",
      "weight": "500",
      "attr_4": "8G",
      "name_4": "运行内存",
      "attr_5": "256G",
      "name_5": "机身存储",
      "ladderAddOther": 1,
      "subtitle": "屏下摄像头+CUP全面屏+120W双档秒充【点击抢购尚硅谷手机】",
      "saleAttrs": [
        {
          "attrId": "3",
          "attrName": "机身颜色",
          "attrValue": "黑色"
        },
        {
          "attrId": "4",
          "attrName": "运行内存",
          "attrValue": "8G"
        },
        {
          "attrId": "5",
          "attrName": "机身存储",
          "attrValue": "256G"
        }
      ]
    },
    {
      "attr_3": "黑色",
      "name_3": "机身颜色",
      "price": "5200",
      "stock": 0,
      "growBounds": "5000",
      "buyBounds": "2000",
      "work": [
        0,
        1,
        1,
        0
      ],
      "fullCount": 1,
      "discount": "90",
      "fullPrice": "2000",
      "reducePrice": "1000",
      "fullAddOther": 1,
      "images": [
        "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/a5c86456-ac85-48ad-89f8-19b7b39161e4_1f15cdbcf9e1273c.jpg",
        "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/d9cb7886-a836-47a4-83c0-e00f9fef38e5_0d40c24b264aa511.jpg",
        "https://yiki-gmall.oss-cn-hangzhou.aliyuncs.com/2022-10-29/53dfe1b3-e2d8-46ee-8130-95aaef45de59_28f296629cca865e.jpg"
      ],
      "name": "尚硅谷手机 黑色,8G,512G",
      "title": "尚硅谷手机 一亿像素三摄 全网通手机 黑色,8G,512G",
      "subTitle": "尚硅谷手机 黑色,8G,512G",
      "weight": "500",
      "attr_4": "8G",
      "name_4": "运行内存",
      "attr_5": "512G",
      "name_5": "机身存储",
      "ladderAddOther": 1,
      "subtitle": "屏下摄像头+CUP全面屏+120W双档秒充【点击抢购尚硅谷手机】",
      "saleAttrs": [
        {
          "attrId": "3",
          "attrName": "机身颜色",
          "attrValue": "黑色"
        },
        {
          "attrId": "4",
          "attrName": "运行内存",
          "attrValue": "8G"
        },
        {
          "attrId": "5",
          "attrName": "机身存储",
          "attrValue": "512G"
        }
      ]
    }
  ]
}
```

![img.png](https://oss.yiki.tech/gmall/202211210414888.png)

```java
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<?> spuImages;

    // 基本属性信息
    private List<?> baseAttrs;

    // sku 信息
    private List<?> skus;

}
```

![img_1.png](https://oss.yiki.tech/gmall/202211210414383.png)

```java
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<?> baseAttrs;

    // sku信息
    private List<?> skus;

}
```

![img_2.png](https://oss.yiki.tech/gmall/202211210415268.png)

```java
@Data
public class SpuAttrValueVo extends SpuAttrValueEntity {
//    private List<String> valueSelected;
//
//    public void setValueSelected(List<String> valueSelected) {
//
//        // 如果接受的集合为空, 则不设置
//        if (CollectionUtils.isEmpty(valueSelected))
//            return;
//
//        this.valueSelected = valueSelected;
//    }

    public void setValueSelected(List<String> valueSelected) {
        // 如果接受的集合为空, 则不设置
        if (CollectionUtils.isEmpty(valueSelected)) {
            return;
        }

        // 将接受的集合根据 "," 分割为字符串 赋值给 AttrValue 属性
        this.setAttrValue(StringUtils.join(valueSelected, ","));
    }
}
```

```java
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<SpuAttrValueVo> baseAttrs;

    // sku信息
    private List<?> skus;

}
```

![img_3.png](https://oss.yiki.tech/gmall/202211210415006.png)

```java
@Data
public class SkuVo extends SkuEntity {
    
}
```

```java
@Data
public class SpuVo extends SpuEntity {

    // 图片信息
    private List<String> spuImages;

    // 基本属性信息
    private List<SpuAttrValueVo> baseAttrs;

    // sku信息
    private List<SkuVo> skus;

}
```

![img_4.png](https://oss.yiki.tech/gmall/202211210415365.png)


```java
@Data
public class SkuVo extends SkuEntity {

    // sku 的 图片列表
    private List<String> images;
    
}
```

![img_5.png](https://oss.yiki.tech/gmall/202211210415959.png)

```java
@Data
public class SkuVo extends SkuEntity {

    // sku 的 图片列表
    private List<String> images;

    // 销售属性
    private List<SkuAttrValueEntity> saleAttrs;

}
```

![img_6.png](https://oss.yiki.tech/gmall/202211210415619.png)

```java
@Data
public class SkuVo extends SkuEntity {

    // sku 的 图片列表
    private List<String> images;

    // 销售属性
    private List<SkuAttrValueEntity> saleAttrs;

    /*                       积分优惠信息                       */

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
//    private Integer work;
    private List<Integer> work;

}
```

![img_7.png](https://oss.yiki.tech/gmall/202211210415065.png)

```java
@Data
public class SkuVo extends SkuEntity {

    // sku 的 图片列表
    private List<String> images;

    // 销售属性
    private List<SkuAttrValueEntity> saleAttrs;

    /*                       积分优惠信息                       */

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
//    private Integer work;
    private List<Integer> work;


    /*                       满减优惠信息                       */

    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
//    private Integer addOther;
    private Integer fullAddOther;
    
}
```

![img_8.png](https://oss.yiki.tech/gmall/202211210415635.png)

```java
@Data
public class SkuVo extends SkuEntity {

    // sku 的 图片列表
    private List<String> images;

    // 销售属性
    private List<SkuAttrValueEntity> saleAttrs;

    /*                       积分优惠信息                       */

    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
//    private Integer work;
    private List<Integer> work;


    /*                       满减优惠信息                       */

    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;
    /**
     * 是否参与其他优惠
     */
//    private Integer addOther;
    private Integer fullAddOther;


    /*                       打折优惠信息                       */

    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
//    private Integer addOther;
    private Integer ladderAddOther;

}
```

![img_9.png](https://oss.yiki.tech/gmall/202211210416222.png)