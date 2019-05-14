package com.yongwang.log

import org.apache.spark.sql.SparkSession


object SparkStatFormatJob {

  def main(args: Array[String]) {

    val spark = SparkSession.builder().appName("SparkStatFormatJob")
      .master("local[2]").getOrCreate()

    val acccess = spark.sparkContext.textFile("file:///Users/yongwang/10000_access.log")

    acccess.map(line => {
      val splits = line.split(" ")
      val ip = splits(0)

      /**
       * [10/Nov/2016:00:01:02 +0800] ==> yyyy-MM-dd HH:mm:ss
       */
      val time = splits(3) + " " + splits(4)
      val url = splits(11).replaceAll("\"","")
      val traffic = splits(9)
      DateUtils.parse(time) + "\t" + url + "\t" + traffic + "\t" + ip
    }).saveAsTextFile("file:///Users/yongwang/output/")

    spark.stop()
  }

}
