name := "SparkExampleScala"

version := "0.1"

scalaVersion := "2.11.12"

// 使用sbt-assembly，版本信息在project/plugins.sbt
// 使用sbt-assembly会把所有依赖打入应用jar，包括本地lib库
// 所以不需要打入应用jar的spark库要追加provided排除，只在本地编译时生效

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5" %"provided"
// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5" %"provided"
// https://mvnrepository.com/artifact/com.aliyun.dfs/aliyun-sdk-dfs
// 阿里云的hdfs依赖base镜像中没有，需要打入应用jar
libraryDependencies += "com.aliyun.dfs" % "aliyun-sdk-dfs" % "1.0.3"
// https://mvnrepository.com/artifact/com.aliyun.oss/aliyun-sdk-oss
libraryDependencies += "com.aliyun.oss" % "aliyun-sdk-oss" % "3.4.1"
// https://mvnrepository.com/artifact/org.jdom/jdom
libraryDependencies += "org.jdom" % "jdom" % "1.1"
