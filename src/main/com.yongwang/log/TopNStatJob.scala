package com.yongwang.log

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ListBuffer

object TopNStatJob {

  def main(args: Array[String]) {
    val spark = SparkSession.builder().appName("TopNStatJob")
      .config("spark.sql.sources.partitionColumnTypeInference.enabled","false")
      .master("local[2]").getOrCreate()


    val accessDF = spark.read.format("parquet").load("/Users/yongwang/clean")


    val day = "20170511"

    StatDAO.deleteData(day)

    videoAccessTopNStat(spark, accessDF, day)

    cityAccessTopNStat(spark, accessDF, day)

    videoTrafficsTopNStat(spark, accessDF, day)

    spark.stop()
  }

  def videoTrafficsTopNStat(spark: SparkSession, accessDF:DataFrame, day:String): Unit = {
    import spark.implicits._

    val cityAccessTopNDF = accessDF.filter($"day" === day && $"cmsType" === "video")
    .groupBy("day","cmsId").agg(sum("traffic").as("traffics"))
    .orderBy($"traffics".desc)
    //.show(false)

    try {
      cityAccessTopNDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoTrafficsStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val traffics = info.getAs[Long]("traffics")
          list.append(DayVideoTrafficsStat(day, cmsId,traffics))
        })

        StatDAO.insertDayVideoTrafficsAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }

  def cityAccessTopNStat(spark: SparkSession, accessDF:DataFrame, day:String): Unit = {
    import spark.implicits._

    val cityAccessTopNDF = accessDF.filter($"day" === day && $"cmsType" === "video")
    .groupBy("day","city","cmsId")
    .agg(count("cmsId").as("times"))


    val top3DF = cityAccessTopNDF.select(
      cityAccessTopNDF("day"),
      cityAccessTopNDF("city"),
      cityAccessTopNDF("cmsId"),
      cityAccessTopNDF("times"),
      row_number().over(Window.partitionBy(cityAccessTopNDF("city"))
      .orderBy(cityAccessTopNDF("times").desc)
      ).as("times_rank")
    ).filter("times_rank <=3") //.show(false)  //Top3


    try {
      top3DF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayCityVideoAccessStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val city = info.getAs[String]("city")
          val times = info.getAs[Long]("times")
          val timesRank = info.getAs[Int]("times_rank")
          list.append(DayCityVideoAccessStat(day, cmsId, city, times, timesRank))
        })

        StatDAO.insertDayCityVideoAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }

  def videoAccessTopNStat(spark: SparkSession, accessDF:DataFrame, day:String): Unit = {

    import spark.implicits._

    val videoAccessTopNDF = accessDF.filter($"day" === day && $"cmsType" === "video")
    .groupBy("day","cmsId").agg(count("cmsId").as("times")).orderBy($"times".desc)
    videoAccessTopNDF.show(false)
    try {
      videoAccessTopNDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoAccessStat]
        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val times = info.getAs[Long]("times")
          list.append(DayVideoAccessStat(day, cmsId, times))
        })
        StatDAO.insertDayVideoAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }

}
