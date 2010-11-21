
# full giant job
COMMON="-c /tmp/lsh_hadoop/common-dim100-site.xml"

FULL="/tmp/lsh_hadoop/project-site.xml /cygwin/home/lance/github/LSH-Hadoop/project/test/resources/SV_GL-points-site.xml /cygwin/home/lance/github/LSH-Hadoop/project/test/resources/SV_GL-corners-site.xml"


CP="/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-cli-1.2.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-codec-1.3.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-el-1.0.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-httpclient-3.0.1.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-logging-1.0.4.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-logging-api-1.0.4.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/commons-net-1.4.1.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/core-3.1.1.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/hsqldb-1.8.0.10.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/jasper-compiler-5.5.12.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/jasper-runtime-5.5.12.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/jets3t-0.6.1.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/jetty-6.1.14.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/jetty-util-6.1.14.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/junit-3.8.1.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/kfs-0.2.2.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/log4j-1.2.15.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/mockito-all-1.8.0.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/oro-2.0.8.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/servlet-api-2.5-6.1.14.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/slf4j-api-1.4.3.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/slf4j-log4j12-1.4.3.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/xmlenc-0.52.jar;/cygwin/home/lance/open/hadoop/hadoop-0.20.2/hadoop-0.20.2-core.jar;/cygwin/home/lance/open/mahout/math/target/dependency/commons-math-1.2.jar;/cygwin/home/lance/open/mahout/examples/target/dependency/mahout-core-0.4-SNAPSHOT.jar"

export CLASSPATH="$CP;/cygwin/home/lance/github/LSH-Hadoop/project/bin/;/cygwin/home/lance/github/LSH-Hadoop/mahout/bin/"
OPTS=-XX:+UseConcMarkSweepGC 
java -Xmx350m "$OPTS" lsh.hadoop.LSHDriver "$COMMON" $FULL
