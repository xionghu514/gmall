# 搜索

* 查询: 根据用户输入的条件查询出用户所需要的数据
* 搜索引擎: 根据用户需求结合特定的算法, 运用特定的策略检索出指定信息并反馈给用户的一门检索技术

## 与 MySQL 对比

> Es 中 一个 索引库只能有一个 类型所以显得 type 很鸡肋, 7.x 废弃

![](https://oss.yiki.tech/oss/202212121224104.png)

## DSL 

### 索引库

```
# 查询所有索引库
GET /_cat/indices?v
```

![](https://oss.yiki.tech/oss/202212121227455.png)

```
# 查询索引具体信息 GET /索引名
GET /索引名
```

![](https://oss.yiki.tech/oss/202212121227381.png)

```
# 创建索引, PUT /索引名
PUT /索引名

# 参数可选：指定分片及副本，默认分片为3，副本为2。
PUT /mall
{
    "settings": {
        "number_of_shards": 3,
        "number_of_replicas": 2
      }
}
```

```
# 删除索引, DELETE /索引库名
DELETE /索引库名
```

### 映射(_mapping)

> 映射是定义文档的过程，文档包含哪些字段，这些字段是否保存，是否索引，是否分词等

```
# 创建映射(不能修改 / 修改映射)
# string类型: text(会进行分词)、keyword(不会进行分词)
# index: 是否创建索引, 取决于要不要已改字段进行搜索(默认为 true)
# store：是否存储，默认为false，即使为false也会存储到_source中，如果为true则会额外存储一份
# analyzer: 指定分词器 ik_max_work
PUT /索引库名/_mapping
{
  "properties": {
    "字段名": {
      "type": "类型",
      "index": true，
      "store": true，
      "analyzer": "分词器"
    }
  }
}
```

```
# 查看映射关系
GET /索引库名/_mapping
```

### 新增文档（document）

> 有了索引、类型和映射，就可以对文档做增删改查操作了

```
# 新增指定 id 的文档(数据不存在新增, 已存在 修改(覆盖更新 覆盖时只有部分字段 最终结果也只有这些字段), _version表示修改多少次 result 表示是 更新还是新增)
POST /索引库名/_doc/id值
{
    // 属性名: 属性值
}
```

```
# 查询指定 id 文档
GET /索引库名/_doc/id值
```

```
# 查询索引库所有数据
GET /索引库名/_search
```

```
# 删除指定 id 文档
DELETE /索引库名/_doc/id 值
```

## 文档查询

```
POST /mall/_bulk
{"index":{"_id":1}}
{"title":"小米手机","images":"http://image.jd.com/12479122.jpg","price":1999,"stock":200,"attr":{"category":"手机","brand":"小米"}}
{"index":{"_id":2}}
{"title":"超米手机","images":"http://image.jd.com/12479122.jpg","price":2999,"stock":300,"attr":{"category":"手机","brand":"小米"}}
{"index":{"_id":3}}
{"title":"小米电视","images":"http://image.jd.com/12479122.jpg","price":3999,"stock":400,"attr":{"category":"电视","brand":"小米"}}
{"index":{"_id":4}}
{"title":"小米笔记本","images":"http://image.jd.com/12479122.jpg","price":4999,"stock":200,"attr":{"category":"笔记本","brand":"小米"}}
{"index":{"_id":5}}
{"title":"华为手机","images":"http://image.jd.com/12479122.jpg","price":3999,"stock":400,"attr":{"category":"手机","brand":"华为"}}
{"index":{"_id":6}}
{"title":"华为笔记本","images":"http://image.jd.com/12479122.jpg","price":5999,"stock":200,"attr":{"category":"笔记本","brand":"华为"}}
{"index":{"_id":7}}
{"title":"荣耀手机","images":"http://image.jd.com/12479122.jpg","price":2999,"stock":300,"attr":{"category":"手机","brand":"华为"}}
{"index":{"_id":8}}
{"title":"oppo手机","images":"http://image.jd.com/12479122.jpg","price":2799,"stock":400,"attr":{"category":"手机","brand":"oppo"}}
{"index":{"_id":9}}
{"title":"vivo手机","images":"http://image.jd.com/12479122.jpg","price":2699,"stock":300,"attr":{"category":"手机","brand":"vivo"}}
{"index":{"_id":10}}
{"title":"华为nova手机","images":"http://image.jd.com/12479122.jpg","price":2999,"stock":300,"attr":{"category":"手机","brand":"华为"}}
```

#### 查询结果

- took：查询花费时间，单位是毫秒
- time_out：是否超时
- _shards：分片信息
- hits：搜索结果总览对象
  - total：搜索到的总条数
  - max_score：所有结果中文档得分的最高分
  - hits：搜索结果的文档对象数组，每个元素是一条搜索到的文档信息
    - _index：索引库
    - _type：文档类型
    - _id：文档id
    - _score：文档得分
    - _source：文档的源数据

#### 匹配查询（match）

```
# 查询结果 不仅仅包含 小米手机, 而且 小米 & 手机 相关的都会查询到. 多个词 默认是 or 的关系
GET /mall/_search
{
  "query": { 
    "match": {
      "title": "小米手机"
    }
  }
}
```

```
# operator 限定查询关系
GET /mall/_search
{
  "query": {
    "match": {
      "title": {
        "query": "小米手机",
        "operator": "and"
      }
    }
  }
}
```

#### 词条查询（term）

> 词条: 一个最小的分词单元. `term` 查询被用于精确值 匹配，这些精确值可能是数字、时间、布尔或者那些**未分词**的字符串。

```
GET /mall/_search
{
  "query": {
    "term": {
      "price": 4999
    }
  }
}
```

```
# 多词条查询. 分词与分词间是 or 的关系
GET /mall/_search
{
  "query": {
    "terms": {
      "title": [
        "小米",
        "手机"
      ]
    }
  }
}
```

#### 范围查询（range）

> `range` 查询找出那些落在指定区间内的数字或者时间

* `range`查询允许以下字符：

| 操作符 |   说明   |
| :----: | :------: |
|   gt   |   大于   |
|  gte   | 大于等于 |
|   lt   |   小于   |
|  lte   | 小于等于 |

```
# 范围查询（range）
GET /mall/_search
{
    "query":{
        "range": {
            "price": {
                "gte":  1000,
                "lt":   3000
            }
    	}
    }
}
```

#### 布尔组合（bool)

> `bool`把各种其它查询通过`must`（与）、`must_not`（非）、`should`（或）的方式进行组合

```
# 布尔组合（bool) # must（与）、must_not（非）、should（或）
# 一个组合查询里面只能出现一种组合，不能混用
# 查询 价格大于 2699 并且 小于 3999 的 手机
GET /mall/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "手机"
          }
        },
        {
          "range": {
            "price": {
              "gte": 2699,
              "lte": 3999
            }
          }
        }
      ]
    }
  }
}
```

#### 过滤（filter）

> 所有的查询都会影响到文档的评分及排名。如果我们需要在查询结果中进行过滤，并且不希望过滤条件影响评分，那么就不要把过滤条件作为查询条件来用。而是使用`filter`方式

```
# 过滤(依赖 bool 查询 效果类似于 bool 查询, 但是不影响得分进而不会影响排名) 
# `filter`中还可以再次进行`bool`组合条件过滤。
GET /mall/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "title": "手机"
          }
        }
      ],
      "filter": [
        {
          "range": {
            "price": {
              "gte": 2699,
              "lte": 3999
            }
          }
        }
      ]
    }
  }
}
```

#### 排序（sort）

> `sort` 可以让我们按照不同的字段进行排序，并且通过`order`指定排序的方式

```
GET /mall/_search
{
  "query": {
    "match": {
      "title": "小米手机"
    }
  },
  "sort": [
    {
      "price": { "order": "desc" }
    },
    {
      "_score": { "order": "desc"}
    }
  ]
}
```

#### 分页（from/size）

> from：从那一条开始
>
> size：取多少条

```
GET /mall/_search
{
  "query": {
    "match": {
      "title": "小米手机"
    }
  },
  "from": 2,
  "size": 2
}
```

#### 高亮（highlight）

> 高亮的本质是给关键字添加了<em>标签，在前端再给该标签添加样式即可
>
> fields：高亮字段
>
> pre_tags：前置标签
>
> post_tags：后置标签

```

GET /mall/_search
{
  "query": {
    "match": {
      "title": "小米"
    }
  },
  "highlight": {
    "fields": {"title": {}}, 
    "pre_tags": "<em>",
    "post_tags": "</em>"
  }
}
```

#### 结果过滤（_source）

> 默认情况下，elasticsearch在搜索的结果中，会把文档中保存在`_source`的所有字段都返回。
>
> 如果我们只想获取其中的部分字段，可以添加`_source`的过滤

```
GET /mall/_search
{
  "_source": ["title","price"],
  "query": {
    "term": {
      "price": 2699
    }
  }
}
```

#### 聚合（aggregations）

> 聚合可以让我们极其方便的实现对数据的统计、分析
>
> 实现这些统计功能的比数据库的sql要方便的多，而且查询速度非常快，可以实现实时搜索效果。

- 什么品牌的手机最受欢迎？
- 这些手机的平均价格、最高价格、最低价格？
- 这些手机每月的销售情况如何？

##### 基本概念

> Elasticsearch中的聚合，包含多种类型，最常用的两种，一个叫`桶`，一个叫`度量`

* 桶（bucket）:	桶的作用，是按照某种方式对数据进行分组，每一组数据在ES中称为一个`桶`，例如我们根据国籍对人划分，可以得到`中国桶`、`英国桶`，`日本桶`……或者我们按照年龄段对人进行划分：0~10,10~20,20~30,30~40等。
  * Date Histogram Aggregation：根据日期阶梯分组，例如给定阶梯为周，会自动每周分为一组
  * Histogram Aggregation：根据数值阶梯分组，与日期类似
  * Terms Aggregation：根据词条内容分组，词条内容完全匹配的为一组
  * Range Aggregation：数值和日期的范围分组，指定开始和结束，然后按段分组
  * ......
* 度量（metrics）：bucket aggregations 只负责对数据进行分组，并不进行计算，因此往往bucket中往往会嵌套另一种聚合：metrics aggregations即度量
  * 分组完成以后，我们一般会对组中的数据进行聚合运算，例如求平均值、最大、最小、求和等，这些在ES中称为`度量`
    * Avg Aggregation：求平均值
    * Max Aggregation：求最大值
    * Min Aggregation：求最小值
    * Percentiles Aggregation：求百分比
    * Stats Aggregation：同时返回avg、max、min、sum、count等
    * Sum Aggregation：求和
    * Top hits Aggregation：求前几
    * Value Count Aggregation：求总数
    * ...

##### 聚合为桶 类似于 mysql 中的 group by

- size： 查询条数，这里设置为0，因为我们不关心搜索到的数据，只关心聚合结果，提高效率
- aggs：声明这是一个聚合查询，是aggregations的缩写
  - brands：给这次聚合起一个名字，任意。
    - terms：划分桶的方式，这里是根据词条划分
      - field：划分桶的字段

```
GET /mall/_search
{
    "size" : 0,
    "aggs" : { 
        "brands" : { 
            "terms" : { 
              "field" : "attr.brand.keyword"
            }
        }
    }
}
```

- hits：查询结果为空，因为我们设置了size为0
- aggregations：聚合的结果
- brands：我们定义的聚合名称
- buckets：查找到的桶，每个不同的品牌字段值都会形成一个桶
  - key：这个桶对应的品牌字段的值
  - doc_count：这个桶中的文档数量

##### 桶内度量 类似于 mysql 中的 聚合函数 ave max min

* 前面的例子告诉我们每个桶里面的文档数量，这很有用。 但通常，我们的应用需要提供更复杂的文档度量。 例如，每种品牌手机的平均价格是多少？

* 因此，我们需要告诉Elasticsearch`使用哪个字段`，`使用何种度量方式`进行运算，这些信息要嵌套在`桶`内，`度量`的运算会基于`桶`内的文档进行

```
# 聚合结果添加 求价格平均值的度量
GET /mall/_search
{
    "size" : 0,
    "aggs" : { 
        "brands" : { 
            "terms" : { 
              "field" : "attr.brand.keyword"
            },
            "aggs":{
                "avg_price": { 
                   "avg": {
                      "field": "price" 
                   }
                }
            }
        }
    }
}
```

- aggs：我们在上一个aggs(brands)中添加新的aggs。可见`度量`也是一个聚合
- avg_price：聚合的名称
- avg：度量的类型，这里是求平均值
- field：度量运算的字段

##### 桶内嵌套桶

> 桶不仅可以嵌套运算， 还可以再嵌套其它桶。也就是说在每个分组中，再分更多组。

```
GET /atguigu/_search
{
    "size" : 0,
    "aggs" : { 
        "brands" : { 
            "terms" : { 
              "field" : "attr.brand.keyword"
            },
            "aggs":{
                "avg_price": { 
                   "avg": {
                      "field": "price" 
                   }
                },
                "categorys": {
                  "terms": {
                    "field": "attr.category.keyword"
                  }
                }
            }
        }
    }
}
```

- 我们可以看到，新的聚合`categorys`被嵌套在原来每一个`brands`的桶中。
- 每个品牌下面都根据 `attr.category.keyword`字段进行了分组
- 我们能读取到的信息：
  - 华为有4中产品
  - 华为产品的平均售价是 3999.0美元。
  - 其中3种手机产品，1种笔记本产品