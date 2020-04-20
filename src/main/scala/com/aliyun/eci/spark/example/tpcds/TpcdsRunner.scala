package com.aliyun.eci.spark.example.tpcds

import java.util.concurrent._

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.util.Try

object TpcdsRunner {

  def main(args: Array[String]): Unit = {
    var dataLocation = Constants.dataLocation
    var resultLocation = Constants.resultLocation
    var queryOptimize = false
    val databaseName = Constants.databaseName
    var iterations = Constants.iterations
    var timeout = Constants.timeout
    var queryType = 0
    var parallelism = 1
    var filterQueries = new Array[String](0)

    if (args.length > 0) {
      dataLocation = Try(args(0).toString).getOrElse(Constants.dataLocation)
      resultLocation = Try(args(1).toString).getOrElse(Constants.resultLocation)
      queryOptimize = Try(args(2).toBoolean).getOrElse(false)
      iterations = Try(args(3).toInt).getOrElse(Constants.iterations)
      timeout = Try(args(4).toInt).getOrElse(Constants.timeout)
      queryType = Try(args(5).toInt).getOrElse(0)
      parallelism = Try(args(6).toInt).getOrElse(1)
      filterQueries= Try(args(7).toString.split(",")).getOrElse(new Array[String](0))
    }

    // 创建sqlContext
    val conf: SparkConf = new SparkConf()
    conf.setAppName("TpcdsRunner")
    conf.set("fs.oss.impl", Constants.ossImpl)
    conf.set("fs.dfs.impl", Constants.hdfsImpl)
    conf.set("mapreduce.fileoutputcommitter.algorithm.version", "2")

    // spark 2.0+
    val spark = SparkSession.builder
      .config(conf)
      .getOrCreate()

    val sqlContext = spark.sqlContext

    val tables = new TPCDSTables(sqlContext,
      dsdgenDir = Constants.toolLocation, // location of dsdgen
      scaleFactor = Constants.dataScaleFactor,
      useDoubleForDecimal = false, // true to replace DecimalType with DoubleType
      useStringForDate = false) // true to replace DateType with StringType

    // Run:
    // 生成表，优化查询
    if (queryOptimize) {
      spark.sql(s"create database $databaseName")

      tables.createExternalTables(
        dataLocation,
        Constants.dataFormat,
        databaseName,
        overwrite = true,
        discoverPartitions = true)

      tables.analyzeTables(databaseName, analyzeColumns = true)

      spark.conf.set("spark.sql.cbo.enabled", "true")

      // 默认会自动切
      spark.sql(s"use $databaseName")

    } else {
      // 生成临时表
      tables.createTemporaryTables(dataLocation, Constants.dataFormat)
    }

    val tpcds = new TPCDS(sqlContext = sqlContext)

    val queries = queryType match {
      case 0 => tpcds.tpcds2_4Queries
      case 1 => tpcds.tpcds1_4Queries
      case _ => tpcds.tpcds2_4Queries
    }

    val experiments = queries
      .filter(query => !filterQueries.contains(query.name))
      .sliding(queries.size / parallelism, queries.size / parallelism)
      .map(executionsToRun => {

        // 防止时间戳重复
        Thread.sleep(5)

        tpcds.runExperiment(
          executionsToRun,
          iterations = iterations,
          resultLocation = resultLocation,
          forkThread = false)
      })
      .toTraversable

    println(s"Submit ${experiments.size} TPC-DS query successfully.")

    val threadPool: ExecutorService = new ThreadPoolExecutor(
      experiments.size, experiments.size,
      0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue[Runnable]
    )
    val latch = new CountDownLatch(experiments.size)

    try {
      experiments.foreach(experiment => {
        threadPool.execute(new Runnable {
          override def run(): Unit = {
            println(s"Waiting for one of TPC-DS query batch to finish.")
            experiment.waitForFinish(timeout)
            latch.countDown()
            println(s"TPC-DS query batch finished, waiting for ${latch.getCount}")
          }
        })
      })

      println(s"Waiting for ${latch.getCount} TPC-DS query batch to finish.")

      latch.await(timeout, TimeUnit.SECONDS)

      println("TPC-DS query finish successfully.")

    } catch {
      case e: Exception =>
        println("TPC-DS query error")
        e.printStackTrace()
    } finally {
      threadPool.shutdown()
    }

    spark.stop()
  }
}
