#!/bin/bash

bin=`dirname "$0"`
bin=`cd "$bin">/dev/null; pwd`

# This will set SANFS_HOME, etc.
. "$bin"/sanfs-config.sh

cd $SANFS_HOME

pathsep=":"

JARS=`find $SANFS_HOME/lib -name "*.jar"`
#echo $JARS
for i in $JARS; do
    if [ -n "$CLASSPATH" ]; then
        CLASSPATH=${CLASSPATH}${pathsep}${i}
    else
        CLASSPATH=${i}
    fi
done

JRE_HOME=$JAVA_HOME/jre
CLASSPATH=.:$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH

#echo $CLASSPATH
java -cp $CLASSPATH aaron.sanfs.nameserver.NameServerLauncher