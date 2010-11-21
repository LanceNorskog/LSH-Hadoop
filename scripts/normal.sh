


export CLASSPATH="\
/cygwin/home/lance/github/LSH-Hadoop/project/bin/;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/bin/;\
/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/*;\
/cygwin/home/lance/open/hadoop/hadoop-0.20.2/hadoop-0.20.2-core.jar;\
/cygwin/home/lance/open/mahout/math/target/classes;\
/cygwin/home/lance/open/mahout/core/target/classes;\
/cygwin/home/lance/open/mahout/core/target/dependency/*;\
/cygwin/home/lance/open/mahout/examples/target/classes;\
"


# OPTS=-XX:+UseConcMarkSweepGC 

[ $# -ge 3 ] || exit "Usage: normal.sh ratings.dat points/part-r-00000 corners/part-r-00000"


java $OPTS -Xmx600m org.apache.mahout.cf.taste.impl.eval.NormalRankingRecommenderEvaulator "$1" "$2" "$3"
