package com.aliyun.eci.spark.example.tpcds

/**
  * 参数默认值
  */
object Constants {
  // 数据存放的地址，提前扩容/预留
  val dataLocation = "dfs://f-xxx.cn-hangzhou.dfs.aliyuncs.com:10290/pod/data/tpc-ds-data"
  // 计算结果存放的我日志
  val resultLocation = "dfs://f-xxx.cn-hangzhou.dfs.aliyuncs.com:10290/pod/data/tpc-ds-result"
  // 数据生成器安装的位置，自定义目录，但是默认会去/tmp/tpcds-kit/tools下面找
  val toolLocation = "/tmp/tpcds-kit/tools"
  // 列式存储
  val dataFormat = "parquet"
  // 单位为G，生成10T测试数据
  val dataScaleFactor = "1"
  // 写入的并行度
  val numPartitions = 100
  // 查询迭代的次数
  val iterations = 1
  // 查询测试的超时时间
  val timeout = 24 * 60 * 60
  // 数据库名，在查询需要优化的时候使用
  val databaseName = "tpcdsdb"
  // aliyun oss
  val ossImpl = "org.apache.hadoop.fs.aliyun.oss.AliyunOSSFileSystem"
  // aliyun hdfs
  val hdfsImpl = "com.alibaba.dfs.DistributedFileSystem"

}
