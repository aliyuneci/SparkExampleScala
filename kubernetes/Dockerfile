FROM registry.cn-hangzhou.aliyuncs.com/acs/spark:ack-2.4.5-f757ab6
RUN rm $SPARK_HOME/jars/kubernetes-client-*.jar
ADD https://repo1.maven.org/maven2/io/fabric8/kubernetes-client/4.4.2/kubernetes-client-4.4.2.jar $SPARK_HOME/jars
RUN mkdir -p /opt/spark/jars
RUN mkdir -p /tmp/tpcds-kit/tools
COPY SparkExampleScala-assembly-0.1.jar /opt/spark/jars
COPY tools/ /tmp/tpcds-kit/tools