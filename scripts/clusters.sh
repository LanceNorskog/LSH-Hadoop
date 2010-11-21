



# OPTS=-XX:+UseConcMarkSweepGC 

[ $# -ge 1 ] || exit "Usage: clusters.sh points/part-r-00000 [training points file]"

export CLASSPATH="\
/cygwin/home/lance/github/LSH-Hadoop/project/bin/;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/bin/;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/lib/*;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/lib/gson-1.3.jar;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/lib/commons-logging-1.1.1.jar;\
/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/*;\
/cygwin/home/lance/open/hadoop/hadoop-0.20.2/hadoop-0.20.2-core.jar;\
/cygwin/home/lance/open/mahout/math/target/classes;\
/cygwin/home/lance/open/mahout/core/target/classes;\
/cygwin/home/lance/open/mahout/core/target/dependency/*;\
"


java $OPTS -Xmx100m lsh.mahout.clustering.KMeansTest "$1" "$2"
