package com.yongwang.log

import java.sql.{Connection, PreparedStatement, DriverManager}

object MySQLUtils {

  def getConnection() = {
    DriverManager.getConnection("jdbc:mysql://localhost:3306/yongwang_project?user=root&password=root")
  }

  def release(connection: Connection, pstmt: PreparedStatement): Unit = {
    try {
      if (pstmt != null) {
        pstmt.close()
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (connection != null) {
        connection.close()
      }
    }
  }

  def main(args: Array[String]) {
    println(getConnection())
  }

}
