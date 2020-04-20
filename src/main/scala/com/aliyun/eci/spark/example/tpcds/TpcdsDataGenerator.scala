package com.aliyun.eci.spark.example.tpcds

import com.databricks.spark.sql.perf.tpcds.TPCDSTables
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.Try

object TpcdsDataGenerator {

  def main(args: Array[String]): Unit = {
    var dataLocation = Constants.dataLocation
    var dataScaleFactor = Constants.dataScaleFactor
    var numPartitions = Constants.numPartitions
    if (args.length > 0) {
      dataLocation = Try(args(0).toString).getOrElse(Constants.dataLocation)
      dataScaleFactor = Try(args(1).toString).getOrElse(Constants.dataScaleFactor)
      numPartitions = Try(args(2).toInt).getOrElse(Constants.numPartitions)
    }

    // 创建sqlContext
    val conf: SparkConf = new SparkConf()
    conf.setAppName("TpcdsDataGenerator")
    conf.set("fs.oss.impl", Constants.ossImpl)
    conf.set("fs.dfs.impl", Constants.hdfsImpl)
    conf.set("mapreduce.fileoutputcommitter.algorithm.version", "2")

    // spark 2.0+
    val spark = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val sqlContext = spark.sqlContext

    // 基本参数，10T数据，有则覆盖
    val tables = new TPCDSTables(sqlContext,
      dsdgenDir = Constants.toolLocation, // location of dsdgen
      scaleFactor = dataScaleFactor,
      useDoubleForDecimal = false, // true to replace DecimalType with DoubleType
      useStringForDate = false) // true to replace DateType with StringType

    // 只生成数据
    tables.genData(
      location = dataLocation,
      format = Constants.dataFormat,
      overwrite = true, // overwrite the data that is already there
      partitionTables = true, // create the partitioned fact tables
      clusterByPartitionColumns = true, // shuffle to get partitions coalesced into single files.
      filterOutNullPartitionValues = false, // true to filter out the partition with NULL key value
      tableFilter = "", // "" means generate all tables
      numPartitions = numPartitions) // how many dsdgen partitions to run - number of input tasks.

    //tables.createTemporaryTables(dataLocation, Constants.dataFormat)

    println("TPC-DS data has been successfully generated and saved.")

    spark.stop()

  }
}
