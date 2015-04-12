#!/bin/sh

# resolve links - "${BASH_SOURCE-$0}" may be a softlink

this="${BASH_SOURCE-$0}"
while [ -h "$this" ]; do
  ls=`ls -ld "$this"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    this="$link"
  else
    this=`dirname "$this"`/"$link"
  fi
done

# convert relative path to absolute path
bin=`dirname "$this"`
script=`basename "$this"`
bin=`cd "$bin">/dev/null; pwd`
this="$bin/$script"

# the root of the owl installation
if [ -z "$SANFS_HOME" ]; then
  export SANFS_HOME=`dirname "$this"`/..
fi

# sanfs conf dir location.
SANFS_CONF_DIR="$SANFS_HOME/conf"

if [ -z "$SANFS_ENV_INIT" ] && [ -f "${SANFS_CONF_DIR}/sanfs-env.sh" ]; then
  . "${SANFS_CONF_DIR}/sanfs-env.sh"
  export SANFS_ENV_INIT="true"
fi

if [ -z "$JAVA_HOME" ]; then
  for candidate in \
    /usr/lib/jvm/java-6-sun \
    /usr/lib/jvm/java-1.6.0-sun-1.6.0.*/jre \
    /usr/lib/jvm/java-1.6.0-sun-1.6.0.* \
    /usr/lib/j2sdk1.6-sun \
    /usr/java/jdk1.6* \
    /usr/java/jre1.6* \
    /Library/Java/Home ; do
    if [ -e $candidate/bin/java ]; then
      export JAVA_HOME=$candidate
      break
    fi
  done
  # if we didn't set it
  if [ -z "$JAVA_HOME" ]; then
    cat 1>&2 <<EOF
+======================================================================+
|      Error: JAVA_HOME is not set and Java could not be found         |
+----------------------------------------------------------------------+
| Please download the latest Sun JDK from the Sun Java web site        |
|       > http://java.sun.com/javase/downloads/ <                      |
|                                                                      |
| credit-model requires Java 1.6 or later.                             |
| NOTE: This script will find Sun Java whether you install using the   |
|       binary or the RPM based installer.                             |
+======================================================================+
EOF
    exit 1
  fi
fi