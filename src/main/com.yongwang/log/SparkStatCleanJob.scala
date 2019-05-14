package com.yongwang.log

import org.apache.spark.sql.{SaveMode, SparkSession}

object SparkStatCleanJob {

  def main(args: Array[String]) {
    val spark = SparkSession.builder().appName("SparkStatCleanJob")
      .config("spark.sql.parquet.compression.codec","gzip")
      .master("local[2]").getOrCreate()

    val accessRDD = spark.sparkContext.textFile("/Users/yongwang/access.log")
    val accessDF = spark.createDataFrame(accessRDD.map(x => AccessConvertUtil.parseLog(x)),
      AccessConvertUtil.struct)
    accessDF.coalesce(1).write.format("parquet").mode(SaveMode.Overwrite)
      .partitionBy("day").save("/Users/yongwang/clean")
    spark.stop
  }
}
