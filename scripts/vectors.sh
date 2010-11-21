



# OPTS=-XX:+UseConcMarkSweepGC 

[ $# -ge 2 ] || exit "Usage: vectors.sh [-stuff] points/part-r-00000 vectors/part-r-00000"

export CLASSPATH="\
/cygwin/home/lance/github/LSH-Hadoop/project/bin/;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/bin/;\
/cygwin/home/lance/github/LSH-Hadoop/mahout/lib/*;\
/cygwin/home/lance/open/hadoop/hadoop-0.20.2/lib/*;\
/cygwin/home/lance/open/hadoop/hadoop-0.20.2/hadoop-0.20.2-core.jar;\
/cygwin/home/lance/open/mahout/math/target/classes;\
/cygwin/home/lance/open/mahout/core/target/classes;\
/cygwin/home/lance/open/mahout/utils/target/classes;\
/cygwin/home/lance/open/mahout/core/target/dependency/*;\
"


java $OPTS -Xmx100m lsh.mahout.io.WriteVectors $@
