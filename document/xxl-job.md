# 定时任务

* 什么是定时任务
  * 时间驱动: 整点发送优惠券，每天更新收益，每天刷新标签数据和人群数据
  * 批量处理数据: 按月批量统计报表数据，批量更新短信状态，实时性要求不高
  * 异步执行解耦: 活动状态刷新，数据同步，异步执行离线查询，与内部逻辑解耦。

* 场景
  * 报表
  * mq 重试重发
  * 购物车数据同步: 异步新增到 mysql, 可能会失败, 如果一直不同步的话, 可能导致数据失真
  
* 实现方式
  * jdk: juc 提供了定时任务任务线程池、Timer 定时器、死循环
  * springScheduling声明式定时任务：@EnableScheduling、@Scheduled
  * 定时任务框架quartz
  * 分布式定时任务: xxl-job
  * MQ 延迟队列
  
* 传统定时任务缺点
  * 触发器和定时任务的代码耦合
  * 修改触发器需要重启服务
  * 无法统一管理任务
  * 单机版本的
  
## XXL-JOB

* 项目结构
  * xxl-job-core: 核心依赖
  * xxl-job-admin: 调度中心, 统一管理任务, 动态修改配置不重启, 触发调度任务, 并提供管理页面
  * xxl-job-executor-samples: 执行器案例工程, 本质就是一个普通的工程, 在执行器工程中定义定时任务
  
* 步骤
  * 搭建调度中心
    * 把 doc/db/tables_xxl_job.sql 导入到 mysql 中
    * 修改配置 application.properties
      * 端口、数据源、token
    * 打包: mvn clean package -Dmaven.skip.test=true
    * 运行: nohup java -jar xxl-job-admin-2.2.0.jar >xxl.log &
  * 搭建执行器工程, 并定义任务
    * 引入依赖: xxl-job-core
    * 配置: application.properties
      * 端口、调度中心地址 accessToken 应用名 日志目录
      * XxlJobConfig.java 配置文件: 直接 copy
    * 编写任务
      * 方法必须返回 ReturnT<String>
      * 方法必须有 String 类型参数
      * 方法上必须添加 @XxlJob("唯一标识")
      * 如果方法向向调度中心输出日志: XxlJobLogger.log("日志内容")
  * 在调度中心中配置任务
    * 配置执行器：appName(要和执行器工程配置文件中的appname一致) 名称（介绍） 注册方式（一般选择自动注册）
    * 配置任务
      * 路由策略：第一个 最后一个 轮询 随机 最不经常使用 最近最久未使用 一致性hash 故障转移 忙碌转移 分片广播
      * 运行模式：BEAN模式
      * 阻塞处理策略：单机串行 丢弃后续调度 覆盖之前调度（一般选择前两个）
    * 查看日志或报表统计
