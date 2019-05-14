package com.yongwang.log


object IpUtils {
  val iptoCity = Map(
    "192.168.17.32" -> "CityA",
    "192.168.17.33" -> "CityB",
    "192.168.17.34" -> "CityC"
  )
  def getCity(ip:String) = {
    iptoCity.get(ip).getOrElse("unknow")
  }
}
