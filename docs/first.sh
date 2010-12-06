
FULL="/tmp/lsh_hadoop/project-site.xml"

COMMON="-c common-dim100-site.xml"

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


OPTS=-XX:+UseConcMarkSweepGC 
java -Xmx250m "$OPTS" lsh.hadoop.LSHDriver "$COMMON" $FULL
